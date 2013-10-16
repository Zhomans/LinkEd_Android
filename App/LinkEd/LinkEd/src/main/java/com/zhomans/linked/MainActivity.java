package com.zhomans.linked;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import LinkEd.linked.R;

import android.app.Activity;
import android.util.SparseArray;

import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    // more efficient than HashMap for mapping integers to objects
    SparseArray<Group> groups = new SparseArray<Group>();
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String PREF_USERNAME = "ids";
    public static String id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        Set<String> empty = new HashSet<String>();
        final Set ids = pref.getStringSet(PREF_USERNAME, empty);

        if (ids == empty) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setIcon(R.drawable.ic_social_add_person);
            alert.setTitle("ID");
            alert.setMessage("Please enter an ID.");

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", null);
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            final AlertDialog dialog = alert.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Boolean wantToCloseDialog = false;
                    Editable value = input.getText();
                    try{
                        if (!value.toString().matches("")) {
                            Integer int_value = Integer.parseInt(value.toString());
                            id = int_value.toString();
                            ids.add(int_value.toString());
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                    .edit()
                                    .putStringSet(PREF_USERNAME,ids)
                                    .commit();
                            wantToCloseDialog = true;
                            connect();
                        }
                    }
                    catch (NumberFormatException e){}
                    if(wantToCloseDialog)
                        dialog.dismiss();
                }
            });
        }
        else {id = ids.iterator().next().toString();

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        MyExpandableListAdapter adapter = new MyExpandableListAdapter(this,
                groups);
        listView.setAdapter(adapter);

        connect();
        }
    }

    public void connect() {
        new AsyncTask<Void, Void, SparseArray<Group>>() {
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            InputStream inputStream = null;
            String result = "";


            @Override
            protected void onPreExecute() {
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
            }

            protected SparseArray<Group> doInBackground(Void... voids) {
                SparseArray<Group> data = new SparseArray<Group>();

                try {
                    String website = "http://olin-linked.herokuapp.com/"+id;
                    HttpGet all_tweets = new HttpGet(website);
                    all_tweets.setHeader("Content-type","application/json");

                    response = client.execute(all_tweets);
                    response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();

                    inputStream = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),8);
                    StringBuilder sb = new StringBuilder();

                    String line;
                    String nl = System.getProperty("line.separator");
                    while ((line = reader.readLine())!= null){
                        sb.append(line + nl);
                    }
                    result = sb.toString();
                }
                catch (Exception e) {e.printStackTrace(); Log.e("Server", "Cannot Establish Connection");
                }
                finally{
                    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}}

                try {
                    if (!result.equals("")) {
                        JSONObject jObject = new JSONObject(result);
                        ArrayList<String> sections = new ArrayList<String>();
                        JSONArray names = jObject.names();
                        if (names != null) {
                            for (int i=0;i<names.length();i++){
                                sections.add(names.get(i).toString());
                            }
                        }
                        ArrayList<ArrayList<String>> all_section_data = new ArrayList<ArrayList<String>>();
                        for (int i=0;i<sections.size();i++){
                            ArrayList<String> section_data = new ArrayList<String>();
                            String section = sections.get(i);
                            JSONArray jArray = jObject.getJSONArray(section);
                            for (int j=0;j<jArray.length();j++){
                                section_data.add(jArray.getJSONObject(j).get("title").toString());
                            }
                            all_section_data.add(section_data);
                        }
                        ArrayList<Integer> icons = new ArrayList<Integer>();
                        for (int i=0;i<sections.size();i++){
                            String section = sections.get(i);
                            if (section.equals("Math") || section.equals("Mathematics")) {
                                icons.add(R.drawable.math_blue);
                            } else if (section.equals("Writing")) {
                                icons.add(R.drawable.writing_blue);
                            } else if (section.equals("Reading") || section.equals("English")) {
                                icons.add(R.drawable.reading_blue);
                            } else if (section.equals("History") || section.equals("Geography")) {
                                icons.add(R.drawable.history_blue);
                            } else if (section.equals("Science")) {
                                icons.add(R.drawable.science_blue);
                            } else {
                                icons.add(R.drawable.default_blue);
                            }
                        }

                        for (int j = 0; j < sections.size(); j++) {
                            Group group = new Group(sections.get(j),icons.get(j));
                            for (int i = 0; i < all_section_data.get(j).size(); i++) {
                                group.children.add(all_section_data.get(j).get(i));
                            }
                            data.append(j, group);
                        }
                    }
                }catch (JSONException e){e.printStackTrace();}
                return data;
            }

            protected void onPostExecute(SparseArray<Group> all_data){
                MyExpandableListAdapter adapter = new MyExpandableListAdapter(MainActivity.this, all_data);
                ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
                listView.setAdapter(adapter);
                for (int i=0;i<adapter.getGroupCount();i++){
                    listView.expandGroup(i);
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        Set<String> empty = new HashSet<String>();
        final Set ids = pref.getStringSet(PREF_USERNAME, empty);
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                MainActivity.this);
        builderSingle.setIcon(R.drawable.ic_social_group);
        builderSingle.setTitle("Select An ID:");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.select_dialog_singlechoice);


        switch (item.getItemId()) {
            case R.id.action_change:
                arrayAdapter.addAll(ids);
                builderSingle.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        // Do something useful withe the position of the selected radio button
                        id = arrayAdapter.getItem(selectedPosition);
                        connect();
                    }
                });

                builderSingle.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                id = arrayAdapter.getItem(which);
                                connect();
                            }
                        });
                builderSingle.show();
                break;

            case R.id.action_add:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setIcon(R.drawable.ic_social_add_person);
                alert.setTitle("ID");
                alert.setMessage("Please enter an ID.");

                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                alert.setView(input);
                alert.setPositiveButton("Ok", null);
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
                final AlertDialog dialog = alert.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Boolean wantToCloseDialog = false;
                        Editable value = input.getText();
                        try{
                            if (!value.toString().matches("")) {
                                Integer int_value = Integer.parseInt(value.toString());
                                id = int_value.toString();
                                ids.add(id);
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                        .edit()
                                        .remove(PREF_USERNAME)
                                        .commit();
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                        .edit()
                                        .putStringSet(PREF_USERNAME,ids)
                                        .commit();
                                wantToCloseDialog = true;
                                connect();
                            }
                        }
                        catch (NumberFormatException e){}
                        if(wantToCloseDialog)
                            dialog.dismiss();
                    }
                });
                break;

            case R.id.action_delete:
                arrayAdapter.addAll(ids);
                builderSingle.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                id = arrayAdapter.getItem(which);
                                ids.remove(id);
                            }
                        });
                builderSingle.show();
                break;
            default:
                break;
        }

        return true;
    }
}
