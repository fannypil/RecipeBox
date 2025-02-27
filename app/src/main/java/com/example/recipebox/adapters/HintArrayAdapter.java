package com.example.recipebox.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HintArrayAdapter<T> extends ArrayAdapter<T> {
    public HintArrayAdapter(@NonNull Context context, int resource, @NonNull T[] objects) {
        super(context, resource, objects);
    }
    @Override
    public boolean isEnabled(int position) {
        return position != 0;// prevent picking first option (hint)
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;
        if (position == 0) {
            textView.setTextColor(0xFFAAAAAA); // grey color- hint
        } else {
            textView.setTextColor(0xFF000000); // black color- options
        }
        return view;
    }

}
