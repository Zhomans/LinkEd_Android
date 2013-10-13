package com.zhomans.linked;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import LinkEd.linked.R;

/**
 * Created by zach on 10/13/13.
 */
public class TextAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> values;

    public TextAdapter(Context context, ArrayList<String> values) {
        super(context, R.layout.list_row_view, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_row_view, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.itemText);
        textView.setText(values.get(position));

        return rowView;
    }
}
