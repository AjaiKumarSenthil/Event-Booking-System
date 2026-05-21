package com.bookmyshow.inventory.common.service.impl;

import com.bookmyshow.inventory.common.entity.Language;
import com.bookmyshow.inventory.common.mapper.LanguageMapper;
import com.bookmyshow.inventory.common.repository.LanguageRepository;
import com.bookmyshow.inventory.common.service.ILanguageService;
import com.bookmyshow.inventory.exception.ConflictException;
import com.bookmyshow.inventory.exception.ResourceNotFoundException;
import com.bookmyshow.inventory.model.LanguageCreateRequest;
import com.bookmyshow.inventory.model.LanguageListResponse;
import com.bookmyshow.inventory.model.LanguageUpdateRequest;
import com.bookmyshow.inventory.movie.repository.MovieLanguageFormatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements ILanguageService {

    private final LanguageRepository languageRepository;
    private final MovieLanguageFormatRepository movieLanguageFormatRepository;
    private final LanguageMapper languageMapper;

    @Override
    @Transactional(readOnly = true)
    public LanguageListResponse findAllLanguages() {
        return new LanguageListResponse().languages(
                languageRepository.findAll()
                        .stream()
                        .map(languageMapper::toDto)
                        .toList()
        );
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.Language createLanguage(LanguageCreateRequest request) {
        if (languageRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Language already exists with name: " + request.getName());
        }
        if (languageRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new ConflictException("Language already exists with code: " + request.getCode());
        }

        Language entity = languageMapper.toEntity(request);
        Language saved = languageRepository.save(entity);
        return languageMapper.toDto(saved);
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.Language updateLanguage(Integer id, LanguageUpdateRequest request) {
        Language entity = languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found: " + id));

        if (request.getName() != null
                && !request.getName().equalsIgnoreCase(entity.getName())
                && languageRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Language already exists with name: " + request.getName());
        }
        if (request.getCode() != null
                && !request.getCode().equalsIgnoreCase(entity.getCode())
                && languageRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new ConflictException("Language already exists with code: " + request.getCode());
        }

        languageMapper.updateEntity(entity, request);
        Language saved = languageRepository.save(entity);
        return languageMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteLanguage(Integer id) {
        if (!languageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Language not found: " + id);
        }
        if (movieLanguageFormatRepository.existsByLanguage_Id(id)) {
            throw new ConflictException("Language " + id + " still referenced by movies");
        }
        languageRepository.deleteById(id);
    }
}
