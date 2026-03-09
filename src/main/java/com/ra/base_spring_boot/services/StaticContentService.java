package com.ra.base_spring_boot.services;

import com.ra.base_spring_boot.dto.req.StaticContentRequest;
import com.ra.base_spring_boot.dto.resp.StaticContentResponse;

import java.util.List;

public interface StaticContentService {

    List<StaticContentResponse> getAll();

    StaticContentResponse getById(Long id);

    StaticContentResponse getByCode(String code);

    StaticContentResponse create(StaticContentRequest request, Long adminId);

    StaticContentResponse update(Long id, StaticContentRequest request, Long adminId);

    void delete(Long id);
}
