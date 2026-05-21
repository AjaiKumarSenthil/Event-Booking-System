package com.bookmyshow.inventory.common.service;

import com.bookmyshow.inventory.model.Language;
import com.bookmyshow.inventory.model.LanguageCreateRequest;
import com.bookmyshow.inventory.model.LanguageListResponse;
import com.bookmyshow.inventory.model.LanguageUpdateRequest;

public interface ILanguageService {

    LanguageListResponse findAllLanguages();

    Language createLanguage(LanguageCreateRequest request);

    Language updateLanguage(Integer id, LanguageUpdateRequest request);

    void deleteLanguage(Integer id);
}
