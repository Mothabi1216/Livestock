package com.example.livestock;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String EMAIL = BuildConfig.SMTP_EMAIL;
    private static final String PASSWORD = BuildConfig.SMTP_PASSWORD;

    public interface EmailListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static void sendEmail(final String toEmail, final String subject, final String body) {
        new SendEmailTask(toEmail, subject, body, false, null).execute();
    }

    public static void sendHtmlEmail(final String toEmail, final String subject, final String htmlBody) {
        new SendEmailTask(toEmail, subject, htmlBody, true, null).execute();
    }

    public static void sendHtmlEmail(final String toEmail, final String subject, final String htmlBody, EmailListener listener) {
        new SendEmailTask(toEmail, subject, htmlBody, true, listener).execute();
    }

    private static class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private String toEmail, subject, body;
        private boolean isHtml;
        private EmailListener listener;
        private Exception exception;

        SendEmailTask(String toEmail, String subject, String body, boolean isHtml, EmailListener listener) {
            this.toEmail = toEmail;
            this.subject = subject;
            this.body = body;
            this.isHtml = isHtml;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL, PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject(subject);
                
                if (isHtml) {
                    message.setContent(body, "text/html; charset=utf-8");
                } else {
                    message.setText(body);
                }

                Transport.send(message);
                Log.d("EmailSender", "Email sent successfully to: " + toEmail);
                return true;
            } catch (MessagingException e) {
                Log.e("EmailSender", "Failed to send email to: " + toEmail, e);
                this.exception = e;
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (listener != null) {
                if (success) listener.onSuccess();
                else listener.onFailure(exception);
            }
        }
    }
}
