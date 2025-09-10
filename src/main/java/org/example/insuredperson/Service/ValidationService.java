package org.example.insuredperson.Service;

import org.example.insuredperson.Exception.CustomExceptions;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    void validateUserId(String userId) {
        // At least 8 chars, one upper, one lower, one digit, one special char
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@!$*_])[A-Za-z\\d@!$*_]{8,}$";
        if (userId == null || !userId.matches(regex)) {
            throw new CustomExceptions.ValidationException(
                    "UserId must be at least 8 characters long and contain " +
                            "one uppercase, one lowercase, one digit, and one special character (@!$*_)");
        }
    }

    void validatePassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@!$*_])[A-Za-z\\d@!$*_]{8,}$";
        if (password == null || !password.matches(regex)) {
            throw new CustomExceptions.ValidationException(
                    "Password must be at least 8 characters long and contain " +
                            "one uppercase, one lowercase, one digit, and one special character (@!$*_)");
        }
    }

    void validatePolicyNumber(String policyNumber) {
        if (policyNumber == null || !policyNumber.startsWith("PA")) {
            throw new CustomExceptions.ValidationException(
                    "Policy Number must start with 'PA'");
        }
    }

    void validateEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if(email == null || !email.matches(regex)) {
            throw new CustomExceptions.ValidationException(
                    "Email id format is invalid");
        }
    }

}