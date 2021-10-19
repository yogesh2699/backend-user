package com.codercms.ImportExcelDemo.Exceptions;

public class UserException extends RuntimeException{
    public UserException(String errorMessage) {
        super(errorMessage);
    }
}
