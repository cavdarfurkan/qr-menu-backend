package com.furkancavdar.qrmenu.menu_module.adapter.api.controller;

import com.furkancavdar.qrmenu.common.ApiResponse;
import com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.UpdateJobStatusRequestDto;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.response.MenuJobStatusResponse;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuJobUseCase;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/menu/job")
@RequiredArgsConstructor
// TODO: Implement CORS to same domain only or Api Key mechanism, this controller is internal only
public class MenuJobControllerV1 {

  private final MenuJobUseCase menuJobUseCase;

  @GetMapping("/{jobId}")
  public ResponseEntity<ApiResponse<MenuJobStatusResponse>> jobStatus(
      @Valid @NotBlank @PathVariable("jobId") String jobId) {
    try {
      MenuJobStatus menuJobStatus = menuJobUseCase.getJobStatus(jobId);
      return ResponseEntity.ok(ApiResponse.success(new MenuJobStatusResponse(menuJobStatus)));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
    }
  }

  @PostMapping(
      value = "/{jobId}",
      consumes = {"application/json"})
  public ResponseEntity<ApiResponse<String>> updateJobStatus(
      @Valid @NotBlank @PathVariable("jobId") String jobId,
      @Valid @RequestBody UpdateJobStatusRequestDto requestDto) {
    try {
      Boolean res = menuJobUseCase.updateJobStatus(jobId, requestDto.getStatus());

      if (res == Boolean.FALSE) {
        return ResponseEntity.internalServerError().body(ApiResponse.error("Error"));
      }
    } catch (IllegalArgumentException e) {
      String errorMessage = requestDto.getStatus().name() + " is not valid a status";
      return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
    }

    return ResponseEntity.ok(
        ApiResponse.success("Job status updated to " + requestDto.getStatus()));
  }
}
