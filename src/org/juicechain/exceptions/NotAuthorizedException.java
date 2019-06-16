package org.juicechain.exceptions;

public class NotAuthorizedException extends Exception {

    public NotAuthorizedException(){}

    public NotAuthorizedException(String message){
        super(message);
    }

}
