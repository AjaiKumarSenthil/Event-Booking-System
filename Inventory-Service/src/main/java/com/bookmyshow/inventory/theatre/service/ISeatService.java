package com.bookmyshow.inventory.theatre.service;

import com.bookmyshow.inventory.theatre.entity.Seat;

import java.util.List;
import java.util.UUID;

public interface ISeatService {

    List<Seat> getSeatByScreenId(UUID screenId);
}
