package com.example.livestock;

import java.util.List;

public class EmailHelper {

    private static String getHtmlTemplate(String title, String content) {
        return "<html><body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);'>" +
                "<div style='background: #2c3e50; color: white; padding: 20px; text-align: center;'>" +
                "<h1 style='margin: 0;'>" + title + "</h1>" +
                "</div>" +
                "<div style='padding: 30px; color: #333; line-height: 1.6;'>" +
                content +
                "</div>" +
                "<div style='background: #f4f4f4; color: #777; padding: 15px; text-align: center; font-size: 12px;'>" +
                "&copy; 2024 Livestock Management System. All rights reserved." +
                "</div>" +
                "</div></body></html>";
    }

    public static void sendWelcomeEmail(String toEmail, String name, String role) {
        String content = "<h2>Welcome to Livestock App!</h2>" +
                "<p>Hello <b>" + name + "</b>,</p>" +
                "<p>Your account has been successfully registered on the Livestock Management System.</p>" +
                "<p><b>Role:</b> " + role + "<br>" +
                "<b>Email:</b> " + toEmail + "</p>" +
                "<p>You can now log in and start using our services. We're glad to have you with us!</p>";
        EmailSender.sendHtmlEmail(toEmail, "Welcome to Livestock App", getHtmlTemplate("Registration Success", content));
    }

    public static void sendOutbreakAlert(String title, String message, List<String> recipients) {
        String content = "<h2>OUTBREAK ALERT!</h2>" +
                "<p style='color: #e74c3c; font-weight: bold;'>" + title + "</p>" +
                "<p>" + message + "</p>" +
                "<p>Please stay vigilant and report any suspicious cases immediately through the app.</p>";
        String html = getHtmlTemplate("Disease Outbreak Warning", content);
        for (String email : recipients) {
            EmailSender.sendHtmlEmail(email, "URGENT: " + title, html);
        }
    }

    public static void sendAnnouncement(String title, String message, List<String> recipients) {
        String content = "<h2>Important Announcement</h2>" +
                "<p>" + message + "</p>";
        String html = getHtmlTemplate("System Announcement", content);
        for (String email : recipients) {
            EmailSender.sendHtmlEmail(email, title, html);
        }
    }

    public static void sendForgotPasswordEmail(String toEmail, String code) {
        String content = "<h2>Password Reset Request</h2>" +
                "<p>You requested to reset your password. Use the verification code below to proceed:</p>" +
                "<div style='background: #ecf0f1; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #2c3e50; border-radius: 5px; margin: 20px 0;'>" +
                code +
                "</div>" +
                "<p>If you did not request this, please ignore this email.</p>";
        EmailSender.sendHtmlEmail(toEmail, "Password Reset Verification Code", getHtmlTemplate("Security Verification", content));
    }
}
