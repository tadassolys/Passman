package com.tadas.passman;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listViewItems;
    private ArrayList<Item> itemList;
    private ItemAdapter itemAdapter;
    private DatabaseHelper dbHelper;
    private SearchView searchView;
    private BroadcastReceiver receiver;
    private List<Item> originalItemList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewItems = findViewById(R.id.listViewItems);
        searchView = findViewById(R.id.searchView);

        dbHelper = new DatabaseHelper(this);

        itemList = new ArrayList<>();
        originalItemList = new ArrayList<>();
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
                copyToClipboard(copyLogin == 0 ? clickedItem.getUsername() : clickedItem.getPassword());
            }
        });

        listViewItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Item longClickedItem = itemList.get(position);
                showDeleteConfirmationDialog(longClickedItem);
                return true;
            }
        });

        updateItemList("");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("data_changed")) {
                    updateItemList("");
                }
            }
        };

        registerReceiver(receiver, new IntentFilter("data_changed"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }


    private void updateItemList(String searchTerm) {
        originalItemList = dbHelper.getAllItems();
        if (searchTerm.isEmpty()) {
            itemList.clear();
            itemList.addAll(originalItemList);
        } else {
            itemList.clear();
            for (Item originalItem : originalItemList) {
                if (originalItem.getItemName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    itemList.add(originalItem);
                }
            }
        }
        itemAdapter.notifyDataSetChanged();
    }


    private int copyLogin = 0;

    private void copyToClipboard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clipData;
        if (copyLogin == 0) {
            clipData = ClipData.newPlainText("Username", text);
        } else {
            clipData = ClipData.newPlainText("Password", text);
        }

        clipboardManager.setPrimaryClip(clipData);

        String message = copyLogin == 0 ? "Username copied" : "Password copied";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        copyLogin = (copyLogin + 1) % 2;
        // Schedule clipboard clearing after 30 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""));
                //Toast.makeText(MainActivity.this, "Clipboard cleared", Toast.LENGTH_SHORT).show();
            }
        }, 30000); // 30000 milliseconds = 30 seconds
    }

    private void showDeleteConfirmationDialog(final Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.deleteItem(item);
                updateItemList("");
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_item:
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_export:
                showExportConfirmationDialog();
                return true;
            case R.id.menu_edit:
                Intent editItemIntent = new Intent(MainActivity.this, EditItemActivity.class);
                startActivity(editItemIntent);
                return true;
            case R.id.menu_import:
                showImportFileChooser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showExportConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Database")
                .setMessage("Are you sure you want to export the database?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exportData();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void exportData() {
        try {
            dbHelper.exportDatabase(getApplicationContext());
            Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //reikia naujo approucho
    // Initialize ActivityResultLauncher for file chooser
    private final ActivityResultLauncher<Intent> importFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importDatabaseFromUri(uri); // Import the database
                    }
                }
            }
    );

    // Show file chooser to select a database file
    private void showImportFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream"); // Adjust MIME type if necessary
        importFileLauncher.launch(intent); // Use the ActivityResultLauncher
    }

    // Importuoja duomenų bazę iš nurodyto URI
private void importDatabaseFromUri(Uri uri) {
    File tempFile = null;
    try {
        // Atidaro įvesties srautą iš URI (failo, kurį pasirinko vartotojas)
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream != null) {
            // Sukuriamas laikinas failas su originaliu failo pavadinimu
            tempFile = new File(getFilesDir(), getFileNameFromUri(uri));
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            // Kopijuoja duomenis iš inputStream į outputStream (laikiną failą)
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Uždaromi srautai po kopijavimo
            outputStream.close();
            inputStream.close();

            // Importuojama duomenų bazė iš laikino failo
            dbHelper.importDatabase(tempFile);
            Toast.makeText(this, "Duomenų bazė sėkmingai importuota", Toast.LENGTH_SHORT).show();
            updateItemList(""); 
        }
    } catch (Exception e) {
        Log.e("MainActivity", "Klaida importuojant duomenų bazę: " + e.getMessage());
        Toast.makeText(this, "Nepavyko importuoti duomenų bazės", Toast.LENGTH_SHORT).show();
    } finally {
        // Ištriname laikiną failą po importavimo, jei jis buvo sukurtas
        if (tempFile != null && tempFile.exists()) {
            boolean deleted = tempFile.delete();
            if (deleted) {
                Log.d("MainActivity", "Laikinas failas ištrintas.");
            } else {
                Log.e("MainActivity", "Nepavyko ištrinti laikino failo.");
            }
        }
    }
}

// Gauti failo pavadinimą iš URI
private String getFileNameFromUri(Uri uri) {
    String fileName = "exported_items.db"; // Numatytas failo pavadinimas (jei nepavyksta gauti)
    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
    if (cursor != null) {
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        if (cursor.moveToFirst() && nameIndex >= 0) {
            fileName = cursor.getString(nameIndex); // Gauti tikrą failo pavadinimą
        }
        cursor.close();
    }
    return fileName;
}
}
