package com.bookmyshow.inventory.common.controller;

import com.bookmyshow.inventory.api.FormatsApi;
import com.bookmyshow.inventory.common.service.IFormatService;
import com.bookmyshow.inventory.model.Format;
import com.bookmyshow.inventory.model.FormatCreateRequest;
import com.bookmyshow.inventory.model.FormatListResponse;
import com.bookmyshow.inventory.model.FormatUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FormatController implements FormatsApi {

    private final IFormatService formatService;

    @Override
    public ResponseEntity<FormatListResponse> getFormats() {
        return ResponseEntity.ok(formatService.findAllFormats());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Format> createFormat(FormatCreateRequest formatCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(formatService.createFormat(formatCreateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Format> updateFormat(Integer id, FormatUpdateRequest formatUpdateRequest) {
        return ResponseEntity.ok(formatService.updateFormat(id, formatUpdateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFormat(Integer id) {
        formatService.deleteFormat(id);
        return ResponseEntity.noContent().build();
    }
}
