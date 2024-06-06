package com.tadas.passman;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {

    private Context context;
    private int resource;
    private List<Item> itemList;

    public ItemAdapter(Context context, int resource, List<Item> itemList) {
        super(context, resource, itemList);
        this.context = context;
        this.resource = resource;
        this.itemList = itemList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        Item currentItem = itemList.get(position);

        TextView textViewItem = convertView.findViewById(R.id.textViewItem);
        TextView textViewUsername = convertView.findViewById(R.id.textViewUsername);
        TextView textViewPassword = convertView.findViewById(R.id.textViewPassword);

        textViewItem.setText(currentItem.getItemName());
        textViewUsername.setText("Username: " + currentItem.getUsername());
        textViewPassword.setText("Password: " + currentItem.getPassword());

        return convertView;
    }
}
