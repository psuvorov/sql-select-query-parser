package com.newjob.parser.exceptions;

public class InvalidQueryFormatException extends Exception {

    public InvalidQueryFormatException(String message) {
        super(message);
    }

    public InvalidQueryFormatException() {
        super("Wrong query format");
    }
}
