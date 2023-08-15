package com.musala.gorskikh.services.validators;

public interface Validator<T> {

    ValidationResult validate(T item);
}
