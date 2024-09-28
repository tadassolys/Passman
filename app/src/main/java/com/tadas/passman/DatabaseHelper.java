package com.tadas.passman;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.Toast;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "items.db";
    private static final String TABLE_NAME = "items_table";
    private static final String COL1 = "ID";
    private static final String COL2 = "ITEM_NAME";
    private static final String COL3 = "USERNAME";
    private static final String COL4 = "PASSWORD";

    private static final String KEYSTORE_ALIAS = "encryption_key_alias";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String SHARED_PREFS_NAME = "regular_prefs";
    private static final String ENCRYPTED_KEY_PREF = "encrypted_key";

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

    public void exportDatabase(Context context) {
        SQLiteDatabase db = getReadableDatabase(getStoredEncryptionKey());
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

    public void importDatabase(File importFile) {
        SQLiteDatabase db = getWritableDatabase(getStoredEncryptionKey());
        SQLiteDatabase importDb = SQLiteDatabase.openOrCreateDatabase(importFile.getAbsolutePath(), getStoredEncryptionKey(), null);

        try {
            db.beginTransaction(); // Start a transaction

            Cursor cursor = importDb.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ContentValues contentValues = new ContentValues();
                    int itemNameIndex = cursor.getColumnIndex(COL2);
                    int usernameIndex = cursor.getColumnIndex(COL3);
                    int passwordIndex = cursor.getColumnIndex(COL4);

                    if (itemNameIndex >= 0 && usernameIndex >= 0 && passwordIndex >= 0) {
                        contentValues.put(COL2, cursor.getString(itemNameIndex));
                        contentValues.put(COL3, cursor.getString(usernameIndex));
                        contentValues.put(COL4, cursor.getString(passwordIndex));

                        db.insert(TABLE_NAME, null, contentValues);
                    } else {
                        Log.e("DatabaseHelper", "One or more columns do not exist in the imported database.");
                    }
                } while (cursor.moveToNext());
            }

            if (cursor != null) {
                cursor.close();
            }

            db.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error importing database: " + e.getMessage());
        } finally {
            db.endTransaction(); // End the transaction
            importDb.close(); // Close the imported database
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

    public boolean updateItem(Item item) {
        SQLiteDatabase db = getWritableDatabase(getStoredEncryptionKey());
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, item.getItemName());
        contentValues.put(COL3, item.getUsername());
        contentValues.put(COL4, item.getPassword());

        String whereClause = COL1 + " = ?";
        String[] whereArgs = {String.valueOf(item.getId())};

        int result = db.update(TABLE_NAME, contentValues, whereClause, whereArgs);
        return result > 0;
    }

    protected void storeEncryptionKey(String encryptionKey) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
                keyGenerator.init(
                        new KeyGenParameterSpec.Builder(KEYSTORE_ALIAS,
                                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                .build());
                keyGenerator.generateKey();
            }

            SecretKey secretKey = ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEYSTORE_ALIAS, null)).getSecretKey();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptionKeyBytes = encryptionKey.getBytes();
            byte[] iv = cipher.getIV();
            byte[] encryptedKey = cipher.doFinal(encryptionKeyBytes);

            SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(ENCRYPTED_KEY_PREF, bytesToHex(encryptedKey));
            editor.putString(ENCRYPTED_KEY_PREF + "_iv", bytesToHex(iv));
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the encryption key used for SQLCipher from SharedPreferences.
     * The actual encryption key is not stored in SharedPreferences; only metadata needed for decryption (the encrypted key and IV) is stored there.
     * This method decrypts the key using the stored IV (Initialization Vector) and
     * the key from the Android Keystore, and returns the plaintext encryption key for use with SQLCipher.
     */
    protected String getStoredEncryptionKey() {
        try {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            String encryptedKeyHex = sharedPreferences.getString(ENCRYPTED_KEY_PREF, null);
            String ivHex = sharedPreferences.getString(ENCRYPTED_KEY_PREF + "_iv", null);

            if (encryptedKeyHex == null || ivHex == null) {
                return null;
            }

            byte[] encryptedKey = hexStringToByteArray(encryptedKeyHex);
            byte[] iv = hexStringToByteArray(ivHex);

            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            SecretKey secretKey = ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEYSTORE_ALIAS, null)).getSecretKey();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] decryptedKeyBytes = cipher.doFinal(encryptedKey);
            return new String(decryptedKeyBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to convert byte array to hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Helper method to convert hex string to byte array
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
