package com.bookmyshow.inventory.show.mapper;

import com.bookmyshow.inventory.model.TheatreShows;
import com.bookmyshow.inventory.show.entity.Show;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TheatreShowsMapper {

    private final ShowTimeMapper showTimeMapper;

    public TheatreShows toDto(List<Show> shows) {
        Show firstShow = shows.getFirst();
        return new TheatreShows()
                .theatreId(firstShow.getScreen().getTheatre().getId())
                .theatreName(firstShow.getScreen().getTheatre().getName())
                .location(firstShow.getScreen().getTheatre().getAddress())
                .showTimes(shows.stream().map(showTimeMapper::toDto).toList());
    }
}
