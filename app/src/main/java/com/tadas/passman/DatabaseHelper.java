package com.tadas.passman;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "items.db";
    private static final String TABLE_NAME = "items_table";
    private static final String COL1 = "ID";
    private static final String COL2 = "ITEM_NAME";
    private static final String COL3 = "USERNAME";
    private static final String COL4 = "PASSWORD";

    protected static String PASSWORD;

   private Context mContext;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase.loadLibs(context);
        mContext = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 + " TEXT, " +
                COL3 + " TEXT, " +
                COL4 + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String item, String username, String password) {
        SQLiteDatabase db = getWritableDatabase(getStoredEncryptionKey());
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, item);
        contentValues.put(COL3, username);
        contentValues.put(COL4, password);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase(getStoredEncryptionKey());
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COL1));
                        @SuppressLint("Range") String itemName = cursor.getString(cursor.getColumnIndex(COL2));
                        @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(COL3));
                        @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COL4));

                        Item item = new Item(id, itemName, username, password);
                        itemList.add(item);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        return itemList;
    }
    public List<Item> searchItemsByName(String itemName) {
        List<Item> itemList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase(PASSWORD);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL2 + " LIKE ?", new String[]{"%" + itemName + "%"});

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COL1));
                        @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(COL3));
                        @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(COL4));

                        Item item = new Item(id, itemName, username, password);
                        itemList.add(item);
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }

        return itemList;
    }

    // possibility to store without encryption to txt
    public void exportDataToTxt(Context context) {
        List<Item> itemList = getAllItems();
        File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "PassmanExports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "exported_data.txt");
        try {
            FileWriter fileWriter = new FileWriter(file);
            for (Item item : itemList) {
                String line = item.getId() + ", " + item.getItemName() + ", " +
                        item.getUsername() + ", " + item.getPassword() + "\n";
                fileWriter.write(line);
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportDatabase(Context context) {
        SQLiteDatabase db = getReadableDatabase(getStoredEncryptionKey());
        // Saves db to Documents folder.
       // File exportDir = new File(context.getExternalFilesDir(null), "Android/data/" + context.getPackageName() + "/files/ExportedDatabases");
        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        File sourceDbFile = context.getDatabasePath(DATABASE_NAME);
        File destinationDbFile = new File(exportDir, "exported_items.db");

        try {
            copyFile(sourceDbFile, destinationDbFile);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(destinationDbFile));
            context.sendBroadcast(mediaScanIntent);

            Toast.makeText(context, "Database exported successfully", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(context, "Export failed. Please try again.", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }



    private void copyFile(File sourceFile, File destFile) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;

        try {
            sourceChannel = new FileInputStream(sourceFile).getChannel();
            destChannel = new FileOutputStream(destFile).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (destChannel != null) {
                destChannel.close();
            }
        }
    }

    public void deleteItem(Item item) {
        SQLiteDatabase db = getWritableDatabase(getStoredEncryptionKey());
        String whereClause = COL1 + " = ?";
        String[] whereArgs = {String.valueOf(item.getId())};
        db.delete(TABLE_NAME, whereClause, whereArgs);
    }



    protected void storeEncryptionKey(String PASSWORD) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("regular_prefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("password", PASSWORD).apply();
    }

    protected String getStoredEncryptionKey() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("regular_prefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("password", null);
    }

}
