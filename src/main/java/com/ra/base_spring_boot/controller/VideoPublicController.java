package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.VideoSearchDTO;
import com.ra.base_spring_boot.dto.resp.VideoResponseDTO;
import com.ra.base_spring_boot.services.IVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoPublicController {

    private final IVideoService videoService;

    /**
     * Public search videos (only APPROVED).
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<Page<VideoResponseDTO>>> searchVideos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        VideoSearchDTO searchDTO = new VideoSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setAuthorId(authorId);
        searchDTO.setCategoryId(categoryId);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VideoResponseDTO> videos = videoService.searchPublished(searchDTO, pageable);

        return ResponseEntity.ok(ResponseWrapper.<Page<VideoResponseDTO>>builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(videos)
                .build());
    }
}


