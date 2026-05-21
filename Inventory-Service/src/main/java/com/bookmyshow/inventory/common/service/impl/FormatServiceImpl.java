package com.bookmyshow.inventory.common.service.impl;

import com.bookmyshow.inventory.common.entity.Format;
import com.bookmyshow.inventory.common.mapper.FormatMapper;
import com.bookmyshow.inventory.common.repository.FormatRepository;
import com.bookmyshow.inventory.common.service.IFormatService;
import com.bookmyshow.inventory.exception.ConflictException;
import com.bookmyshow.inventory.exception.ResourceNotFoundException;
import com.bookmyshow.inventory.model.FormatCreateRequest;
import com.bookmyshow.inventory.model.FormatListResponse;
import com.bookmyshow.inventory.model.FormatUpdateRequest;
import com.bookmyshow.inventory.movie.repository.MovieLanguageFormatRepository;
import com.bookmyshow.inventory.theatre.repository.ScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormatServiceImpl implements IFormatService {

    private final FormatRepository formatRepository;
    private final MovieLanguageFormatRepository movieLanguageFormatRepository;
    private final ScreenRepository screenRepository;
    private final FormatMapper formatMapper;

    @Override
    @Transactional(readOnly = true)
    public FormatListResponse findAllFormats() {
        return new FormatListResponse().formats(
                formatRepository.findAll()
                        .stream()
                        .map(formatMapper::toDto)
                        .toList()
        );
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.Format createFormat(FormatCreateRequest request) {
        if (formatRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Format already exists with name: " + request.getName());
        }

        Format entity = formatMapper.toEntity(request);
        Format saved = formatRepository.save(entity);
        return formatMapper.toDto(saved);
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.Format updateFormat(Integer id, FormatUpdateRequest request) {
        Format entity = formatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Format not found: " + id));

        if (request.getName() != null
                && !request.getName().equalsIgnoreCase(entity.getName())
                && formatRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Format already exists with name: " + request.getName());
        }

        formatMapper.updateEntity(entity, request);
        Format saved = formatRepository.save(entity);
        return formatMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteFormat(Integer id) {
        if (!formatRepository.existsById(id)) {
            throw new ResourceNotFoundException("Format not found: " + id);
        }
        if (movieLanguageFormatRepository.existsByFormat_Id(id)
                || screenRepository.existsByFormat_Id(id)) {
            throw new ConflictException("Format " + id + " still referenced by movies or screens");
        }
        formatRepository.deleteById(id);
    }
}
