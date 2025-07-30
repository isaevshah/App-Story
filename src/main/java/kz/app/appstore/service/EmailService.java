package kz.app.appstore.service;

public interface EmailService {
    void sendOtpEmail(String to, String otp);
}
