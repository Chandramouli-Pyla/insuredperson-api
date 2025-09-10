package org.example.insuredperson.DTO;

public class ForgotPasswordRequest {
    private String userId;
    private String email;

    // getters and setters
    public String getUserId() { return userId; }
    public void setUsername(String username) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
