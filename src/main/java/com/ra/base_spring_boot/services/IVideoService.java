package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.VideoRequestDTO;
import com.ra.base_spring_boot.dto.req.VideoSearchDTO;
import com.ra.base_spring_boot.dto.resp.VideoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IVideoService {
    Page<VideoResponseDTO> findAll(VideoSearchDTO searchDTO, Pageable pageable);
    VideoResponseDTO findById(Long id);
    VideoResponseDTO create(VideoRequestDTO videoRequestDTO);
    VideoResponseDTO update(Long id, VideoRequestDTO videoRequestDTO);
    void delete(Long id);

    /**
     * Public search for published videos (approved only).
     */
    Page<VideoResponseDTO> searchPublished(VideoSearchDTO searchDTO, Pageable pageable);
}

