package com.furkancavdar.qrmenu.menu_module.adapter.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.common.ApiResponse;
import com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.UpsertMenuContentRequestDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuContentUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/menu/{menuId}/content")
@RequiredArgsConstructor
public class MenuContentControllerV1 {

    private final MenuContentUseCase menuContentUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> upsert(
            @PathVariable Long menuId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpsertMenuContentRequestDto requestDto
    ) {
        try {
            menuContentUseCase.validateAndSave(userDetails.getUsername(), menuId, requestDto.getCollection(), requestDto.getContent());
            log.info("MenuContentControllerV1:upsert menu content is saved/updated");

            return ResponseEntity.ok(ApiResponse.success("Menu content saved/updated successfully"));
        } catch (ResourceNotFoundException e) {
            log.error("MenuContentControllerV1:upsert resource not found: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (AccessDeniedException e) {
            log.error("MenuContentControllerV1:upsert access denied: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("MenuContentControllerV1:upsert error {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{collection}")
    public ResponseEntity<ApiResponse<List<JsonNode>>> getCollectionContent(
            @PathVariable Long menuId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @PathVariable @NotBlank String collection
    ) {
        try {
            List<JsonNode> content = menuContentUseCase.getCollection(userDetails.getUsername(), menuId, collection);
            return ResponseEntity.ok(ApiResponse.success(content));
        } catch (ResourceNotFoundException e) {
            log.error("MenuContentControllerV1:getCollectionContent resource not found: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (AccessDeniedException e) {
            log.error("MenuContentControllerV1:getCollectionContent access denied: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("MenuContentControllerV1:getCollectionContent error {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
