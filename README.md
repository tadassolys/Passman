# PassMan

PassMan is a simple and secure password manager application for Android, built using SQLCipher for database encryption. It allows users to store and manage their usernames and passwords securely. The app provides functionality for adding, viewing, searching, copying, and deleting credentials, as well as exporting the database for backup purposes.

## Features

- **Secure Storage**: Utilizes SQLCipher to encrypt and secure the database.
- **Add Credentials**: Easily add new username and password pairs.
- **Search**: Quickly find stored credentials using the search functionality.
- **Copy to Clipboard**: Copy usernames or passwords to the clipboard with a single tap.
- **Delete Credentials**: Long-press to delete credentials with a confirmation dialog.
- **Export Database**: Export the encrypted database for backup purposes.

## Screenshots

<!--  screenshots -->
![Screenshot_20240628-133347](https://github.com/tadassolys/Passman/assets/103380760/c130515f-afea-4a61-b140-95afdd663624)
![Screenshot_20240628-133400](https://github.com/tadassolys/Passman/assets/103380760/a8db4817-4529-4b63-bcc4-3376d1cfc2f9)
![Screenshot_20240628-133354](https://github.com/tadassolys/Passman/assets/103380760/a4e49709-dd82-4b45-84f2-89ae5a40e50f)
![Screenshot_20240628-133407](https://github.com/tadassolys/Passman/assets/103380760/f15c0532-dc83-4aef-b305-d356ab242a26)
![Screenshot_20240628-133416](https://github.com/tadassolys/Passman/assets/103380760/f56a53d0-2f25-42b5-bf1d-f4d5c43716cd)
![Screenshot_20240628-133424](https://github.com/tadassolys/Passman/assets/103380760/62781762-a4ac-4473-ab9b-d227e0ffe58a)


## Installation

1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run the project on an Android device or emulator.

## Usage

### Adding Credentials

1. Click the "Add Item" button in the menu.
2. Fill in the username and password fields.
3. Click "Save" to store the credentials securely.

### Searching for Credentials

- Use the search bar to filter the list of stored credentials.

### Copying Credentials to Clipboard

- Tap on a credential in the list to copy the username or password to the clipboard. The app alternates between copying the username and the password each time.

### Deleting Credentials

1. Long-press on a credential in the list.
2. Confirm the deletion in the dialog that appears.

### Exporting the Database

1. Click the "Export Data" button in the menu.
2. Confirm the export in the dialog that appears.
3. The database will be exported to the application's external storage directory.
4. The encrypted database can be opened on a PC using the password set by the user with tools like [DB Browser for SQLite](https://sqlitebrowser.org/).
