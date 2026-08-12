package com.anazcom.labingddd.shared.domain;

public class DomainRepositoryException extends RuntimeException {

    public DomainRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

}
