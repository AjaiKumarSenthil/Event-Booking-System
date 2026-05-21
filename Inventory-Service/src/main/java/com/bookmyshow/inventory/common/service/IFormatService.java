package com.bookmyshow.inventory.common.service;

import com.bookmyshow.inventory.model.Format;
import com.bookmyshow.inventory.model.FormatCreateRequest;
import com.bookmyshow.inventory.model.FormatListResponse;
import com.bookmyshow.inventory.model.FormatUpdateRequest;

public interface IFormatService {

    FormatListResponse findAllFormats();

    Format createFormat(FormatCreateRequest request);

    Format updateFormat(Integer id, FormatUpdateRequest request);

    void deleteFormat(Integer id);
}
