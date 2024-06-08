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
<!-- ![Main Screen](screenshots/main_screen.png) -->
<!-- ![Add Item Screen](screenshots/add_item_screen.png) -->

## Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/yourusername/passman.git
    ```
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
