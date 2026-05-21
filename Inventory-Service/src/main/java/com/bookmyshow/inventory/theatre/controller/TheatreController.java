package com.bookmyshow.inventory.theatre.controller;

import com.bookmyshow.inventory.api.TheatresApi;
import com.bookmyshow.inventory.model.Screen;
import com.bookmyshow.inventory.model.ScreenCreateRequest;
import com.bookmyshow.inventory.model.ScreenUpdateRequest;
import com.bookmyshow.inventory.model.SeatBulkCreateRequest;
import com.bookmyshow.inventory.model.SeatListResponse;
import com.bookmyshow.inventory.model.Theatre;
import com.bookmyshow.inventory.model.TheatreCreateRequest;
import com.bookmyshow.inventory.model.TheatreListResponse;
import com.bookmyshow.inventory.model.TheatreUpdateRequest;
import com.bookmyshow.inventory.theatre.service.ITheatreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TheatreController implements TheatresApi {

    private final ITheatreService theatreService;

    @Override
    public ResponseEntity<TheatreListResponse> getTheatres(Integer cityId) {
        return ResponseEntity.ok(theatreService.getTheatresByCity(cityId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Theatre> createTheatre(TheatreCreateRequest theatreCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(theatreService.createTheatre(theatreCreateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or (hasRole('THEATRE_OWNER') and @theatreSecurity.isOwner(#theatreId))")
    public ResponseEntity<Theatre> updateTheatre(UUID theatreId, TheatreUpdateRequest theatreUpdateRequest) {
        return ResponseEntity.ok(theatreService.updateTheatre(theatreId, theatreUpdateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTheatre(UUID theatreId) {
        theatreService.deleteTheatre(theatreId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or (hasRole('THEATRE_OWNER') and @theatreSecurity.isOwner(#theatreId))")
    public ResponseEntity<Screen> createScreen(UUID theatreId, ScreenCreateRequest screenCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(theatreService.createScreen(theatreId, screenCreateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or (hasRole('THEATRE_OWNER') and @theatreSecurity.isOwner(#theatreId))")
    public ResponseEntity<Screen> updateScreen(UUID theatreId, UUID screenId,
                                               ScreenUpdateRequest screenUpdateRequest) {
        return ResponseEntity.ok(theatreService.updateScreen(theatreId, screenId, screenUpdateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or (hasRole('THEATRE_OWNER') and @theatreSecurity.isOwner(#theatreId))")
    public ResponseEntity<Void> deleteScreen(UUID theatreId, UUID screenId) {
        theatreService.deleteScreen(theatreId, screenId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or (hasRole('THEATRE_OWNER') and @theatreSecurity.isOwner(#theatreId))")
    public ResponseEntity<SeatListResponse> bulkCreateSeats(UUID theatreId, UUID screenId,
                                                            SeatBulkCreateRequest seatBulkCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(theatreService.bulkCreateSeats(theatreId, screenId, seatBulkCreateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or (hasRole('THEATRE_OWNER') and @theatreSecurity.isOwner(#theatreId))")
    public ResponseEntity<Void> deleteScreenSeats(UUID theatreId, UUID screenId) {
        theatreService.deleteScreenSeats(theatreId, screenId);
        return ResponseEntity.noContent().build();
    }
}
