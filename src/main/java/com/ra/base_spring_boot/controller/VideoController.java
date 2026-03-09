package com.ra.base_spring_boot.controller;

import com.ra.base_spring_boot.dto.ResponseWrapper;
import com.ra.base_spring_boot.dto.req.VideoRequestDTO;
import com.ra.base_spring_boot.dto.req.VideoSearchDTO;
import com.ra.base_spring_boot.dto.resp.VideoResponseDTO;
import com.ra.base_spring_boot.services.IVideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin/videos")
@RequiredArgsConstructor
public class VideoController {
    private final IVideoService videoService;

    /**
     * @apiNote Get all videos with pagination and search
     */
    @GetMapping
    public ResponseEntity<?> getAllVideos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        VideoSearchDTO searchDTO = new VideoSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setStatus(status);
        searchDTO.setType(type);
        searchDTO.setAuthorId(authorId);
        searchDTO.setCategoryId(categoryId);

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VideoResponseDTO> videos = videoService.findAll(searchDTO, pageable);

        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(videos)
                        .build()
        );
    }

    /**
     * @apiNote Get video by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVideoById(@PathVariable Long id) {
        VideoResponseDTO video = videoService.findById(id);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(video)
                        .build()
        );
    }

    /**
     * @apiNote Create new video
     */
    @PostMapping
    public ResponseEntity<?> createVideo(@Valid @RequestBody VideoRequestDTO videoRequestDTO) {
        VideoResponseDTO video = videoService.create(videoRequestDTO);
        return ResponseEntity.created(URI.create("/api/v1/admin/videos/" + video.getId())).body(
                ResponseWrapper.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data(video)
                        .build()
        );
    }

    /**
     * @apiNote Update video
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVideo(@PathVariable Long id, @Valid @RequestBody VideoRequestDTO videoRequestDTO) {
        VideoResponseDTO video = videoService.update(id, videoRequestDTO);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(video)
                        .build()
        );
    }

    /**
     * @apiNote Delete video
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVideo(@PathVariable Long id) {
        videoService.delete(id);
        return ResponseEntity.ok().body(
                ResponseWrapper.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Video deleted successfully")
                        .build()
        );
    }
}

