# Livestock Health & Disease Tracking App

An Android application designed to connect farmers with local veterinary professionals, enabling quick disease reporting, proximity-based vet matching, and local health record management.

## Tech Stack

* **Language:** Kotlin & Java
* **IDE:** Android Studio
* **Database:** SQLite / Room Database
* **Architecture:** MVVM (Model-View-ViewModel) / Android Jetpack

---

## Key Features

* **Farmer Portal:** Log disease reports, describe symptoms, and track report status.
* **Nearby Vet Locator:** View and contact local veterinary doctors for fast response times.
* **SQLite Offline Storage:** Store disease reports, animal profiles, and doctor directories locally on the device.
* **Media Uploads:** Attach photo logs of livestock symptoms directly within the app.

---

## Getting Started

### Prerequisites

* Android Studio (Ladybug or newer recommended)
* JDK 14 or higher
* Android SDK (API Level 24+ recommended)
* An Android Virtual Device (Emulator) or a physical Android device with USB debugging enabled

### Build & Run Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Mothabi1216/Livestock.git
   ```

2. **Open in Android Studio:**
   * Open Android Studio.
   * Select **Open an Existing Project**.
   * Navigate to the cloned directory and click **OK**.

3. **Sync & Run:**
   * Wait for Gradle sync to complete.
   * Select your emulator/device and press **Shift + F10** or click the green **Run** button.

---

## Database Schema (SQLite)

* **Farmers:** `id`, `name`, `location`, `phone`
* **Vets:** `id`, `name`, `specialty`, `clinic_location`, `contact`
* **Disease Reports:** `id`, `farmer_id`, `animal_type`, `symptoms`, `image_uri`, `status`, `assigned_vet_id`, `timestamp`