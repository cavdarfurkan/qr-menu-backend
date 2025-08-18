package com.furkancavdar.qrmenu.menu_module.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.response.BuildMenuResponseDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.BuildMenuResultDto;

public class BuildMenuResponseMapper {

    private BuildMenuResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static BuildMenuResponseDto fromBuildMenuResultDto(BuildMenuResultDto buildMenuResultDto) {
        return new BuildMenuResponseDto(buildMenuResultDto.getStatusUrl());
    }
}
