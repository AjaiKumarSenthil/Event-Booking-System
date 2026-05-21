package com.bookmyshow.booking.service.impl;

import com.bookmyshow.booking.client.AuthClient;
import com.bookmyshow.booking.client.InventoryClient;
import com.bookmyshow.booking.client.dto.LockedSeat;
import com.bookmyshow.booking.client.dto.SeatLockResponse;
import com.bookmyshow.booking.client.dto.UserContact;
import com.bookmyshow.booking.entity.Booking;
import com.bookmyshow.booking.entity.BookingSeat;
import com.bookmyshow.booking.enums.BookingStatus;
import com.bookmyshow.booking.enums.BookingEventType;
import com.bookmyshow.booking.event.BookingEvent;
import com.bookmyshow.booking.event.BookingEventProducer;
import com.bookmyshow.booking.exception.BookingNotAllowedException;
import com.bookmyshow.booking.exception.ResourceNotFoundException;
import com.bookmyshow.booking.mapper.BookingMapper;
import com.bookmyshow.booking.model.BookingListResponse;
import com.bookmyshow.booking.model.BookingRequest;
import com.bookmyshow.booking.model.BookingResponse;
import com.bookmyshow.booking.model.PaymentConfirmation;
import com.bookmyshow.booking.repository.BookingRepository;
import com.bookmyshow.booking.service.IBookingService;
import com.bookmyshow.jwt.AuthContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final BookingRepository bookingRepository;
    private final InventoryClient inventoryClient;
    private final AuthClient authClient;
    private final BookingMapper bookingMapper;
    private final BookingEventProducer bookingEventProducer;
    private final TransactionTemplate txTemplate;

    @Value("${booking.seat-hold-minutes:10}")
    private long seatHoldMinutes;

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        UUID userId = AuthContext.getUserId();
        UserContact userContact = authClient.getUserContact(userId);

        List<Map<String, Object>> seats = request.getSeats().stream()
                .map(s -> Map.<String, Object>of(
                        "rowLabel", s.getRowLabel(),
                        "seatNumber", s.getSeatNumber()))
                .toList();
        SeatLockResponse lockResponse = inventoryClient.lockSeats(request.getShowId(), seats);

        try {
            // Short, focused transaction: just persist the booking aggregate.
            return txTemplate.execute(status -> persistBooking(userId, userContact, lockResponse));
        } catch (Exception ex) {
            List<UUID> seatIds = lockResponse.lockedSeats().stream()
                    .map(LockedSeat::showSeatId).toList();
            try {
                inventoryClient.releaseSeats(request.getShowId(), seatIds);
            } catch (Exception releaseEx) {
                log.error("Compensation failed; seats {} for show {} remain BLOCKED until expiry sweep: {}",
                        seatIds, request.getShowId(), releaseEx.getMessage(), releaseEx);
            }
            throw ex;
        }
    }

    private BookingResponse persistBooking(UUID userId, UserContact userContact, SeatLockResponse lockResponse) {
        Booking booking = bookingMapper.toEntity(
                userId, userContact.email(), userContact.fullName(), lockResponse);
        LocalDateTime now = LocalDateTime.now();
        booking.setCreatedAt(now);
        booking.setExpiresAt(now.plusMinutes(seatHoldMinutes));
        booking = bookingRepository.save(booking);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingDetails(UUID bookingId) {
        Booking booking = findBookingOrThrow(bookingId);
        assertCanAccess(booking);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingListResponse getBookings(UUID userId) {
        assertCanAccessUser(userId);

        List<BookingResponse> responses = bookingRepository.findByUserIdOrderByCreatedDesc(userId)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();

        return new BookingListResponse().bookings(responses);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = findBookingOrThrow(bookingId);
        assertCanAccess(booking);

        if (booking.getStatus() != BookingStatus.PENDING
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingNotAllowedException(
                    "Cannot cancel booking with status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        List<UUID> showSeatIds = booking.getSeats().stream()
                .map(BookingSeat::getShowSeatId)
                .toList();
        
        // inventoryClient.releaseSeats(booking.getShowId(), showSeatIds);
        bookingEventProducer.publishBookingCancelled(
                buildEvent(BookingEventType.BOOKING_CANCELLED, booking, showSeatIds));

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(UUID bookingId, PaymentConfirmation confirmation) {
        Booking booking = findBookingOrThrow(bookingId);
        assertCanAccess(booking);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BookingNotAllowedException(
                    "Cannot confirm booking with status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentId(confirmation.getPaymentId());
        booking.setPaymentMethod(confirmation.getPaymentMethod());
        bookingRepository.save(booking);

        List<UUID> showSeatIds = booking.getSeats().stream()
                .map(BookingSeat::getShowSeatId)
                .toList();
        
        // inventoryClient.releaseSeats(booking.getShowId(), showSeatIds);
        bookingEventProducer.publishBookingConfirmation(
                buildEvent(BookingEventType.BOOKING_CONFIRMED, booking, showSeatIds));

        return bookingMapper.toResponse(booking);
    }

    private Booking findBookingOrThrow(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    /**
     * Allows the caller to act on a booking only if they own it, or if they
     * have the ADMIN role. Anything else raises {@link AccessDeniedException},
     * which the global handler maps to HTTP 403.
     */
    private void assertCanAccess(Booking booking) {
        UUID currentUserId = AuthContext.getUserId();
        if (booking.getUserId().equals(currentUserId)) {
            return;
        }
        if (AuthContext.hasRole(ROLE_ADMIN)) {
            return;
        }
        throw new AccessDeniedException(
                "Caller is not authorized to access booking " + booking.getId());
    }

    private void assertCanAccessUser(UUID userId) {
        UUID currentUserId = AuthContext.getUserId();
        if (currentUserId.equals(userId)) {
            return;
        }
        if (AuthContext.hasRole(ROLE_ADMIN)) {
            return;
        }
        throw new AccessDeniedException(
                "Caller is not authorized to read bookings for user " + userId);
    }

    private BookingEvent buildEvent(BookingEventType eventType, Booking booking, List<UUID> showSeatIds) {
        return BookingEvent.builder()
                .eventType(eventType)
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .userEmail(booking.getUserEmail())
                .userName(booking.getUserName())
                .showId(booking.getShowId())
                .movieTitle(booking.getMovieTitle())
                .theatreName(booking.getTheatreName())
                .screenName(booking.getScreenName())
                .showTime(booking.getShowTime())
                .showSeatIds(showSeatIds)
                .seatLabels(booking.getSeats().stream()
                        .map(bs -> bs.getRowLabel() + "-" + bs.getSeatNumber()).toList())
                .totalAmount(booking.getTotalAmount())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
