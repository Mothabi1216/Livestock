# Livestock

Android livestock management application with farmer, veterinarian, and administrator workflows.

## Local setup

1. Open the project in Android Studio and let Gradle sync.
2. Copy `email-secrets.properties.example` to `email-secrets.properties` in the project root.
3. Set `SMTP_EMAIL` and `SMTP_PASSWORD` to your sender account and Gmail app password if email sending is needed.
4. Build and run the `app` module. The project uses Android SDK 36 and requires Android 7.0 (API 24) or newer.

Local SDK paths, email credentials, signing keys, IDE state, and generated build output are excluded from Git. Without email settings the project can build, but email delivery requires valid credentials.

These settings keep credentials out of the repository; credentials used by the app are still packaged in its APK. A production deployment should send email through a backend service.
