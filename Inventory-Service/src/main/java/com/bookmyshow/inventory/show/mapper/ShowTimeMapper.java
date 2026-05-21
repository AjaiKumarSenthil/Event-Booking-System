package com.bookmyshow.inventory.show.mapper;

import com.bookmyshow.inventory.model.ShowTime;
import com.bookmyshow.inventory.show.entity.Show;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ShowTimeMapper {

    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    public ShowTime toDto(Show show) {
        return new ShowTime()
                .showId(show.getId())
                .startTime(show.getStartTime().toLocalTime().format(TIME_FORMAT))
                .language(show.getMlf().getLanguage().getName())
                .format(show.getMlf().getFormat().getName())
                .price(show.getPrice());
    }
}
