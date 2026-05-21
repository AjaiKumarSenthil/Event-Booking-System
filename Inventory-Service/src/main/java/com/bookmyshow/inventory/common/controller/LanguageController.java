package com.bookmyshow.inventory.common.controller;

import com.bookmyshow.inventory.api.LanguagesApi;
import com.bookmyshow.inventory.common.service.ILanguageService;
import com.bookmyshow.inventory.model.Language;
import com.bookmyshow.inventory.model.LanguageCreateRequest;
import com.bookmyshow.inventory.model.LanguageListResponse;
import com.bookmyshow.inventory.model.LanguageUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LanguageController implements LanguagesApi {

    private final ILanguageService languageService;

    @Override
    public ResponseEntity<LanguageListResponse> getLanguages() {
        return ResponseEntity.ok(languageService.findAllLanguages());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Language> createLanguage(LanguageCreateRequest languageCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(languageService.createLanguage(languageCreateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Language> updateLanguage(Integer id, LanguageUpdateRequest languageUpdateRequest) {
        return ResponseEntity.ok(languageService.updateLanguage(id, languageUpdateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLanguage(Integer id) {
        languageService.deleteLanguage(id);
        return ResponseEntity.noContent().build();
    }
}
