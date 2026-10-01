package com.example.livestock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "livestock.db";
    private static final int DATABASE_VERSION = 14; // Incremented for updates

    public static final String TABLE_ROLES = "Roles";
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_AUDIT_LOGS = "Audit_Logs";
    public static final String TABLE_FARMERS_REPORT = "Farmers_Disease_Report";
    public static final String TABLE_REPORT_IMAGES = "Report_Images";
    public static final String TABLE_VET_RESPONSE = "Vet_Response";
    public static final String TABLE_VET_RATINGS = "Vet_Ratings";
    public static final String TABLE_GLOBAL_ALERTS = "Global_Alerts";
    public static final String TABLE_USER_ALERT_STATUS = "User_Alert_Status";
    public static final String TABLE_PASSWORD_RESETS = "Password_Resets";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ROLES + " (" +
                "role_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "role_name TEXT NOT NULL UNIQUE)");

        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_names TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone TEXT, " +
                "password_hash TEXT NOT NULL, " +
                "is_active INTEGER DEFAULT 1, " +
                "role_id INTEGER, " +
                "profile_pic TEXT, " +
                "FOREIGN KEY (role_id) REFERENCES Roles(role_id))");

        db.execSQL("CREATE TABLE " + TABLE_AUDIT_LOGS + " (" +
                "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "action TEXT NOT NULL, " +
                "time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id))");

        db.execSQL("CREATE TABLE " + TABLE_FARMERS_REPORT + " (" +
                "report_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "animal_type TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "symptoms TEXT NOT NULL, " +
                "date_observed TEXT NOT NULL, " +
                "gps_latitude REAL, " +
                "gps_longitude REAL, " +
                "severity_level TEXT, " +
                "suspected_disease TEXT, " +
                "affected_count INTEGER, " +
                "status TEXT DEFAULT 'Pending', " +
                "district_constituency TEXT, " +
                "village TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id))");

        db.execSQL("CREATE TABLE " + TABLE_REPORT_IMAGES + " (" +
                "image_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "report_id INTEGER, " +
                "image_url TEXT NOT NULL, " +
                "uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (report_id) REFERENCES Farmers_Disease_Report(report_id))");

        db.execSQL("CREATE TABLE " + TABLE_VET_RESPONSE + " (" +
                "response_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "report_id INTEGER, " +
                "vet_id INTEGER, " +
                "response_text TEXT NOT NULL, " +
                "visit_date TEXT, " +
                "recommendation TEXT, " +
                "visit_confirmed INTEGER DEFAULT 0, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (report_id) REFERENCES Farmers_Disease_Report(report_id), " +
                "FOREIGN KEY (vet_id) REFERENCES Users(user_id))");

        db.execSQL("CREATE TABLE " + TABLE_VET_RATINGS + " (" +
                "rating_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "vet_id INTEGER, " +
                "farmer_id INTEGER, " +
                "report_id INTEGER, " +
                "rating INTEGER CHECK (rating BETWEEN 1 AND 5), " +
                "comment TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (vet_id) REFERENCES Users(user_id), " +
                "FOREIGN KEY (farmer_id) REFERENCES Users(user_id), " +
                "FOREIGN KEY (report_id) REFERENCES Farmers_Disease_Report(report_id))");

        db.execSQL("CREATE TABLE " + TABLE_GLOBAL_ALERTS + " (" +
                "alert_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "message TEXT NOT NULL, " +
                "disease_name TEXT, " +
                "severity_level TEXT, " +
                "district TEXT, " +
                "animal_type TEXT, " +
                "report_count INTEGER, " +
                "source TEXT DEFAULT 'MANUAL', " +
                "created_by INTEGER, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (created_by) REFERENCES Users(user_id))");

        db.execSQL("CREATE TABLE " + TABLE_USER_ALERT_STATUS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "alert_id INTEGER NOT NULL, " +
                "is_read INTEGER DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id), " +
                "FOREIGN KEY (alert_id) REFERENCES Global_Alerts(alert_id))");

        db.execSQL("CREATE TABLE " + TABLE_PASSWORD_RESETS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT NOT NULL, " +
                "code TEXT NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

        insertRole(db, "Farmer");
        insertRole(db, "Veterinary Officer");
        insertRole(db, "Admin");
        insertAdmin(db);
    }

    private void insertRole(SQLiteDatabase db, String roleName) {
        ContentValues values = new ContentValues();
        values.put("role_name", roleName);
        db.insert(TABLE_ROLES, null, values);
    }

    private void insertAdmin(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("full_names", "Admin User");
        values.put("email", "admin@livestock.com");
        values.put("phone", "12345678");
        values.put("password_hash", "admin123");
        values.put("role_id", 3);
        values.put("is_active", 1);
        db.insert(TABLE_USERS, null, values);
    }

    public boolean addUser(String name, String email, String phone, String password, int roleId, String profilePic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_names", name);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password_hash", password);
        values.put("role_id", roleId);
        values.put("profile_pic", profilePic);
        values.put("is_active", 1);
        long result = db.insert(TABLE_USERS, null, values);
        if (result != -1) {
            addAuditLog((int) result, "Account Created");
            String roleStr = (roleId == 1) ? "Farmer" : (roleId == 2 ? "Veterinary Officer" : "Admin");
            EmailHelper.sendWelcomeEmail(email, name, roleStr);
        }
        return result != -1;
    }

    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT user_id FROM Users WHERE email = ?", new String[]{email});
        int id = -1;
        if (cursor.moveToFirst()) id = cursor.getInt(0);
        cursor.close();
        return id;
    }

    public boolean updateUserProfile(String email, String name, String phone, String profilePic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_names", name);
        values.put("phone", phone);
        if (profilePic != null && !profilePic.isEmpty()) values.put("profile_pic", profilePic);
        int result = db.update(TABLE_USERS, values, "email = ?", new String[]{email});
        if (result > 0) {
            int id = getUserIdByEmail(email);
            if (id != -1) addAuditLog(id, "Profile Updated");
        }
        return result > 0;
    }

    public boolean toggleUserStatus(int userId, int currentStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        int newStatus = (currentStatus == 1) ? 0 : 1;
        values.put("is_active", newStatus);
        int result = db.update(TABLE_USERS, values, "user_id = ?", new String[]{String.valueOf(userId)});
        if (result > 0) addAuditLog(userId, "Account " + (newStatus == 1 ? "Activated" : "Deactivated"));
        return result > 0;
    }

    public List<Map<String, String>> getAllUsersExcept(String currentEmail) {
        List<Map<String, String>> usersList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT U.*, R.role_name FROM Users U JOIN Roles R ON U.role_id = R.role_id WHERE U.email != ?";
        Cursor cursor = db.rawQuery(query, new String[]{currentEmail});
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> user = new HashMap<>();
                user.put("id", cursor.getString(cursor.getColumnIndexOrThrow("user_id")));
                user.put("name", cursor.getString(cursor.getColumnIndexOrThrow("full_names")));
                user.put("email", cursor.getString(cursor.getColumnIndexOrThrow("email")));
                user.put("phone", cursor.getString(cursor.getColumnIndexOrThrow("phone")));
                user.put("role", cursor.getString(cursor.getColumnIndexOrThrow("role_name")));
                user.put("status", cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1 ? "Active" : "Inactive");
                user.put("is_active", cursor.getString(cursor.getColumnIndexOrThrow("is_active")));
                user.put("pic", cursor.getString(cursor.getColumnIndexOrThrow("profile_pic")));
                usersList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return usersList;
    }

    public List<String> getAllUserEmails() {
        List<String> emails = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT email FROM Users WHERE is_active = 1", null);
        if (cursor.moveToFirst()) do { emails.add(cursor.getString(0)); } while (cursor.moveToNext());
        cursor.close();
        return emails;
    }
    
    public List<String> getEmailsByRole(int roleId) {
        List<String> emails = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT email FROM Users WHERE role_id = ? AND is_active = 1", new String[]{String.valueOf(roleId)});
        if (cursor.moveToFirst()) do { emails.add(cursor.getString(0)); } while (cursor.moveToNext());
        cursor.close();
        return emails;
    }

    // --- ALERTS MODULE ---

    public long addGlobalAlert(String title, String message, String disease, String severity, String district, String animal, int reportCount, String source, int createdBy) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("message", message);
        values.put("disease_name", disease);
        values.put("severity_level", severity);
        values.put("district", district);
        values.put("animal_type", animal);
        values.put("report_count", reportCount);
        values.put("source", source);
        values.put("created_by", createdBy);
        long alertId = db.insert(TABLE_GLOBAL_ALERTS, null, values);
        if (alertId != -1) {
            if (createdBy != -1) addAuditLog(createdBy, "Alert Created: " + title);
            List<String> emails = getAllUserEmails();
            EmailHelper.sendOutbreakAlert(title, message, emails);
        }
        return alertId;
    }

    public List<Map<String, String>> getAllAlerts(int userId) {
        List<Map<String, String>> alerts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT A.*, U.full_names, (SELECT is_read FROM User_Alert_Status WHERE alert_id = A.alert_id AND user_id = ?) as is_read " +
                "FROM " + TABLE_GLOBAL_ALERTS + " A " +
                "LEFT JOIN " + TABLE_USERS + " U ON A.created_by = U.user_id " +
                "ORDER BY A.created_at DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                Map<String, String> alert = new HashMap<>();
                alert.put("id", cursor.getString(cursor.getColumnIndexOrThrow("alert_id")));
                alert.put("title", cursor.getString(cursor.getColumnIndexOrThrow("title")));
                alert.put("message", cursor.getString(cursor.getColumnIndexOrThrow("message")));
                alert.put("disease", cursor.getString(cursor.getColumnIndexOrThrow("disease_name")));
                alert.put("severity", cursor.getString(cursor.getColumnIndexOrThrow("severity_level")));
                alert.put("district", cursor.getString(cursor.getColumnIndexOrThrow("district")));
                alert.put("created_at", cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                String author = cursor.getString(cursor.getColumnIndexOrThrow("full_names"));
                alert.put("author", author != null ? author : "System (AUTO)");
                alert.put("is_read", cursor.getString(cursor.getColumnIndexOrThrow("is_read")));
                alerts.add(alert);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alerts;
    }

    public void markAlertAsRead(int userId, int alertId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("alert_id", alertId);
        values.put("is_read", 1);
        db.insertWithOnConflict(TABLE_USER_ALERT_STATUS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public int getUnreadAlertCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_GLOBAL_ALERTS + " WHERE alert_id NOT IN (SELECT alert_id FROM " + TABLE_USER_ALERT_STATUS + " WHERE user_id = ? AND is_read = 1)";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // --- PASSWORD RESET ---

    public void saveResetCode(String email, String code) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PASSWORD_RESETS, "email = ?", new String[]{email});
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("code", code);
        db.insert(TABLE_PASSWORD_RESETS, null, values);
        EmailHelper.sendForgotPasswordEmail(email, code);
    }

    public boolean verifyResetCode(String email, String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PASSWORD_RESETS + " WHERE email = ? AND code = ?", new String[]{email, code});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password_hash", newPassword);
        int res = db.update(TABLE_USERS, values, "email = ?", new String[]{email});
        if (res > 0) db.delete(TABLE_PASSWORD_RESETS, "email = ?", new String[]{email});
        return res > 0;
    }

    // --- DISEASE REPORTS & VET ---

    public long addDiseaseReport(int userId, String animalType, String description, String symptoms, String dateObserved, double lat, double lng, String severity, String suspected, int affectedCount, String district, String village) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("user_id", userId); v.put("animal_type", animalType); v.put("description", description); v.put("symptoms", symptoms); v.put("date_observed", dateObserved); v.put("gps_latitude", lat); v.put("gps_longitude", lng); v.put("severity_level", severity); v.put("suspected_disease", suspected); v.put("affected_count", affectedCount); v.put("district_constituency", district); v.put("village", village); v.put("status", "Pending");
        long reportId = db.insert(TABLE_FARMERS_REPORT, null, v);
        
        if (reportId != -1) {
            checkForOutbreak(animalType, district, dateObserved);
        }
        
        return reportId;
    }
    
    private void checkForOutbreak(String animal, String district, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_FARMERS_REPORT + " WHERE animal_type = ? AND district_constituency = ? AND date_observed = ?";
        Cursor c = db.rawQuery(query, new String[]{animal, district, date});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        
        if (count > 5) {
            // Check if alert already exists for this date, place, and animal
            String checkAlert = "SELECT COUNT(*) FROM " + TABLE_GLOBAL_ALERTS + " WHERE district = ? AND animal_type = ? AND created_at LIKE ?";
            String todayPrefix = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()) + "%";
            Cursor c2 = db.rawQuery(checkAlert, new String[]{district, animal, todayPrefix});
            int alertExists = 0;
            if (c2.moveToFirst()) alertExists = c2.getInt(0);
            c2.close();
            
            if (alertExists == 0) {
                String title = "Potential Outbreak Detected: " + animal;
                String msg = "Potential outbreak detected: " + count + " " + animal + " reports from " + district + " today.";
                addGlobalAlert(title, msg, "Multiple Cases", "Critical", district, animal, count, "AUTO", -1);
            }
        }
    }

    public void addReportImage(long reportId, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues(); v.put("report_id", reportId); v.put("image_url", url);
        db.insert(TABLE_REPORT_IMAGES, null, v);
    }

    public List<Map<String, String>> getVetsToRate(int farmerId) {
        List<Map<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT R.report_id, U.user_id, U.full_names, U.profile_pic, VR.response_text " +
                "FROM Farmers_Disease_Report R " +
                "JOIN Vet_Response VR ON R.report_id = VR.report_id " +
                "JOIN Users U ON VR.vet_id = U.user_id " +
                "WHERE R.user_id = ? AND R.report_id NOT IN (SELECT report_id FROM Vet_Ratings WHERE farmer_id = ?)";
        Cursor c = db.rawQuery(query, new String[]{String.valueOf(farmerId), String.valueOf(farmerId)});
        if (c.moveToFirst()) {
            do {
                Map<String, String> m = new HashMap<>();
                m.put("report_id", c.getString(0));
                m.put("id", c.getString(1));
                m.put("name", c.getString(2));
                m.put("pic", c.getString(3));
                m.put("message", c.getString(4));
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean addRating(int farmerId, int vetId, int reportId, float rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("farmer_id", farmerId); v.put("vet_id", vetId); v.put("report_id", reportId); v.put("rating", (int)rating); v.put("comment", comment);
        return db.insert(TABLE_VET_RATINGS, null, v) != -1;
    }

    public void addAuditLog(int userId, String action) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues(); v.put("user_id", userId); v.put("action", action);
        db.insert(TABLE_AUDIT_LOGS, null, v);
    }

    public List<Map<String, String>> getAuditLogs() {
        List<Map<String, String>> logs = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT L.*, U.full_names FROM Audit_Logs L JOIN Users U ON L.user_id = U.user_id ORDER BY L.time DESC", null);
        if (c.moveToFirst()) { do { Map<String, String> m = new HashMap<>(); m.put("name", c.getString(c.getColumnIndexOrThrow("full_names"))); m.put("action", c.getString(c.getColumnIndexOrThrow("action"))); m.put("time", c.getString(c.getColumnIndexOrThrow("time"))); logs.add(m); } while (c.moveToNext()); }
        c.close(); return logs;
    }

    public List<Map<String, String>> getReportsByFarmer(int farmerId) {
        List<Map<String, String>> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT R.*, V.visit_date, V.visit_confirmed, VET.full_names as vet_name, V.response_text as response FROM Farmers_Disease_Report R LEFT JOIN Vet_Response V ON R.report_id = V.report_id LEFT JOIN Users VET ON V.vet_id = VET.user_id WHERE R.user_id = ? ORDER BY R.created_at DESC", new String[]{String.valueOf(farmerId)});
        if (c.moveToFirst()) { do { Map<String, String> m = new HashMap<>(); m.put("id", c.getString(c.getColumnIndexOrThrow("report_id"))); m.put("animal", c.getString(c.getColumnIndexOrThrow("animal_type"))); m.put("status", c.getString(c.getColumnIndexOrThrow("status"))); m.put("date", c.getString(c.getColumnIndexOrThrow("date_observed"))); m.put("suspected", c.getString(c.getColumnIndexOrThrow("suspected_disease"))); m.put("visit_date", c.getString(c.getColumnIndexOrThrow("visit_date"))); m.put("visit_confirmed", c.getString(c.getColumnIndexOrThrow("visit_confirmed"))); m.put("vet_name", c.getString(c.getColumnIndexOrThrow("vet_name"))); m.put("response", c.getString(c.getColumnIndexOrThrow("response"))); list.add(m); } while (c.moveToNext()); }
        c.close(); return list;
    }

    public List<Map<String, String>> getPreviousReports() {
        List<Map<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT R.*, U.full_names as farmer_name, VET.full_names as vet_name " +
                "FROM Farmers_Disease_Report R " +
                "JOIN Users U ON R.user_id = U.user_id " +
                "LEFT JOIN Vet_Response VR ON R.report_id = VR.report_id " +
                "LEFT JOIN Users VET ON VR.vet_id = VET.user_id " +
                "WHERE R.status != 'Pending' " +
                "ORDER BY R.created_at DESC";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                Map<String, String> m = new HashMap<>();
                m.put("id", c.getString(c.getColumnIndexOrThrow("report_id")));
                m.put("animal", c.getString(c.getColumnIndexOrThrow("animal_type")));
                m.put("farmer", c.getString(c.getColumnIndexOrThrow("farmer_name")));
                m.put("disease", c.getString(c.getColumnIndexOrThrow("suspected_disease")));
                m.put("status", c.getString(c.getColumnIndexOrThrow("status")));
                m.put("date", c.getString(c.getColumnIndexOrThrow("date_observed")));
                m.put("vet", c.getString(c.getColumnIndexOrThrow("vet_name")));
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public List<Map<String, String>> getAllPendingReports() {
        List<Map<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT R.*, U.full_names as farmer_name " +
                "FROM Farmers_Disease_Report R " +
                "JOIN Users U ON R.user_id = U.user_id " +
                "WHERE R.status = 'Pending' " +
                "ORDER BY R.created_at DESC";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            do {
                Map<String, String> m = new HashMap<>();
                m.put("id", c.getString(c.getColumnIndexOrThrow("report_id")));
                m.put("farmer", c.getString(c.getColumnIndexOrThrow("farmer_name")));
                m.put("severity", c.getString(c.getColumnIndexOrThrow("severity_level")));
                m.put("animal", c.getString(c.getColumnIndexOrThrow("animal_type")));
                m.put("desc", c.getString(c.getColumnIndexOrThrow("description")));
                m.put("symptoms", c.getString(c.getColumnIndexOrThrow("symptoms")));
                m.put("suspected", c.getString(c.getColumnIndexOrThrow("suspected_disease")));
                m.put("affected", c.getString(c.getColumnIndexOrThrow("affected_count")));
                m.put("location", c.getString(c.getColumnIndexOrThrow("district_constituency")));
                m.put("village", c.getString(c.getColumnIndexOrThrow("village")));
                m.put("date", c.getString(c.getColumnIndexOrThrow("date_observed")));
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public List<Map<String, String>> getVetSchedule(String email) {
        List<Map<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT R.*, U.full_names as farmer_name, V.visit_date, V.visit_confirmed " +
                "FROM Farmers_Disease_Report R " +
                "JOIN Users U ON R.user_id = U.user_id " +
                "JOIN Vet_Response V ON R.report_id = V.report_id " +
                "JOIN Users VET ON V.vet_id = VET.user_id " +
                "WHERE VET.email = ? " +
                "ORDER BY V.visit_date ASC";
        Cursor c = db.rawQuery(query, new String[]{email});
        if (c.moveToFirst()) {
            do {
                Map<String, String> m = new HashMap<>();
                m.put("animal", c.getString(c.getColumnIndexOrThrow("animal_type")));
                m.put("farmer", c.getString(c.getColumnIndexOrThrow("farmer_name")));
                m.put("location", c.getString(c.getColumnIndexOrThrow("district_constituency")));
                m.put("desc", c.getString(c.getColumnIndexOrThrow("description")));
                m.put("symptoms", c.getString(c.getColumnIndexOrThrow("symptoms")));
                m.put("report_date", c.getString(c.getColumnIndexOrThrow("created_at")));
                m.put("visit_date", c.getString(c.getColumnIndexOrThrow("visit_date")));
                m.put("confirmed", c.getString(c.getColumnIndexOrThrow("visit_confirmed")));
                list.add(m);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public Map<String, String> getReportById(int reportId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT R.*, U.full_names as farmer_name, V.visit_date, V.response_text " +
                "FROM " + TABLE_FARMERS_REPORT + " R " +
                "JOIN " + TABLE_USERS + " U ON R.user_id = U.user_id " +
                "LEFT JOIN " + TABLE_VET_RESPONSE + " V ON R.report_id = V.report_id " +
                "WHERE R.report_id = ?";
        Cursor c = db.rawQuery(query, new String[]{String.valueOf(reportId)});
        Map<String, String> m = null;
        if (c.moveToFirst()) {
            m = new HashMap<>();
            m.put("id", c.getString(c.getColumnIndexOrThrow("report_id")));
            m.put("farmer", c.getString(c.getColumnIndexOrThrow("farmer_name")));
            m.put("animal", c.getString(c.getColumnIndexOrThrow("animal_type")));
            m.put("location", c.getString(c.getColumnIndexOrThrow("district_constituency")));
            m.put("village", c.getString(c.getColumnIndexOrThrow("village")));
            m.put("lat", c.getString(c.getColumnIndexOrThrow("gps_latitude")));
            m.put("lng", c.getString(c.getColumnIndexOrThrow("gps_longitude")));
            m.put("visit_date", c.getString(c.getColumnIndexOrThrow("visit_date")));
            m.put("response", c.getString(c.getColumnIndexOrThrow("response_text")));
            m.put("status", c.getString(c.getColumnIndexOrThrow("status")));
        }
        c.close();
        return m;
    }

    public List<String> getReportImages(int reportId) {
        List<String> images = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT image_url FROM " + TABLE_REPORT_IMAGES + " WHERE report_id = ?", new String[]{String.valueOf(reportId)});
        if (c.moveToFirst()) {
            do {
                images.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return images;
    }

    public boolean addVetResponse(String reportId, int vetId, String response, String visitDate, String recommendation, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put("report_id", reportId);
            v.put("vet_id", vetId);
            v.put("response_text", response);
            v.put("visit_date", visitDate);
            v.put("recommendation", recommendation);
            long res = db.insert(TABLE_VET_RESPONSE, null, v);

            if (res != -1) {
                ContentValues v2 = new ContentValues();
                v2.put("status", status);
                db.update(TABLE_FARMERS_REPORT, v2, "report_id = ?", new String[]{reportId});
                db.setTransactionSuccessful();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return false;
    }

    public boolean confirmVisit(int reportId) {
        ContentValues v = new ContentValues(); v.put("visit_confirmed", 1);
        return getWritableDatabase().update("Vet_Response", v, "report_id = ?", new String[]{String.valueOf(reportId)}) > 0;
    }

    public List<Map<String, String>> getAllRatings() {
        List<Map<String, String>> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT T.*, VET.full_names as vet_name, FARM.full_names as farmer_name FROM Vet_Ratings T JOIN Users VET ON T.vet_id = VET.user_id JOIN Users FARM ON T.farmer_id = FARM.user_id ORDER BY T.created_at DESC", null);
        if (c.moveToFirst()) { do { Map<String, String> m = new HashMap<>(); m.put("vet", c.getString(c.getColumnIndexOrThrow("vet_name"))); m.put("farmer", c.getString(c.getColumnIndexOrThrow("farmer_name"))); m.put("report_id", c.getString(c.getColumnIndexOrThrow("report_id"))); m.put("stars", c.getString(c.getColumnIndexOrThrow("rating"))); m.put("time", c.getString(c.getColumnIndexOrThrow("created_at"))); list.add(m); } while (c.moveToNext()); }
        c.close(); return list;
    }

    // --- ADMIN DASHBOARD STATS ---

    public int getTotalUsersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getActiveAlertsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Assume alerts from the last 30 days are "active" or just count all
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_GLOBAL_ALERTS, null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public Map<String, Integer> getReportsByAnimalType() {
        Map<String, Integer> data = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT animal_type, COUNT(*) FROM " + TABLE_FARMERS_REPORT + " GROUP BY animal_type", null);
        if (cursor.moveToFirst()) {
            do {
                data.put(cursor.getString(0), cursor.getInt(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return data;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDIT_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FARMERS_REPORT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORT_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VET_RESPONSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VET_RATINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GLOBAL_ALERTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_ALERT_STATUS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSWORD_RESETS);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) { super.onOpen(db); if (!db.isReadOnly()) db.execSQL("PRAGMA foreign_keys=ON;"); }
}
