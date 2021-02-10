package com.example.kkali.soundboardapp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

// class handles activities favourite sounds related

public class FavouriteActivity extends AppCompatActivity {

    private static final String LOG_TAG = "FAVOURITEACTIVITY";

    Toolbar toolbar;

    ArrayList<SoundObject> favouriteList = new ArrayList<>();

    RecyclerView FavouriteView;
    SoundboardRecyclerAdapter FavouriteAdapter = new SoundboardRecyclerAdapter(favouriteList);
    RecyclerView.LayoutManager FavouriteLayoutManager;

    Database database = new Database(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        toolbar = (Toolbar) findViewById(R.id.favourite_toolbar);
        setSupportActionBar(toolbar);

        addDataToArrayList();

        FavouriteView = (RecyclerView) findViewById(R.id.favouriteRecyclerView);

        FavouriteLayoutManager = new GridLayoutManager(this, 3);
        FavouriteView.setLayoutManager(FavouriteLayoutManager);
        FavouriteView.setAdapter(FavouriteAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar_menu_fav, menu);   // setting up toolbar with full star

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_favourite_hide)      // switching back to main context
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventHandler.releaseMediaPlayer();
    }

    private void addDataToArrayList() {
        favouriteList.clear();

        Cursor cursor = database.getFavorites();

        if (cursor.getCount() == 0) {

            Log.e(LOG_TAG, "Cursor is null");

            cursor.close();
        }

        if (cursor.getCount() != favouriteList.size()) {    // adding sound to the favourites
            while (cursor.moveToNext()) {
                String NAME = cursor.getString(cursor.getColumnIndex("favoName"));
                Integer ID = cursor.getInt(cursor.getColumnIndex("favoId"));

                favouriteList.add(new SoundObject(NAME, ID));

                FavouriteAdapter.notifyDataSetChanged();
            }
            cursor.close();
        }
    }
}
