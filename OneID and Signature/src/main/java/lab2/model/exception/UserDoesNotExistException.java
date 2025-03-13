package lab2.model.exception;

import javax.naming.AuthenticationException;

public class UserDoesNotExistException extends AuthenticationException {
    public UserDoesNotExistException(){
        super("User Does Not Exist Exception");
    }
}
