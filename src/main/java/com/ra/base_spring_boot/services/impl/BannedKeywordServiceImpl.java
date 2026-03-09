package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.BannedKeywordRequestDTO;
import com.ra.base_spring_boot.dto.resp.BannedKeywordResponseDTO;
import com.ra.base_spring_boot.exception.HttpConflict;
import com.ra.base_spring_boot.exception.HttpNotFound;
import com.ra.base_spring_boot.model.entity.BannedKeyword;
import com.ra.base_spring_boot.repository.IBannedKeywordRepository;
import com.ra.base_spring_boot.services.IBannedKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannedKeywordServiceImpl implements IBannedKeywordService {
    
    private final IBannedKeywordRepository bannedKeywordRepository;
    
    @Override
    public List<BannedKeywordResponseDTO> getAllBannedKeywords() {
        return bannedKeywordRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(BannedKeywordResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public BannedKeywordResponseDTO createBannedKeyword(BannedKeywordRequestDTO dto) {
        // Check if keyword already exists (case-insensitive)
        bannedKeywordRepository.findByKeywordIgnoreCase(dto.getKeyword())
                .ifPresent(existing -> {
                    throw new HttpConflict("Banned keyword already exists: " + dto.getKeyword());
                });
        
        BannedKeyword bannedKeyword = new BannedKeyword();
        bannedKeyword.setKeyword(dto.getKeyword().trim());
        bannedKeyword.setDescription(dto.getDescription());
        
        BannedKeyword saved = bannedKeywordRepository.save(bannedKeyword);
        return new BannedKeywordResponseDTO(saved);
    }
    
    @Override
    @Transactional
    public BannedKeywordResponseDTO updateBannedKeyword(Long id, BannedKeywordRequestDTO dto) {
        BannedKeyword bannedKeyword = bannedKeywordRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Banned keyword not found with id: " + id));
        
        // Check if new keyword already exists (case-insensitive) and is not the current one
        if (!bannedKeyword.getKeyword().equalsIgnoreCase(dto.getKeyword().trim())) {
            bannedKeywordRepository.findByKeywordIgnoreCase(dto.getKeyword().trim())
                    .ifPresent(existing -> {
                        throw new HttpConflict("Banned keyword already exists: " + dto.getKeyword());
                    });
        }
        
        bannedKeyword.setKeyword(dto.getKeyword().trim());
        bannedKeyword.setDescription(dto.getDescription());
        bannedKeyword.setUpdatedAt(LocalDateTime.now());
        
        BannedKeyword updated = bannedKeywordRepository.save(bannedKeyword);
        return new BannedKeywordResponseDTO(updated);
    }
    
    @Override
    @Transactional
    public void deleteBannedKeyword(Long id) {
        BannedKeyword bannedKeyword = bannedKeywordRepository.findById(id)
                .orElseThrow(() -> new HttpNotFound("Banned keyword not found with id: " + id));
        bannedKeywordRepository.delete(bannedKeyword);
    }
    
    @Override
    public boolean containsBannedKeyword(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        List<BannedKeyword> allKeywords = bannedKeywordRepository.findAll();
        
        return allKeywords.stream()
                .anyMatch(keyword -> lowerContent.contains(keyword.getKeyword().toLowerCase()));
    }
}

