package com.tadas.passman;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddItemActivity extends AppCompatActivity {

    private EditText editTextItemAdd, editTextUsernameAdd, editTextPasswordAdd;
    private Button btnSave;

    private DatabaseHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        dbHelper = new DatabaseHelper(this);

        editTextItemAdd = findViewById(R.id.editTextItemAdd);
        editTextUsernameAdd = findViewById(R.id.editTextUsernameAdd);
        editTextPasswordAdd = findViewById(R.id.editTextPasswordAdd);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });
    }

    private void saveItem() {
        String item = editTextItemAdd.getText().toString().trim();
        String username = editTextUsernameAdd.getText().toString().trim();
        String password = editTextPasswordAdd.getText().toString().trim();

        dbHelper.addData(item, username, password);
        Intent intent = new Intent("data_changed");
        sendBroadcast(intent);
        finish();
    }

}
