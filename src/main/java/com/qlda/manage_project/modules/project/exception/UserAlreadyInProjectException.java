package com.qlda.manage_project.modules.project.exception;

public class UserAlreadyInProjectException extends RuntimeException{
    public UserAlreadyInProjectException(String message) {
        super(message);
    }
}
