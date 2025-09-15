package com.railway.management.nfc.controller;

import com.railway.management.nfc.dto.NfcEntryRequest;
import com.railway.management.nfc.service.NfcEntryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "NFC统一录入接口")
@RestController
@RequestMapping("/api/nfc-entry")
@RequiredArgsConstructor
public class NfcEntryController {

    private final NfcEntryService nfcEntryService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> handleNfcEntry(@Validated @RequestBody NfcEntryRequest request) {
        Object result = nfcEntryService.processNfcEntry(request);
        return ResponseEntity.ok(result);
    }
}