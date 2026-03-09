package com.ra.base_spring_boot.services.impl;

import com.ra.base_spring_boot.dto.req.StaticContentRequest;
import com.ra.base_spring_boot.dto.resp.StaticContentResponse;
import com.ra.base_spring_boot.model.entity.StaticContent;
import com.ra.base_spring_boot.repository.StaticContentRepository;
import com.ra.base_spring_boot.services.StaticContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaticContentServiceImpl implements StaticContentService {

    private final StaticContentRepository repository;

    @Override
    public List<StaticContentResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public StaticContentResponse getById(Long id) {
        StaticContent content = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Static content not found"));
        return mapToResponse(content);
    }

    @Override
    public StaticContentResponse getByCode(String code) {
        StaticContent content = repository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Static content not found"));
        return mapToResponse(content);
    }

    @Override
    public StaticContentResponse create(StaticContentRequest request, Long userId) {

        if (repository.existsByCode(request.getCode())) {
            throw new RuntimeException("Code đã tồn tại");
        }

        StaticContent entity = StaticContent.builder()
                .code(request.getCode())
                .title(request.getTitle())
                .content(request.getContent())
                .contentType(request.getContentType())
                .status(request.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        StaticContent saved = repository.save(entity);

        return mapToResponse(saved);
    }

    @Override
    public StaticContentResponse update(Long id, StaticContentRequest request, Long adminId) {
        StaticContent content = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Static content not found"));

        content.setTitle(request.getTitle());
        content.setContent(request.getContent());
        content.setContentType(request.getContentType());
        content.setStatus(request.getStatus());
        content.setUpdatedAt(LocalDateTime.now());
        content.setUpdatedBy(adminId);

        repository.save(content);
        return mapToResponse(content);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Static content not found");
        }
        repository.deleteById(id);
    }

    private StaticContentResponse mapToResponse(StaticContent entity) {
        return StaticContentResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .title(entity.getTitle())
                .content(entity.getContent())
                .contentType(entity.getContentType())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
