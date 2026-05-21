package com.bookmyshow.inventory.show.mapper;

import com.bookmyshow.inventory.model.ShowResponse;
import com.bookmyshow.inventory.show.entity.Show;
import org.springframework.stereotype.Component;

@Component
public class ShowResponseMapper {

    public ShowResponse toDto(Show show, int totalSeats) {
        return new ShowResponse()
                .id(show.getId())
                .mlfId(show.getMlf() != null ? show.getMlf().getId() : null)
                .movieId(show.getMlf() != null && show.getMlf().getMovie() != null
                        ? show.getMlf().getMovie().getId() : null)
                .movieTitle(show.getMlf() != null && show.getMlf().getMovie() != null
                        ? show.getMlf().getMovie().getTitle() : null)
                .language(show.getMlf() != null && show.getMlf().getLanguage() != null
                        ? show.getMlf().getLanguage().getName() : null)
                .format(show.getMlf() != null && show.getMlf().getFormat() != null
                        ? show.getMlf().getFormat().getName() : null)
                .screenId(show.getScreen() != null ? show.getScreen().getId() : null)
                .screenName(show.getScreen() != null ? show.getScreen().getName() : null)
                .theatreId(show.getScreen() != null && show.getScreen().getTheatre() != null
                        ? show.getScreen().getTheatre().getId() : null)
                .theatreName(show.getScreen() != null && show.getScreen().getTheatre() != null
                        ? show.getScreen().getTheatre().getName() : null)
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .price(show.getPrice())
                .totalSeats(totalSeats);
    }
}
