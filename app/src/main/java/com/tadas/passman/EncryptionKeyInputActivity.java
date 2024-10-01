package com.tadas.passman;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class EncryptionKeyInputActivity extends AppCompatActivity {

    private EditText manualEncryptionKey;
     /*
     * The PREFS_NAME and FIRST_RUN_FLAG work together to store and manage the state of whether the app has been run for the first time.
     * PREFS_NAME is the name of the SharedPreferences file where the state is stored.
     * FIRST_RUN_FLAG is the specific key within the SharedPreferences file that holds a boolean value,
     * tracking whether the app has completed its first run setup.
     */
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_RUN_FLAG = "firstRunFlag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFirstRun()) {
            setContentView(R.layout.activity_encryption_input);

            manualEncryptionKey = findViewById(R.id.encryptionEditText);
            Button inputSaltButton = findViewById(R.id.inputEncryptionButton);


            inputSaltButton.setOnClickListener(view -> {
                String enteredEncryptionPassword = manualEncryptionKey.getText().toString();

                DatabaseHelper databaseHelper = new DatabaseHelper(EncryptionKeyInputActivity.this);
                databaseHelper.storeEncryptionKey(enteredEncryptionPassword);

                navigateToMainActivity();
            });
            setFirstRunFlag();
        } else {
            navigateToMainActivity();
        }
    }

    private void navigateToMainActivity() {
        Intent loginIntent = new Intent(EncryptionKeyInputActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private boolean isFirstRun() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(FIRST_RUN_FLAG, true);
    }

    private void setFirstRunFlag() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FIRST_RUN_FLAG, false);
        editor.apply();
    }
}
