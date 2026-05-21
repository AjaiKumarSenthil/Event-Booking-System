package com.bookmyshow.inventory.show.controller;

import com.bookmyshow.inventory.api.ShowsApi;
import com.bookmyshow.inventory.model.MovieShowListResponse;
import com.bookmyshow.inventory.model.ShowCreateRequest;
import com.bookmyshow.inventory.model.ShowResponse;
import com.bookmyshow.inventory.model.ShowSeats;
import com.bookmyshow.inventory.model.ShowUpdateRequest;
import com.bookmyshow.inventory.model.TheatreShowsDetailResponse;
import com.bookmyshow.inventory.show.service.IShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShowController implements ShowsApi {

    private final IShowService showService;

    @Override
    public ResponseEntity<MovieShowListResponse> getMovieShows(UUID movieId, Integer cityId, Integer langId,
                                                               Integer formatId, LocalDate date) {
        return ResponseEntity.ok(showService.getMovieShows(movieId, cityId, langId, formatId, date));
    }

    @Override
    public ResponseEntity<ShowSeats> getShowSeats(UUID showId) {
        return ResponseEntity.ok(showService.getShowSeats(showId));
    }

    @Override
    public ResponseEntity<TheatreShowsDetailResponse> getTheatreShows(UUID theatreId, LocalDate date) {
        return ResponseEntity.ok(showService.getTheatreShows(theatreId, date));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('THEATRE_OWNER') and @theatreSecurity.isOwnerOfScreen(#showCreateRequest.screenId))")
    public ResponseEntity<ShowResponse> createShow(ShowCreateRequest showCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(showService.createShow(showCreateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('THEATRE_OWNER') and @theatreSecurity.isOwnerOfShow(#showId))")
    public ResponseEntity<ShowResponse> updateShow(UUID showId, ShowUpdateRequest showUpdateRequest) {
        return ResponseEntity.ok(showService.updateShow(showId, showUpdateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or "
            + "(hasRole('THEATRE_OWNER') and @theatreSecurity.isOwnerOfShow(#showId))")
    public ResponseEntity<Void> deleteShow(UUID showId) {
        showService.deleteShow(showId);
        return ResponseEntity.noContent().build();
    }
}
