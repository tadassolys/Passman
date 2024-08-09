package com.tadas.passman;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class EditItemActivity extends AppCompatActivity {

    private ListView listViewItems;
    private ArrayList<Item> itemList;
    private ItemAdapter itemAdapter;
    private DatabaseHelper dbHelper;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // You can reuse the same layout

        listViewItems = findViewById(R.id.listViewItems);
        searchView = findViewById(R.id.searchView);

        dbHelper = new DatabaseHelper(this);

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, R.layout.list_item, itemList);
        listViewItems.setAdapter(itemAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateItemList(newText);
                return true;
            }
        });

        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item clickedItem = itemList.get(position);
                showEditDialog(clickedItem);
            }
        });

        updateItemList("");
    }

    private void updateItemList(String searchTerm) {
        List<Item> originalItemList = dbHelper.getAllItems();
        itemList.clear();
        if (searchTerm.isEmpty()) {
            itemList.addAll(originalItemList);
        } else {
            for (Item originalItem : originalItemList) {
                if (originalItem.getItemName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    itemList.add(originalItem);
                }
            }
        }
        itemAdapter.notifyDataSetChanged();
    }

    private void showEditDialog(final Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Item");

        View viewInflated = getLayoutInflater().inflate(R.layout.dialog_edit_item, null);
        builder.setView(viewInflated);

        final EditText editTextUsername = viewInflated.findViewById(R.id.editTextUsernameEdit);
        final EditText editTextPassword = viewInflated.findViewById(R.id.editTextPasswordEdit);

        editTextUsername.setText(item.getUsername());
        editTextPassword.setText(item.getPassword());

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newUsername = editTextUsername.getText().toString().trim();
                String newPassword = editTextPassword.getText().toString().trim();

                if (!newUsername.isEmpty() && !newPassword.isEmpty()) {
                    item.setUsername(newUsername);
                    item.setPassword(newPassword);
                    dbHelper.updateItem(item);
                    updateItemList("");

                    //Broadcast to notify the MainActivity
                    Intent intent = new Intent("data_changed");
                    sendBroadcast(intent);

                    Toast.makeText(EditItemActivity.this, "Item updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditItemActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }
}
