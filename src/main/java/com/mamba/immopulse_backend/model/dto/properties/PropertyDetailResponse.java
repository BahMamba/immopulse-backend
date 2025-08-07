package com.mamba.immopulse_backend.model.dto.properties;

import java.util.List;

public record PropertyDetailResponse(
    Long id,
    String title,
    String description,
    String address,
    Double price,
    String type,
    String status,
    String coverImageUrl,
    String ownerFullname,
    String ownerEmail,
    List<String> images
) {}
