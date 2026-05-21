package com.bookmyshow.inventory.theatre.security;

import com.bookmyshow.inventory.show.repository.ShowRepository;
import com.bookmyshow.inventory.theatre.repository.ScreenRepository;
import com.bookmyshow.inventory.theatre.repository.TheatreRepository;
import com.bookmyshow.jwt.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Spring bean used inside {@code @PreAuthorize} SpEL expressions to gate
 * write operations on theatres/screens/shows by ownership.
 * <p>
 * Example: {@code @PreAuthorize("hasRole('ADMIN') or
 *  (hasRole('THEATRE_OWNER') and @theatreSecurity.isOwner(#theatreId))")}
 */
@Component("theatreSecurity")
@RequiredArgsConstructor
public class TheatreSecurity {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final ShowRepository showRepository;

    public boolean isOwner(UUID theatreId) {
        if (theatreId == null || !AuthContext.isAuthenticated()) {
            return false;
        }
        UUID callerId = AuthContext.getUserId();
        return theatreRepository.findById(theatreId)
                .map(theatre -> callerId.equals(theatre.getOwnerId()))
                .orElse(false);
    }

    public boolean isOwnerOfScreen(UUID screenId) {
        if (screenId == null || !AuthContext.isAuthenticated()) {
            return false;
        }
        UUID callerId = AuthContext.getUserId();
        return screenRepository.findById(screenId)
                .map(screen -> screen.getTheatre() != null
                        && callerId.equals(screen.getTheatre().getOwnerId()))
                .orElse(false);
    }

    public boolean isOwnerOfShow(UUID showId) {
        if (showId == null || !AuthContext.isAuthenticated()) {
            return false;
        }
        UUID callerId = AuthContext.getUserId();
        return showRepository.findByIdWithDetails(showId)
                .map(show -> show.getScreen() != null
                        && show.getScreen().getTheatre() != null
                        && callerId.equals(show.getScreen().getTheatre().getOwnerId()))
                .orElse(false);
    }
}
