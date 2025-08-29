package com.furkancavdar.qrmenu.menu_module.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.response.HydratedItemResponseDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.HydratedItemDto;

public class HydratedItemResponseMapper {

    private HydratedItemResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static HydratedItemResponseDto fromHydratedItemDto(HydratedItemDto hydratedItemDto) {
        return HydratedItemResponseDto.builder()
                .hydratedItem(hydratedItemDto)
                .build();
    }
}
