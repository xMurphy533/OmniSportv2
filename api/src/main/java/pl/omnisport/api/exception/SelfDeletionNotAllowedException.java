package pl.omnisport.api.exception;

public class SelfDeletionNotAllowedException extends RuntimeException{
    public SelfDeletionNotAllowedException(String message){
        super(message);
    }
}
