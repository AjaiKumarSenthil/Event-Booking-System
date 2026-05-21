package com.bookmyshow.booking.mapper;

import com.bookmyshow.booking.client.dto.SeatLockResponse;
import com.bookmyshow.booking.entity.Booking;
import com.bookmyshow.booking.entity.BookingSeat;
import com.bookmyshow.booking.enums.BookingStatus;
import com.bookmyshow.booking.model.BookingResponse;
import com.bookmyshow.booking.model.SeatIdentifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
public class BookingMapper {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    public Booking toEntity(UUID userId, String userEmail, String userName, SeatLockResponse lock) {
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setUserEmail(userEmail);
        booking.setUserName(userName);
        booking.setShowId(lock.showId());
        booking.setTheatreName(lock.theatreName());
        booking.setScreenName(lock.screenName());
        booking.setMovieTitle(lock.movieTitle());
        booking.setShowTime(lock.showTime());
        booking.setTotalAmount(lock.price().multiply(BigDecimal.valueOf(lock.lockedSeats().size())));
        booking.setStatus(BookingStatus.PENDING);

        lock.lockedSeats().forEach(ls -> {
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setShowSeatId(ls.showSeatId());
            bs.setRowLabel(ls.rowLabel());
            bs.setSeatNumber(ls.seatNumber());
            booking.getSeats().add(bs);
        });

        return booking;
    }

    public BookingResponse toResponse(Booking booking) {
        List<SeatIdentifier> seats = booking.getSeats().stream()
                .sorted(Comparator.comparing(BookingSeat::getRowLabel)
                        .thenComparing(BookingSeat::getSeatNumber))
                .map(bs -> new SeatIdentifier()
                        .rowLabel(bs.getRowLabel())
                        .seatNumber(bs.getSeatNumber()))
                .toList();

        BookingResponse response = new BookingResponse()
                .bookingId(booking.getId())
                .showId(booking.getShowId())
                .theatreName(booking.getTheatreName())
                .screen(booking.getScreenName())
                .movieTitle(booking.getMovieTitle())
                .showTime(booking.getShowTime().toLocalTime().format(TIME_FORMAT))
                .seats(seats)
                .totalAmount(booking.getTotalAmount().doubleValue())
                .status(BookingResponse.StatusEnum.valueOf(booking.getStatus().name()));

        if (booking.getStatus() == BookingStatus.PENDING) {
            response.expiresAt(booking.getExpiresAt().atOffset(ZoneOffset.UTC));
        }

        return response;
    }
}
