package com.bookmyshow.inventory.theatre.service.impl;

import com.bookmyshow.inventory.theatre.entity.Seat;
import com.bookmyshow.inventory.theatre.repository.SeatRepository;
import com.bookmyshow.inventory.theatre.service.ISeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements ISeatService {

    private final SeatRepository seatRepository;

    @Override
    public List<Seat> getSeatByScreenId(UUID screenId) {
        return seatRepository.findAllByScreenId(screenId);
    }
}
