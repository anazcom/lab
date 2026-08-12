package com.anazcom.labingddd.shared.domain;

public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }

}
