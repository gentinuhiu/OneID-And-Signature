package lab2.service;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidationService {
    public boolean isPasswordStrong(String password) {
        String strongPasswordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&.])[A-Za-z\\d@$!%*?&.]{8,}$";
        return password.matches(strongPasswordRegex);
    }
}
