package com.tadas.passman;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class EncryptionKeyInputActivity extends AppCompatActivity {

    private EditText manualEncryptionKey;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_RUN_FLAG = "firstRunFlag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFirstRun()) {
            setContentView(R.layout.activity_encryption_input);

            manualEncryptionKey = findViewById(R.id.encryptionEditText);
            Button inputSaltButton = findViewById(R.id.inputEncryptionButton);


            inputSaltButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String enteredEncryptionPassword = manualEncryptionKey.getText().toString();

                    DatabaseHelper databaseHelper = new DatabaseHelper(EncryptionKeyInputActivity.this);
                    databaseHelper.storeEncryptionKey(enteredEncryptionPassword);

                    navigateToMainActivity(true);
                }
            });
            setFirstRunFlag(false);
        } else {
            navigateToMainActivity(false);
        }
    }

    private void navigateToMainActivity(boolean manualInput) {
        Intent loginIntent = new Intent(EncryptionKeyInputActivity.this, MainActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private boolean isFirstRun() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(FIRST_RUN_FLAG, true);
    }

    private void setFirstRunFlag(boolean value) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FIRST_RUN_FLAG, value);
        editor.apply();
    }
}
