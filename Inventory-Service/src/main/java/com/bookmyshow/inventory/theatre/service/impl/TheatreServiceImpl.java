package com.bookmyshow.inventory.theatre.service.impl;

import com.bookmyshow.inventory.common.entity.City;
import com.bookmyshow.inventory.common.entity.Format;
import com.bookmyshow.inventory.common.repository.CityRepository;
import com.bookmyshow.inventory.common.repository.FormatRepository;
import com.bookmyshow.inventory.exception.ConflictException;
import com.bookmyshow.inventory.exception.ResourceNotFoundException;
import com.bookmyshow.inventory.model.ScreenCreateRequest;
import com.bookmyshow.inventory.model.ScreenUpdateRequest;
import com.bookmyshow.inventory.model.SeatBulkCreateRequest;
import com.bookmyshow.inventory.model.SeatListResponse;
import com.bookmyshow.inventory.model.SeatRowSpec;
import com.bookmyshow.inventory.model.SeatSpec;
import com.bookmyshow.inventory.model.TheatreCreateRequest;
import com.bookmyshow.inventory.model.TheatreListResponse;
import com.bookmyshow.inventory.model.TheatreUpdateRequest;
import com.bookmyshow.inventory.show.repository.ShowRepository;
import com.bookmyshow.inventory.theatre.entity.Screen;
import com.bookmyshow.inventory.theatre.entity.Seat;
import com.bookmyshow.inventory.theatre.entity.Theatre;
import com.bookmyshow.inventory.theatre.mapper.ScreenMapper;
import com.bookmyshow.inventory.theatre.mapper.SeatMapper;
import com.bookmyshow.inventory.theatre.mapper.TheatreMapper;
import com.bookmyshow.inventory.theatre.repository.ScreenRepository;
import com.bookmyshow.inventory.theatre.repository.SeatRepository;
import com.bookmyshow.inventory.theatre.repository.TheatreRepository;
import com.bookmyshow.inventory.theatre.service.ITheatreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TheatreServiceImpl implements ITheatreService {

    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final CityRepository cityRepository;
    private final FormatRepository formatRepository;
    private final ShowRepository showRepository;
    private final TheatreMapper theatreMapper;
    private final ScreenMapper screenMapper;
    private final SeatMapper seatMapper;

    @Override
    @Transactional(readOnly = true)
    public TheatreListResponse getTheatresByCity(Integer cityId) {
        List<Theatre> theatreList = theatreRepository.findByCityId(cityId);

        return new TheatreListResponse()
                .theatres(theatreList.stream()
                        .map(theatreMapper::toDto)
                        .toList());
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.Theatre createTheatre(TheatreCreateRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found: " + request.getCityId()));

        Theatre theatre = new Theatre();
        theatre.setName(request.getName());
        theatre.setAddress(request.getAddress());
        theatre.setCity(city);
        theatre.setLatitude(request.getLatitude());
        theatre.setLongitude(request.getLongitude());
        theatre.setOwnerId(request.getOwnerId());

        Theatre saved = theatreRepository.save(theatre);
        // Re-read with city to make sure mapping populates derived fields.
        return theatreMapper.toDto(
                theatreRepository.findByIdWithCity(saved.getId()).orElse(saved));
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.Theatre updateTheatre(UUID theatreId, TheatreUpdateRequest request) {
        Theatre theatre = theatreRepository.findByIdWithCity(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found: " + theatreId));

        if (request.getName() != null) {
            theatre.setName(request.getName());
        }
        if (request.getAddress() != null) {
            theatre.setAddress(request.getAddress());
        }
        if (request.getCityId() != null
                && (theatre.getCity() == null || !request.getCityId().equals(theatre.getCity().getId()))) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City not found: " + request.getCityId()));
            theatre.setCity(city);
        }
        if (request.getLatitude() != null) {
            theatre.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            theatre.setLongitude(request.getLongitude());
        }
        if (request.getOwnerId() != null) {
            theatre.setOwnerId(request.getOwnerId());
        }

        Theatre saved = theatreRepository.save(theatre);
        return theatreMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteTheatre(UUID theatreId) {
        if (!theatreRepository.existsById(theatreId)) {
            throw new ResourceNotFoundException("Theatre not found: " + theatreId);
        }
        if (screenRepository.existsByTheatre_Id(theatreId)) {
            throw new ConflictException("Theatre " + theatreId + " still has screens");
        }
        theatreRepository.deleteById(theatreId);
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.Screen createScreen(UUID theatreId, ScreenCreateRequest request) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found: " + theatreId));

        Format format = formatRepository.findById(request.getFormatId())
                .orElseThrow(() -> new ResourceNotFoundException("Format not found: " + request.getFormatId()));

        Screen screen = new Screen();
        screen.setName(request.getName());
        screen.setTheatre(theatre);
        screen.setFormat(format);
        screen.setTotalSeats(request.getTotalSeats());

        Screen saved = screenRepository.save(screen);
        return screenMapper.toDto(saved);
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.Screen updateScreen(UUID theatreId, UUID screenId,
                                                            ScreenUpdateRequest request) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + screenId));

        if (screen.getTheatre() == null || !theatreId.equals(screen.getTheatre().getId())) {
            throw new ResourceNotFoundException(
                    "Screen " + screenId + " does not belong to theatre " + theatreId);
        }

        if (request.getName() != null) {
            screen.setName(request.getName());
        }
        if (request.getFormatId() != null
                && (screen.getFormat() == null || !request.getFormatId().equals(screen.getFormat().getId()))) {
            Format format = formatRepository.findById(request.getFormatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Format not found: " + request.getFormatId()));
            screen.setFormat(format);
        }
        if (request.getTotalSeats() != null) {
            screen.setTotalSeats(request.getTotalSeats());
        }

        Screen saved = screenRepository.save(screen);
        return screenMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteScreen(UUID theatreId, UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + screenId));

        if (screen.getTheatre() == null || !theatreId.equals(screen.getTheatre().getId())) {
            throw new ResourceNotFoundException(
                    "Screen " + screenId + " does not belong to theatre " + theatreId);
        }

        if (showRepository.existsByScreen_Id(screenId)) {
            throw new ConflictException("Screen " + screenId + " still has shows scheduled");
        }

        if (seatRepository.existsByScreen_Id(screenId)) {
            seatRepository.deleteAllByScreenId(screenId);
        }
        screenRepository.deleteById(screenId);
    }

    @Override
    @Transactional
    public SeatListResponse bulkCreateSeats(UUID theatreId, UUID screenId, SeatBulkCreateRequest request) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + screenId));

        if (screen.getTheatre() == null || !theatreId.equals(screen.getTheatre().getId())) {
            throw new ResourceNotFoundException(
                    "Screen " + screenId + " does not belong to theatre " + theatreId);
        }

        boolean hasRows = request.getRows() != null && !request.getRows().isEmpty();
        boolean hasSeats = request.getSeats() != null && !request.getSeats().isEmpty();
        if (hasRows == hasSeats) {
            throw new IllegalArgumentException(
                    "Must provide exactly one of 'rows' or 'seats' (not both empty, not both non-empty)");
        }

        if (showRepository.existsByScreen_Id(screenId)) {
            throw new ConflictException("Screen " + screenId + " already has shows scheduled");
        }

        Set<String> existingKeys = new HashSet<>();
        for (Seat existing : seatRepository.findAllByScreenId(screenId)) {
            existingKeys.add(seatKey(existing.getRowLabel(), existing.getSeatNumber()));
        }

        List<Seat> toCreate = new ArrayList<>();
        Set<String> newKeys = new HashSet<>();

        if (hasRows) {
            for (SeatRowSpec rowSpec : request.getRows()) {
                String rowLabel = rowSpec.getRowLabel();
                int count = rowSpec.getSeatCount();
                for (int i = 1; i <= count; i++) {
                    addSeat(toCreate, newKeys, existingKeys, screen, rowLabel, i);
                }
            }
        } else {
            for (SeatSpec seatSpec : request.getSeats()) {
                addSeat(toCreate, newKeys, existingKeys, screen,
                        seatSpec.getRowLabel(), seatSpec.getSeatNumber());
            }
        }

        List<Seat> saved = new ArrayList<>();
        seatRepository.saveAll(toCreate).forEach(saved::add);

        return new SeatListResponse().seats(saved.stream().map(seatMapper::toDto).toList());
    }

    @Override
    @Transactional
    public void deleteScreenSeats(UUID theatreId, UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found: " + screenId));

        if (screen.getTheatre() == null || !theatreId.equals(screen.getTheatre().getId())) {
            throw new ResourceNotFoundException(
                    "Screen " + screenId + " does not belong to theatre " + theatreId);
        }

        if (showRepository.existsByScreen_Id(screenId)) {
            throw new ConflictException("Screen " + screenId + " still has shows scheduled");
        }

        seatRepository.deleteAllByScreenId(screenId);
    }

    private void addSeat(List<Seat> toCreate, Set<String> newKeys, Set<String> existingKeys,
                         Screen screen, String rowLabel, int seatNumber) {
        String key = seatKey(rowLabel, seatNumber);
        if (existingKeys.contains(key)) {
            throw new ConflictException("Seat " + key + " already exists for screen " + screen.getId());
        }
        if (!newKeys.add(key)) {
            throw new ConflictException("Duplicate seat in request: " + key);
        }
        Seat seat = new Seat();
        seat.setScreen(screen);
        seat.setRowLabel(rowLabel);
        seat.setSeatNumber(seatNumber);
        toCreate.add(seat);
    }

    private String seatKey(String rowLabel, Integer seatNumber) {
        return rowLabel + "-" + seatNumber;
    }
}
