package com.bookmyshow.inventory.show.service;

import com.bookmyshow.inventory.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface IShowService {

    MovieShowListResponse getMovieShows(UUID movieId, Integer cityId, Integer langId,
                                         Integer formatId, LocalDate date);

    ShowSeats getShowSeats(UUID showId);

    TheatreShowsDetailResponse getTheatreShows(UUID theatreId, LocalDate date);

    SeatLockResponse lockSeats(UUID showId, List<SeatIdentifier> seats);

    void releaseSeats(UUID showId, List<UUID> showSeatIds);

    void confirmSeats(UUID showId, List<UUID> showSeatIds);

    ShowResponse createShow(ShowCreateRequest request);

    ShowResponse updateShow(UUID showId, ShowUpdateRequest request);

    void deleteShow(UUID showId);
}

