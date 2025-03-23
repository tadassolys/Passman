package com.tadas.passman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.*;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import java.util.concurrent.Executor;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final long SESSION_TIMEOUT_MS = 15 * 1000; // 15 seconds
    private SharedPreferences preferences;
    private boolean isAuthenticated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate called");

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Always set authentication to false when creating the activity
        // This forces a fresh check every time the app starts
        isAuthenticated = false;

        // Check if biometric is supported
        if (!checkBioMetricSupported()) {
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");

        // Always force authentication check in onStart
        checkSessionAndAuthenticate();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart called");
        // This is called when the activity is being restarted after being stopped
        // Force re-check of session status
        isAuthenticated = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        // Get the app background time
        long appBackgroundTime = preferences.getLong("appBackgroundTime", 0);
        long currentTime = System.currentTimeMillis();

        // If app was in background and timeout has passed, force authentication
        if (appBackgroundTime > 0 && (currentTime - appBackgroundTime) > SESSION_TIMEOUT_MS) {
            Log.d(TAG, "App was minimized longer than timeout, forcing authentication");
            isAuthenticated = false;
            authenticateUser();
            return;
        }

        // Otherwise check session normally
        if (!isAuthenticated) {
            checkSessionAndAuthenticate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");

        // Critical: Save the current time when app goes to background
        preferences.edit().putLong("appBackgroundTime", System.currentTimeMillis()).apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");

        // App is no longer visible
        // We don't force authentication here as that happens in onResume/onStart
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");

        // Activity is being destroyed
        isAuthenticated = false;
        preferences.edit().putBoolean("isAuthenticated", false).apply();
    }

    private void checkSessionAndAuthenticate() {
        // First check if we need to authenticate due to timeout
        if (isSessionExpired()) {
            Log.d(TAG, "Session expired, requesting authentication");
            authenticateUser();
        } else if (!isAuthenticated) {
            // Session might be valid but we still need to authenticate this instance
            authenticateUser();
        } else {
            // We're authenticated and session is valid
            proceedToNextActivity();
        }
    }

    private boolean isSessionExpired() {
        long lastAuthTime = preferences.getLong("lastAuthTime", 0);
        long appBackgroundTime = preferences.getLong("appBackgroundTime", 0);
        long currentTime = System.currentTimeMillis();

        // If never authenticated before
        if (lastAuthTime == 0) {
            Log.d(TAG, "No previous authentication found");
            return true;
        }

        // Check if we've been in background longer than timeout
        if (appBackgroundTime > 0) {
            long backgroundDuration = currentTime - appBackgroundTime;
            if (backgroundDuration > SESSION_TIMEOUT_MS) {
                Log.d(TAG, "Session expired in background after " + backgroundDuration + "ms");
                return true;
            }
        }

        // Check general timeout since last authentication
        long authDuration = currentTime - lastAuthTime;
        if (authDuration > SESSION_TIMEOUT_MS) {
            Log.d(TAG, "General session timeout after " + authDuration + "ms");
            return true;
        }

        return false;
    }

    private void authenticateUser() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.d(TAG, "Authentication error: " + errString);
                showToast("Authentication error: " + errString);
                isAuthenticated = false;
                preferences.edit().putBoolean("isAuthenticated", false).apply();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Authentication succeeded");
                //showToast("Authentication succeeded!");

                // Update timestamp and auth status
                long currentTime = System.currentTimeMillis();
                preferences.edit()
                        .putLong("lastAuthTime", currentTime)
                        .putLong("appBackgroundTime", 0) // Reset background time
                        .putBoolean("isAuthenticated", true)
                        .apply();

                isAuthenticated = true;
                proceedToNextActivity();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = dialogMetric();
        biometricPrompt.authenticate(promptInfo);
    }

    private void proceedToNextActivity() {
        startActivity(new Intent(getApplicationContext(), EncryptionKeyInputActivity.class));
        finish();
    }

    private BiometricPrompt.PromptInfo dialogMetric() {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setSubtitle("Log in using biometrics or device credentials")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();
    }

    private boolean checkBioMetricSupported() {
        BiometricManager manager = BiometricManager.from(this);
        int canAuthenticate = manager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                showToast("No biometric features available on this device.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                showToast("Biometric features are currently unavailable.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                promptEnrollBiometrics();
                return false;
            default:
                showToast("Unknown error occurred.");
                return false;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void promptEnrollBiometrics() {
        new AlertDialog.Builder(this)
                .setTitle("Biometric Setup Required")
                .setMessage("No biometrics are enrolled. Would you like to set them up now?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent enrollIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                    startActivity(enrollIntent);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}