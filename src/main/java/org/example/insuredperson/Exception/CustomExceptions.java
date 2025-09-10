package org.example.insuredperson.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class CustomExceptions {

    private CustomExceptions(){

    }

    //raises exception if policy number is same
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicatePolicyException extends RuntimeException{
        public DuplicatePolicyException(String message){
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateUserIdException extends RuntimeException{
        public DuplicateUserIdException(String message){
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

     //already existing exceptions here...
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class UnauthorizedException extends RuntimeException {
            public UnauthorizedException(String message) {
                super(message);
            }
    }

}
