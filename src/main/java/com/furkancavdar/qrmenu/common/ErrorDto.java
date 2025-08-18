package com.furkancavdar.qrmenu.common;

import lombok.Data;

import java.util.List;

@Data
public class ErrorDto {
    private int status;
    private List<String> errors;

    public void addError(String message) {
        this.errors.add(message);
    }
}
