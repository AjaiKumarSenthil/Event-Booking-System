package com.bookmyshow.inventory.theatre.service;

import com.bookmyshow.inventory.model.Screen;
import com.bookmyshow.inventory.model.ScreenCreateRequest;
import com.bookmyshow.inventory.model.ScreenUpdateRequest;
import com.bookmyshow.inventory.model.SeatBulkCreateRequest;
import com.bookmyshow.inventory.model.SeatListResponse;
import com.bookmyshow.inventory.model.Theatre;
import com.bookmyshow.inventory.model.TheatreCreateRequest;
import com.bookmyshow.inventory.model.TheatreListResponse;
import com.bookmyshow.inventory.model.TheatreUpdateRequest;

import java.util.UUID;

public interface ITheatreService {

    TheatreListResponse getTheatresByCity(Integer cityId);

    Theatre createTheatre(TheatreCreateRequest request);

    Theatre updateTheatre(UUID theatreId, TheatreUpdateRequest request);

    void deleteTheatre(UUID theatreId);

    Screen createScreen(UUID theatreId, ScreenCreateRequest request);

    Screen updateScreen(UUID theatreId, UUID screenId, ScreenUpdateRequest request);

    void deleteScreen(UUID theatreId, UUID screenId);

    SeatListResponse bulkCreateSeats(UUID theatreId, UUID screenId, SeatBulkCreateRequest request);

    void deleteScreenSeats(UUID theatreId, UUID screenId);
}
