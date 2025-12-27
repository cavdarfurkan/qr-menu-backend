package com.furkancavdar.qrmenu.menu_module.adapter.api.controller;

import com.furkancavdar.qrmenu.common.ApiResponse;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.mapper.HydratedItemResponseMapper;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.AddMenuContentRequestDto;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.DeleteMenuContentBulkRequestDto;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.UpdateMenuContentRequestDto;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.response.HydratedItemResponseDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuContentUseCase;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.HydratedItemDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/menu/{menuId}/content")
@RequiredArgsConstructor
public class MenuContentControllerV1 {

    private final MenuContentUseCase menuContentUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<HydratedItemResponseDto>> addContent(
            @Valid @PathVariable @NotNull Long menuId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddMenuContentRequestDto requestDto
    ) {
        HydratedItemDto newContent = menuContentUseCase.createContent(
                userDetails.getUsername(),
                menuId,
                requestDto.getCollection(),
                requestDto.getContent(),
                requestDto.getRelations()
        );

        log.info("MenuContentControllerV1:addContent menu content is saved/updated");
        return ResponseEntity.ok(ApiResponse.success(
                "Menu content saved/updated successfully",
                HydratedItemResponseMapper.fromHydratedItemDto(newContent)
        ));
    }

    @PutMapping("/{collection}/{itemId}")
    public ResponseEntity<ApiResponse<HydratedItemResponseDto>> updateContent(
            @Valid @PathVariable @NotNull Long menuId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @PathVariable @NotBlank String collection,
            @Valid @PathVariable @NotBlank String itemId,
            @Valid @RequestBody UpdateMenuContentRequestDto updateMenuContentRequestDto
    ) {
        HydratedItemDto updatedContent = menuContentUseCase.updateContent(
                userDetails.getUsername(),
                menuId,
                collection,
                UUID.fromString(itemId),
                updateMenuContentRequestDto.getNewContent(),
                updateMenuContentRequestDto.getRelations()
        );

        return ResponseEntity.ok(ApiResponse.success(
                HydratedItemResponseMapper.fromHydratedItemDto(updatedContent)
        ));
    }

    @GetMapping("/{collection}")
    public ResponseEntity<ApiResponse<List<HydratedItemResponseDto>>> getCollectionContent(
            @Valid @PathVariable @NotNull Long menuId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @PathVariable @NotBlank String collection
    ) {
        List<HydratedItemDto> content = menuContentUseCase.getCollectionContent(userDetails.getUsername(), menuId,
                collection);
        return ResponseEntity.ok(ApiResponse.success(
                content.stream().map(HydratedItemResponseMapper::fromHydratedItemDto).toList()
        ));
    }

    @GetMapping("/{collection}/{itemId}")
    public ResponseEntity<ApiResponse<HydratedItemResponseDto>> getContent(
            @Valid @PathVariable @NotNull Long menuId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @PathVariable @NotBlank String collection,
            @Valid @PathVariable @NotBlank String itemId
    ) {
        HydratedItemDto content = menuContentUseCase.getContent(userDetails.getUsername(), menuId, collection, UUID.fromString(itemId));
        return ResponseEntity.ok(ApiResponse.success(
                HydratedItemResponseMapper.fromHydratedItemDto(content)
        ));
    }

    @DeleteMapping("/{collection}/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @Valid @PathVariable @NotNull Long menuId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @PathVariable @NotBlank String collection,
            @Valid @PathVariable @NotBlank String itemId
    ) {
        menuContentUseCase.deleteContent(userDetails.getUsername(), menuId, collection, UUID.fromString(itemId));
        log.info("MenuContentControllerV1:deleteContent menu content deleted successfully");
        return ResponseEntity.ok(ApiResponse.success(
                "Menu content deleted successfully"
        ));
    }

    @DeleteMapping("/{collection}")
    public ResponseEntity<ApiResponse<Void>> deleteContentBulk(
            @Valid @PathVariable @NotNull Long menuId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @PathVariable @NotBlank String collection,
            @Valid @RequestBody DeleteMenuContentBulkRequestDto requestDto
    ) {
        List<UUID> uuidList = requestDto.getItemIds().stream().map(UUID::fromString).toList();
        menuContentUseCase.deleteContentBulk(userDetails.getUsername(), menuId, collection, uuidList);
        log.info("MenuContentControllerV1:deleteContentBulk {} menu content items deleted successfully", requestDto.getItemIds().size());
        return ResponseEntity.ok(ApiResponse.success(
                String.format("%d menu content items deleted successfully", requestDto.getItemIds().size())
        ));
    }
}
