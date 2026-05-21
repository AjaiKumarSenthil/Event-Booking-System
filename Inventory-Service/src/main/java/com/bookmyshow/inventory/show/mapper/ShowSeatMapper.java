package com.bookmyshow.inventory.show.mapper;

import com.bookmyshow.inventory.model.Seat;
import com.bookmyshow.inventory.model.SeatRow;
import com.bookmyshow.inventory.model.ShowSeats;
import com.bookmyshow.inventory.show.entity.Show;
import com.bookmyshow.inventory.show.entity.ShowSeat;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShowSeatMapper {

    public ShowSeats toDto(Show show, List<SeatRow> seatRows) {
        return new ShowSeats().showId(show.getId())
                .movieTitle(show.getMlf().getMovie().getTitle())
                .theatreName(show.getScreen().getTheatre().getName())
                .screen(show.getScreen().getName())
                .showTime(show.getStartTime())
                .price(show.getPrice())
                .totalSeats(show.getScreen().getTotalSeats())
                .seatLayout(seatRows);
    }

    public SeatRow toSeatRow(String rowLabel, List<ShowSeat> showSeats) {
        return new SeatRow()
                .rowLabel(rowLabel)
                .seats(showSeats.stream().map(this::toSeatDto).toList());
    }

    private Seat toSeatDto(ShowSeat showSeat) {
        return new Seat()
                .id(showSeat.getSeat().getId())
                .seatNumber(showSeat.getSeat().getSeatNumber())
                .status(Seat.StatusEnum.valueOf(showSeat.getStatus().name()));
    }
}
