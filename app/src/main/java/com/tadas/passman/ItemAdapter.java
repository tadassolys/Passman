package com.tadas.passman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {

    private final Context context;
    private final int resource;
    private final List<Item> itemList;

    public ItemAdapter(Context context, int resource, List<Item> itemList) {
        super(context, resource, itemList);
        this.context = context;
        this.resource = resource;
        this.itemList = itemList;
    }



    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
