package com.musala.gorskikh.services.validators;

import lombok.Getter;

@Getter
public class ValidationResult {

    private final boolean isValid;
    private final String errorMsg;

    private ValidationResult(boolean isValid, String errorMsg) {
        this.isValid = isValid;
        this.errorMsg = errorMsg;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult error(String errorMessage) {
        return new ValidationResult(false, errorMessage);
    }
}
