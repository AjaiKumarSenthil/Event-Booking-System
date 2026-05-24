package com.bookmyshow.inventory.show.service.impl;

import com.bookmyshow.inventory.common.enums.SeatStatus;
import com.bookmyshow.inventory.exception.ConflictException;
import com.bookmyshow.inventory.exception.ResourceNotFoundException;
import com.bookmyshow.inventory.exception.SeatNotAvailableException;
import com.bookmyshow.inventory.model.*;
import com.bookmyshow.inventory.movie.entity.MovieLanguageFormat;
import com.bookmyshow.inventory.movie.repository.MovieLanguageFormatRepository;
import com.bookmyshow.inventory.show.entity.Show;
import com.bookmyshow.inventory.show.entity.ShowSeat;
import com.bookmyshow.inventory.show.mapper.MovieShowsMapper;
import com.bookmyshow.inventory.show.mapper.ShowResponseMapper;
import com.bookmyshow.inventory.show.mapper.ShowSeatMapper;
import com.bookmyshow.inventory.show.mapper.TheatreShowsMapper;
import com.bookmyshow.inventory.show.redis.SeatHoldRedisRepository;
import com.bookmyshow.inventory.show.repository.ShowRepository;
import com.bookmyshow.inventory.show.repository.ShowSeatRepository;
import com.bookmyshow.inventory.show.service.IShowService;
import com.bookmyshow.inventory.theatre.entity.Screen;
import com.bookmyshow.inventory.theatre.entity.Seat;
import com.bookmyshow.inventory.theatre.entity.Theatre;
import com.bookmyshow.inventory.theatre.repository.ScreenRepository;
import com.bookmyshow.inventory.theatre.repository.SeatRepository;
import com.bookmyshow.inventory.theatre.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowServiceImpl implements IShowService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final MovieLanguageFormatRepository movieLanguageFormatRepository;
    private final SeatHoldRedisRepository seatHoldRedis;

    private final TheatreShowsMapper theatreShowsMapper;
    private final ShowSeatMapper showSeatMapper;
    private final MovieShowsMapper movieShowsMapper;
    private final ShowResponseMapper showResponseMapper;

    @Override
    public MovieShowListResponse getMovieShows(UUID movieId, Integer cityId, Integer langId,
                                               Integer formatId, LocalDate date) {

        LocalDate today = LocalDate.now(IST);
        LocalDate showDate = (date != null) ? date : today;

        LocalDateTime startTime = showDate.equals(today)
                ? LocalDateTime.now(IST)
                : showDate.atStartOfDay();
        LocalDateTime endTime = showDate.plusDays(1).atStartOfDay();

        List<Show> shows = fetchShows(movieId, cityId, langId, formatId, startTime, endTime);

        List<TheatreShows> theatreShowsList = shows.stream()
                .collect(Collectors.groupingBy(
                        show -> show.getScreen().getTheatre().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()))
                .values().stream()
                .map(theatreShowsMapper::toDto)
                .toList();

        return new MovieShowListResponse().shows(theatreShowsList);
    }

    @Override
    public ShowSeats getShowSeats(UUID showId) {
        Show show = showRepository.findByIdWithDetails(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        List<ShowSeat> showSeats = showSeatRepository.findAllByShowIdWithSeat(showId);


        List<UUID> availableIds = showSeats.stream()
                .filter(ss -> ss.getStatus() == SeatStatus.AVAILABLE)
                .map(ShowSeat::getId)
                .toList();
        Set<UUID> held = seatHoldRedis.heldAmong(availableIds);

        List<SeatRow> seatRows = showSeats.stream()
                .collect(Collectors.groupingBy(
                        ss -> ss.getSeat().getRowLabel(),
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet().stream()
                .map(entry -> showSeatMapper.toSeatRow(entry.getKey(), entry.getValue(), held))
                .toList();

        return showSeatMapper.toDto(show, seatRows);
    }

    @Override
    public TheatreShowsDetailResponse getTheatreShows(UUID theatreId, LocalDate date) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found"));

        LocalDate today = LocalDate.now(IST);
        LocalDate showDate = (date != null) ? date : today;

        LocalDateTime startTime = showDate.equals(today)
                ? LocalDateTime.now(IST)
                : showDate.atStartOfDay();
        LocalDateTime endTime = showDate.plusDays(1).atStartOfDay();

        List<Show> shows = showRepository.findByTheatreIdAndDate(theatreId, startTime, endTime);

        List<MovieShows> movieShowsList = shows.stream()
                .collect(Collectors.groupingBy(
                        show -> show.getMlf().getMovie().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()))
                .values().stream()
                .map(movieShowsMapper::toMovieShows)
                .toList();

        return new TheatreShowsDetailResponse()
                .theatreId(theatre.getId())
                .theatreName(theatre.getName())
                .location(theatre.getAddress())
                .movies(movieShowsList);
    }

    /**
     * Seat-hold protocol:
     * <ol>
     *   <li>Pessimistically lock the rows so a concurrent {@code confirmSeats}
     *       cannot flip a seat to {@code BOOKED} between our read and our hold.</li>
     *   <li>Reject if any seat is already {@code BOOKED} in Postgres.</li>
     *   <li>Atomically reserve all seats in Redis (all-or-nothing via Lua).</li>
     * </ol>
     * Postgres is never written to here — the {@code AVAILABLE -> BLOCKED -> AVAILABLE}
     * dance is gone, replaced by a Redis key whose TTL handles abandonment.
     */
    @Override
    @Transactional
    public SeatLockResponse lockSeats(UUID showId, List<SeatIdentifier> seats) {
        Show show = showRepository.findByIdWithDetails(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found"));

        List<String> seatKeys = seats.stream()
                .map(s -> s.getRowLabel() + "-" + s.getSeatNumber())
                .toList();

        List<ShowSeat> showSeats = showSeatRepository
                .findByShowIdAndSeatsForUpdate(showId, seatKeys);

        if (showSeats.size() != seats.size()) {
            log.warn("Lock rejected: unknown seats. showId={} requested={} found={}",
                    showId, seats.size(), showSeats.size());
            throw new ResourceNotFoundException("One or more seats do not exist for this show");
        }

        List<ShowSeat> bookedAlready = showSeats.stream()
                .filter(ss -> ss.getStatus() == SeatStatus.BOOKED)
                .toList();
        if (!bookedAlready.isEmpty()) {
            log.warn("Lock rejected: seats already booked. showId={} seats={}",
                    showId, labelsOf(bookedAlready));
            throw new SeatNotAvailableException("Seats already booked: " + labelsOf(bookedAlready));
        }

        List<UUID> showSeatIds = showSeats.stream().map(ShowSeat::getId).toList();
        if (!seatHoldRedis.tryHoldAll(showSeatIds)) {
            // Re-query to give the caller a precise list of conflicting seats.
            Set<UUID> held = seatHoldRedis.heldAmong(showSeatIds);
            List<ShowSeat> conflicting = showSeats.stream()
                    .filter(ss -> held.contains(ss.getId()))
                    .toList();
            log.warn("Lock rejected: seats currently held in Redis. showId={} seats={}",
                    showId, labelsOf(conflicting));
            throw new SeatNotAvailableException("Seats currently held: " + labelsOf(conflicting));
        }

        List<LockedSeat> lockedSeats = showSeats.stream()
                .map(ss -> new LockedSeat()
                        .showSeatId(ss.getId())
                        .rowLabel(ss.getSeat().getRowLabel())
                        .seatNumber(ss.getSeat().getSeatNumber()))
                .toList();

        log.info("Seats locked: showId={} seatCount={}", showId, lockedSeats.size());
        return new SeatLockResponse()
                .showId(show.getId())
                .theatreName(show.getScreen().getTheatre().getName())
                .screenName(show.getScreen().getName())
                .movieTitle(show.getMlf().getMovie().getTitle())
                .showTime(show.getStartTime())
                .price(show.getPrice())
                .lockedSeats(lockedSeats);
    }

    /**
     * Release on cancellation. Always drops the Redis hold (no-op if already
     * expired) and flips any {@code BOOKED} rows back to {@code AVAILABLE} so
     * cancellations of confirmed bookings free their seats.
     */
    @Override
    @Transactional
    public void releaseSeats(UUID showId, List<UUID> showSeatIds) {
        seatHoldRedis.release(showSeatIds);
        showSeatRepository.updateStatusByIds(showSeatIds, SeatStatus.AVAILABLE);
        log.info("Seats released: showId={} seatCount={}", showId, showSeatIds.size());
    }

    /**
     * Promote a Redis hold into a permanent {@code BOOKED} row. DB is updated
     * first so a concurrent {@code lockSeats} that wins the pessimistic lock
     * after the DEL sees the booked status and refuses.
     */
    @Override
    @Transactional
    public void confirmSeats(UUID showId, List<UUID> showSeatIds) {
        showSeatRepository.updateStatusByIds(showSeatIds, SeatStatus.BOOKED);
        seatHoldRedis.release(showSeatIds);
        log.info("Seats confirmed (BOOKED): showId={} seatCount={}", showId, showSeatIds.size());
    }

    private static List<String> labelsOf(List<ShowSeat> seats) {
        return seats.stream()
                .map(ss -> ss.getSeat().getRowLabel() + "-" + ss.getSeat().getSeatNumber())
                .toList();
    }

    private List<Show> fetchShows(UUID movieId, Integer cityId, Integer langId,
                                  Integer formatId, LocalDateTime startTime, LocalDateTime endTime) {
        if (langId == null && formatId == null) {
            return showRepository.findByMovieIdAndCityId(movieId, cityId, startTime, endTime);
        }
        if (langId == null) {
            return showRepository.findByMovieIdCityIdFormatIdDate(movieId, cityId, formatId, startTime, endTime);
        }
        if (formatId == null) {
            return showRepository.findByMovieIdCityIdLangId(movieId, cityId, langId, startTime, endTime);
        }
        return showRepository.findByMovieIdCityIdLangIdFormatIdDate(movieId, cityId, langId, formatId, startTime, endTime);
    }

    @Override
    @Transactional
    public ShowResponse createShow(ShowCreateRequest request) {
        if (request.getStartTime() == null) {
            throw new IllegalArgumentException("startTime is required");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now(IST))) {
            throw new IllegalArgumentException("startTime must be in the future");
        }

        MovieLanguageFormat mlf = movieLanguageFormatRepository.findById(request.getMlfId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MovieLanguageFormat not found: " + request.getMlfId()));

        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + request.getScreenId()));

        LocalDateTime endTime = request.getEndTime();
        if (endTime == null) {
            Integer durationMin = mlf.getMovie() != null ? mlf.getMovie().getDurationMin() : null;
            if (durationMin == null || durationMin <= 0) {
                throw new IllegalArgumentException(
                        "endTime missing and movie duration unavailable to derive it");
            }
            endTime = request.getStartTime().plusMinutes(durationMin);
        }
        if (!endTime.isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        List<Show> overlapping = showRepository.findOverlappingShows(
                screen.getId(), request.getStartTime(), endTime);
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Screen " + screen.getId()
                    + " has an overlapping show in that time range");
        }

        List<Seat> seats = seatRepository.findAllByScreenId(screen.getId());
        if (seats.isEmpty()) {
            throw new ConflictException("Screen " + screen.getId()
                    + " has no seats; create seats before scheduling shows");
        }

        Show show = new Show();
        show.setMlf(mlf);
        show.setScreen(screen);
        show.setStartTime(request.getStartTime());
        show.setEndTime(endTime);
        show.setPrice(request.getPrice());

        Show saved = showRepository.save(show);

        List<ShowSeat> showSeats = new ArrayList<>(seats.size());
        for (Seat seat : seats) {
            ShowSeat ss = new ShowSeat();
            ss.setShow(saved);
            ss.setSeat(seat);
            ss.setStatus(SeatStatus.AVAILABLE);
            showSeats.add(ss);
        }
        showSeatRepository.saveAll(showSeats);

        Show loaded = showRepository.findByIdFullyLoaded(saved.getId()).orElse(saved);
        return showResponseMapper.toDto(loaded, seats.size());
    }

    @Override
    @Transactional
    public ShowResponse updateShow(UUID showId, ShowUpdateRequest request) {
        Show show = showRepository.findByIdFullyLoaded(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found: " + showId));

        if (showSeatRepository.existsByShow_IdAndStatus(showId, SeatStatus.BOOKED)) {
            throw new ConflictException("Show " + showId + " has booked seats; cannot update");
        }

        if (request.getMlfId() != null
                && (show.getMlf() == null || !request.getMlfId().equals(show.getMlf().getId()))) {
            MovieLanguageFormat mlf = movieLanguageFormatRepository.findById(request.getMlfId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "MovieLanguageFormat not found: " + request.getMlfId()));
            show.setMlf(mlf);
        }

        boolean screenChanged = false;
        if (request.getScreenId() != null
                && (show.getScreen() == null || !request.getScreenId().equals(show.getScreen().getId()))) {
            Screen screen = screenRepository.findById(request.getScreenId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Screen not found: " + request.getScreenId()));
            show.setScreen(screen);
            screenChanged = true;
        }

        LocalDateTime newStartTime = request.getStartTime() != null
                ? request.getStartTime() : show.getStartTime();
        LocalDateTime newEndTime = request.getEndTime() != null
                ? request.getEndTime() : show.getEndTime();

        if (request.getStartTime() != null
                && request.getStartTime().isBefore(LocalDateTime.now(IST))) {
            throw new IllegalArgumentException("startTime must be in the future");
        }
        if (!newEndTime.isAfter(newStartTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        boolean timeChanged = !newStartTime.equals(show.getStartTime())
                || !newEndTime.equals(show.getEndTime());
        if (screenChanged || timeChanged) {
            List<Show> overlapping = showRepository.findOverlappingShowsExcluding(
                    show.getScreen().getId(), showId, newStartTime, newEndTime);
            if (!overlapping.isEmpty()) {
                throw new ConflictException("Screen " + show.getScreen().getId()
                        + " has an overlapping show in the new time range");
            }
        }

        show.setStartTime(newStartTime);
        show.setEndTime(newEndTime);
        if (request.getPrice() != null) {
            show.setPrice(request.getPrice());
        }

        Show saved = showRepository.save(show);
        Show loaded = showRepository.findByIdFullyLoaded(saved.getId()).orElse(saved);
        int totalSeats = (int) seatRepository.countByScreen_Id(loaded.getScreen().getId());
        return showResponseMapper.toDto(loaded, totalSeats);
    }

    @Override
    @Transactional
    public void deleteShow(UUID showId) {
        if (!showRepository.existsById(showId)) {
            throw new ResourceNotFoundException("Show not found: " + showId);
        }
        if (showSeatRepository.existsByShow_IdAndStatus(showId, SeatStatus.BOOKED)) {
            throw new ConflictException("Show " + showId + " has booked seats; cannot delete");
        }
        showSeatRepository.deleteAllByShowId(showId);
        showRepository.deleteById(showId);
    }
}
