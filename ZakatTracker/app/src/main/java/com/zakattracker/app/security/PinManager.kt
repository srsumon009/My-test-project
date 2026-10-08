package com.zakattracker.app.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Handles the app's 6-digit PIN lock. The PIN itself is never stored in plain text: only a
 * salted SHA-256 hash lives on disk, inside an [EncryptedSharedPreferences] file (which is
 * itself encrypted at rest using a key kept in the Android Keystore).
 *
 * The app ships with the default PIN 009900. [isDefaultPinStillActive] tells the UI whether
 * the user has ever changed it, so the first successful login can force a change before the
 * dashboard is reachable.
 */
class PinManager(context: Context) {

    companion object {
        const val DEFAULT_PIN = "009900"
        private const val PREFS_NAME = "zakat_secure_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_CHANGED = "pin_changed"
        private const val KEY_REQUIRE_PIN = "require_pin"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Ensures a PIN hash exists (seeded with the default PIN) the very first time the app runs. */
    fun ensureInitialized() {
        if (!prefs.contains(KEY_PIN_HASH)) {
            setPinInternal(DEFAULT_PIN, markChanged = false)
            prefs.edit().putBoolean(KEY_REQUIRE_PIN, true).apply()
        }
    }

    fun isDefaultPinStillActive(): Boolean = !prefs.getBoolean(KEY_PIN_CHANGED, false)

    fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val expectedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return hash(pin, salt) == expectedHash
    }

    /** Sets a new PIN and marks the default PIN as no longer active. */
    fun setPin(newPin: String) = setPinInternal(newPin, markChanged = true)

    private fun setPinInternal(pin: String, markChanged: Boolean) {
        val salt = generateSalt()
        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash(pin, salt))
            .apply()
        if (markChanged) {
            prefs.edit().putBoolean(KEY_PIN_CHANGED, true).apply()
        }
    }

    var requirePinOnResume: Boolean
        get() = prefs.getBoolean(KEY_REQUIRE_PIN, true)
        set(value) = prefs.edit().putBoolean(KEY_REQUIRE_PIN, value).apply()

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hash(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((salt + pin).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
