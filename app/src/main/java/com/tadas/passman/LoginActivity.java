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

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkBioMetricSupported();

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                Intent encryptionInputIntent = new Intent(getApplicationContext(), EncryptionKeyInputActivity.class);
                startActivity(encryptionInputIntent);
            }


            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo.Builder promptInfo = dialogMetric();
        promptInfo.setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
        biometricPrompt.authenticate(promptInfo.build());
    }

    BiometricPrompt.PromptInfo.Builder dialogMetric() {
        return new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login")
                .setSubtitle("Log in using biometrics or device credentials")
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
    }


    // Must be running Android 6
    void checkBioMetricSupported() {
        BiometricManager manager = BiometricManager.from(this);
        int canAuthenticate = manager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
        String info = "";

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                info = "App can authenticate using biometrics or device credentials.";
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                info = "No biometric features available on this device.";
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                info = "Biometric features are currently unavailable.";
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Direct the user to enroll biometrics or set up a PIN/password
                info = "No biometrics enrolled. Using device PIN/password instead.";
                Intent enrollIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                startActivity(enrollIntent);
                return; // Stop execution since user is redirected
            default:
                info = "Unknown error occurred.";
        }

        Toast.makeText(getApplicationContext(), info, Toast.LENGTH_LONG).show();
    }
}
