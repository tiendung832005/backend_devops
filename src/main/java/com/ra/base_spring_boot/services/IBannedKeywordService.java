package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.BannedKeywordRequestDTO;
import com.ra.base_spring_boot.dto.resp.BannedKeywordResponseDTO;

import java.util.List;

public interface IBannedKeywordService {
    List<BannedKeywordResponseDTO> getAllBannedKeywords();
    BannedKeywordResponseDTO createBannedKeyword(BannedKeywordRequestDTO dto);
    BannedKeywordResponseDTO updateBannedKeyword(Long id, BannedKeywordRequestDTO dto);
    void deleteBannedKeyword(Long id);
    boolean containsBannedKeyword(String content);
}

