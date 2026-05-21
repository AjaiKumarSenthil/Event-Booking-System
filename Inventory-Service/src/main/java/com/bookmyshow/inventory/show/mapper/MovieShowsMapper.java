package com.bookmyshow.inventory.show.mapper;

import com.bookmyshow.inventory.model.LanguageFormatShows;
import com.bookmyshow.inventory.model.MovieShows;
import com.bookmyshow.inventory.model.TheatreShowTime;
import com.bookmyshow.inventory.movie.mapper.MovieMapper;
import com.bookmyshow.inventory.show.entity.Show;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MovieShowsMapper {

    private final MovieMapper movieMapper;

    public MovieShows toMovieShows(List<Show> shows) {
        Show first = shows.getFirst();

        List<LanguageFormatShows> langFormatShows = shows.stream()
                .collect(Collectors.groupingBy(
                        show -> show.getMlf().getLanguage().getName() + "-" + show.getMlf().getFormat().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()))
                .values().stream()
                .map(this::toLanguageFormatShows)
                .toList();

        return new MovieShows()
                .movie(movieMapper.toDto(first.getMlf().getMovie()))
                .languageFormats(langFormatShows);
    }

    private LanguageFormatShows toLanguageFormatShows(List<Show> shows) {
        Show first = shows.get(0);
        return new LanguageFormatShows()
                .language(first.getMlf().getLanguage().getName())
                .format(first.getMlf().getFormat().getName())
                .shows(shows.stream().map(this::toTheatreShowTime).toList());
    }

    private TheatreShowTime toTheatreShowTime(Show show) {
        return new TheatreShowTime()
                .showId(show.getId())
                .startTime(show.getStartTime().toLocalTime().format(ShowTimeMapper.TIME_FORMAT))
                .screen(show.getScreen().getName())
                .price(show.getPrice());
    }
}
