package com.tadas.passman;

public class Item {
    private String itemName;
    private String username;
    private String password;
    private int  id;

    public Item(int id, String itemName, String username, String password) {
        this.id = id;
        this.itemName = itemName;
        this.username = username;
        this.password = password;
    }
    public int getId() {return id; }
    public String getItemName() {
        return itemName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
