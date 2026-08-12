package com.anazcom.labingddd.shared.domain;

public class DomainStateException extends RuntimeException {

    public DomainStateException(String message) {
        super(message);
    }

}
