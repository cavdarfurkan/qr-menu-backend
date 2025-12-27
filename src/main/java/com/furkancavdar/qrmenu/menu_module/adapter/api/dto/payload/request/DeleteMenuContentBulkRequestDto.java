package com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeleteMenuContentBulkRequestDto {
    @NotNull(message = "Item IDs list cannot be null")
    @NotEmpty(message = "At least one item id must be provided")
    List<@NotBlank String> itemIds;
}
