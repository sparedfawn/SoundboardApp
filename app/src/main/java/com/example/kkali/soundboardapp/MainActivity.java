package com.example.kkali.soundboardapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MAINACTIVITY";

    Toolbar toolbar;

    ArrayList<SoundObject> soundList = new ArrayList<>();

    RecyclerView SoundView;
    SoundboardRecyclerAdapter SoundAdapter = new SoundboardRecyclerAdapter(soundList);
    RecyclerView.LayoutManager SoundLayoutManager;

    private View mLayout;

    Database database = new Database(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {    // setting up the app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundboard);

        if(appUpdate())
        {
            database.createSoundObjectCollection(this);
        }

        mLayout = findViewById(R.id.activity_soundboard);

        toolbar = (Toolbar) findViewById(R.id.soundboard_toolbar);
        setSupportActionBar(toolbar);

        addDataToArrayList();

        SoundView = (RecyclerView) findViewById(R.id.soundboardRecyclerView);

        SoundLayoutManager = new GridLayoutManager(this, 3);
        SoundView.setLayoutManager(SoundLayoutManager);
        SoundView.setAdapter(SoundAdapter);

        requestPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar_menu, menu);       // toolbar with empty star

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_favourite_show)
            this.startActivity(new Intent(this, FavouriteActivity.class));

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventHandler.releaseMediaPlayer();
    }

    private void addDataToArrayList()
    {
        soundList.clear();

        Cursor cursor = database.getSoundCollection();

        if(cursor.getCount() == 0)
        {
            Log.e(LOG_TAG, "Cursor is null");
        }

        if(cursor.getCount() != soundList.size())   // adding sound to main table
        {
            while(cursor.moveToNext())
            {
                String NAME = cursor.getString(cursor.getColumnIndex("soundName"));
                Integer ID = cursor.getInt(cursor.getColumnIndex("soundId"));

                soundList.add(new SoundObject(NAME, ID));

                SoundAdapter.notifyDataSetChanged();
            }
            cursor.close();
        }
    }

    private void requestPermissions()       // requesting permissions is necessary for stuff like sending or setting up as ringtone
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)      // for newer versions (only there that is necessary)
        {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }

            if(!Settings.System.canWrite(this))
            {
                Snackbar.make(mLayout, "The app needs access to your settings", Snackbar.LENGTH_INDEFINITE).setAction("OK",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Context context = view.getContext();
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:"+context.getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }).show();
            }
        }
    }

    private boolean appUpdate()     // checks if there is an update
    {
        final String PREFS_NAME = "VersionPref";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        int currentVersionCode = 0;
        try
        {
            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            Log.e(LOG_TAG, e.getMessage());
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        SharedPreferences.Editor edit = prefs.edit();

        if(currentVersionCode > savedVersionCode)
        {
            edit.putInt(PREF_VERSION_CODE_KEY, currentVersionCode);
            edit.commit();
            return true;
        }

        return false;
    }
}
