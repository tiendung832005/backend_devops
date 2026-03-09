package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.BannedKeywordRequestDTO;
import com.ra.base_spring_boot.dto.resp.BannedKeywordResponseDTO;
import com.ra.base_spring_boot.services.IBannedKeywordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/banned-keywords")
@RequiredArgsConstructor
public class BannedKeywordController {
    
    private final IBannedKeywordService bannedKeywordService;
    
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<BannedKeywordResponseDTO>>> getAllBannedKeywords() {
        List<BannedKeywordResponseDTO> keywords = bannedKeywordService.getAllBannedKeywords();
        return ResponseEntity.ok(ResponseWrapper.<List<BannedKeywordResponseDTO>>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(keywords)
                .build());
    }
    
    @PostMapping
    public ResponseEntity<ResponseWrapper<BannedKeywordResponseDTO>> createBannedKeyword(
            @Valid @RequestBody BannedKeywordRequestDTO dto) {
        BannedKeywordResponseDTO created = bannedKeywordService.createBannedKeyword(dto);
        return ResponseEntity.created(URI.create("/api/v1/admin/banned-keywords/" + created.getId()))
                .body(ResponseWrapper.<BannedKeywordResponseDTO>builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data(created)
                        .build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<BannedKeywordResponseDTO>> updateBannedKeyword(
            @PathVariable Long id,
            @Valid @RequestBody BannedKeywordRequestDTO dto) {
        BannedKeywordResponseDTO updated = bannedKeywordService.updateBannedKeyword(id, dto);
        return ResponseEntity.ok(ResponseWrapper.<BannedKeywordResponseDTO>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(updated)
                .build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<String>> deleteBannedKeyword(@PathVariable Long id) {
        bannedKeywordService.deleteBannedKeyword(id);
        return ResponseEntity.ok(ResponseWrapper.<String>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data("Banned keyword deleted successfully")
                .build());
    }
}

