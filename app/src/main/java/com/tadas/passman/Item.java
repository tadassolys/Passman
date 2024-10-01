package com.tadas.passman;

public class Item {
    private final String itemName;
    private String username;
    private String password;
    private int id;

    public Item(int id, String itemName, String username, String password) {
        this.id = id;
        this.itemName = itemName;
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
