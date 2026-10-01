package com.example.livestock;

import android.content.Context;
import java.util.List;

public class NotificationHelper {

    public static void broadcastAlert(Context context, String title, String message, String disease, String severity, String district, int createdBy) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        // Correcting the call to addGlobalAlert to match the signature in DatabaseHelper:
        // (String title, String message, String disease, String severity, String district, String animal, int reportCount, String source, int createdBy)
        long alertId = dbHelper.addGlobalAlert(title, message, disease, severity, district, "Not Specified", 0, "MANUAL", createdBy);
        
        if (alertId != -1) {
            List<String> emails = dbHelper.getAllUserEmails();
            String subject = "LIVESTOCK ALERT: " + title;
            String body = "URGENT ALERT\n\n" +
                    "Title: " + title + "\n" +
                    "Disease: " + disease + "\n" +
                    "Severity: " + severity + "\n" +
                    "Location: " + district + "\n\n" +
                    "Message: " + message + "\n\n" +
                    "Please check your Livestock App dashboard for more details.";
            
            sendEmailBroadcast(context, emails, subject, body);
        }
    }

    public static void sendEmailBroadcast(Context context, List<String> emails, String subject, String body) {
        for (String email : emails) {
            EmailSender.sendEmail(email, subject, body);
        }
    }
}
