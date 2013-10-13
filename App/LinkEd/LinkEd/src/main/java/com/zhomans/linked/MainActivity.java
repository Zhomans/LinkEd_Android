package com.zhomans.linked;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import LinkEd.linked.R;

public class MainActivity extends ListActivity {
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new TextAdapter(this, listItems);
        setListAdapter(adapter);
        addItems("Sup");
        addItems("Hi");



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void addItems(String item) {
        listItems.add(item);
        adapter.notifyDataSetChanged();
    }
}
