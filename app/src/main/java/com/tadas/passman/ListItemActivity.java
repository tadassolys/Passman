package com.tadas.passman;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ListItemActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_item);

        Intent intent = getIntent();

        String itemName = intent.getStringExtra("itemName");
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");

        TextView textViewItem = findViewById(R.id.textViewItemDetail);
        TextView textViewUsername = findViewById(R.id.textViewUsernameDetail);
        TextView textViewPassword = findViewById(R.id.textViewPasswordDetail);

        textViewItem.setText("Item Name: " + itemName);
        textViewUsername.setText("Username: " + username);
        textViewPassword.setText("Password: " + password);
    }
}
