# luckycloud Android App

Android app for accessing and managing files in luckycloud.

## Google Play

The app is available on Google Play:

[![Get it on Google Play](https://play.google.com/intl/en_us/badges/images/badge_new.png)](https://play.google.com/store/apps/details?id=de.luckycloud.luckycloud&hl=de)

---

## Build (Release APK)

### Prerequisites

- Android Studio (includes required Android SDK components)
- JDK (recommended: the version required by the Android Gradle Plugin used in this project)
- A configured signing setup (see below)

### Steps

1. Change into the project directory (adjust if your repository layout differs):

   ```bash
   cd luckycloud-Android
   ```

2. Configure signing:

   - Create a `key.properties` file **or** rename `key.properties.example` to `key.properties`
   - Update the values to match your local signing configuration

3. Create a keystore (only if you do not already have one)

   > Note: The command below is an example. Adapt alias, validity and distinguished name (DN) to your needs.

   ```bash
   keytool -genkey -v \
     -keystore app/debug.keystore \
     -alias AndroidDebugKey \
     -keyalg RSA -keysize 2048 -validity 1 \
     -storepass android -keypass android \
     -dname "cn=TEST, ou=TEST, o=TEST, c=TE"
   ```

4. Build the release APK:

   ```bash
   ./gradlew assembleRelease
   ```

### Output

After a successful build, the APK can be found at:

- `app/build/outputs/apk/luckycloud-${versionName}.apk`

---

## Development (Android Studio)

### Open the project

1. Launch Android Studio
2. Select **Open** (or **Import Project**, depending on your version)
3. Choose the `luckycloud-Android` directory
4. Import as a **Gradle** project and wait until the Gradle sync finishes

---

## Security Disclosure

We take security seriously. If you believe you have found a security vulnerability in the luckycloud Android Client (or related services), please report it responsibly so we can investigate and address the issue.

### How to report

- Email: **sec@luckycloud.de**
- Please include:
    - A clear description of the issue and potential impact
    - Steps to reproduce (proof-of-concept if available)
    - Affected versions / device information (Android version, device model)
    - Relevant logs, screenshots, or crash reports (if applicable)

### Guidelines

- Please **do not** publicly disclose the issue until we have had a chance to investigate and provide a fix.
- Avoid accessing data that does not belong to you and avoid disrupting services.

### What to expect

We will acknowledge your report and work with you to understand, reproduce, and remediate the issue as quickly as possible.
