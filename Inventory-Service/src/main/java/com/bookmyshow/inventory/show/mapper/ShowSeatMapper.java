package com.bookmyshow.inventory.show.mapper;

import com.bookmyshow.inventory.common.enums.SeatStatus;
import com.bookmyshow.inventory.model.Seat;
import com.bookmyshow.inventory.model.SeatRow;
import com.bookmyshow.inventory.model.ShowSeats;
import com.bookmyshow.inventory.show.entity.Show;
import com.bookmyshow.inventory.show.entity.ShowSeat;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    public SeatRow toSeatRow(String rowLabel, List<ShowSeat> showSeats, Set<UUID> heldShowSeatIds) {
        Set<UUID> held = heldShowSeatIds != null ? heldShowSeatIds : Collections.emptySet();
        return new SeatRow()
                .rowLabel(rowLabel)
                .seats(showSeats.stream().map(ss -> toSeatDto(ss, held)).toList());
    }

    /**
     * The persisted {@code AVAILABLE} status is overridden to {@code BLOCKED}
     * for any seat currently held in Redis. The wire contract stays unchanged
     * even though Postgres no longer stores the transient {@code BLOCKED} state.
     */
    private Seat toSeatDto(ShowSeat showSeat, Set<UUID> heldShowSeatIds) {
        SeatStatus persisted = showSeat.getStatus();
        SeatStatus effective = persisted == SeatStatus.AVAILABLE && heldShowSeatIds.contains(showSeat.getId())
                ? SeatStatus.BLOCKED
                : persisted;
        return new Seat()
                .id(showSeat.getSeat().getId())
                .seatNumber(showSeat.getSeat().getSeatNumber())
                .status(Seat.StatusEnum.valueOf(effective.name()));
    }
}
