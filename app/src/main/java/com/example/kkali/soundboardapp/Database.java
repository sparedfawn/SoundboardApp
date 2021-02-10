package com.example.kkali.soundboardapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

// cursor is a type of object, which we can read from the result of sql query

public class Database extends SQLiteOpenHelper
{
    private static final String LOG_TAG = "DATABASEHANDLER";

    private static final String DATABASE_NAME = "soundboard.db";
    private static final int DATABASE_VERSION = 12;         // has to match with gradle build version


    // table variables

    private static final String MAIN_TABLE = "main_table";
    private static final String MAIN_ID = "_id";
    private static final String MAIN_NAME = "soundName";
    private static final String MAIN_ITEM_ID = "soundId";

    private static final String FAVORITES_TABLE = "favorites_table";
    private static final String FAVORITES_ID = "_id";
    private static final String FAVORITES_NAME = "favoName";
    private static final String FAVORITES_ITEM_ID = "favoId";


    // creating tables SQL commands

    private static final String SQL_CREATE_MAIN_TABLE = "CREATE TABLE IF NOT EXISTS " + MAIN_TABLE + "(" + MAIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MAIN_NAME + " TEXT, " + MAIN_ITEM_ID + " INTEGER unique);";

    private static final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE IF NOT EXISTS " + FAVORITES_TABLE + "(" + FAVORITES_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FAVORITES_NAME + " TEXT, " + FAVORITES_ITEM_ID + " INTEGER);";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try
        {
            db.execSQL(SQL_CREATE_MAIN_TABLE);          // creating database tables
            db.execSQL(SQL_CREATE_FAVORITES_TABLE);
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + MAIN_TABLE);

        onCreate(db);
    }

    public void createSoundObjectCollection (Context context) {

        // taking string list of names from string.xml file
        List<String> nameList = Arrays.asList(context.getResources().getStringArray(R.array.soundNames));

        // soundobject list
        SoundObject[] soundObjects = {
                new SoundObject(nameList.get(0), R.raw.actually_fakty),
                new SoundObject(nameList.get(1), R.raw.amha),
                new SoundObject(nameList.get(2), R.raw.amha_rejemcted),
                new SoundObject(nameList.get(3), R.raw.big_brain_brothers),
                new SoundObject(nameList.get(4), R.raw.big_brain_time),
                new SoundObject(nameList.get(5), R.raw.citek_moment),
                new SoundObject(nameList.get(6), R.raw.crimge),
                new SoundObject(nameList.get(7), R.raw.czy_ty_mozesz),
                new SoundObject(nameList.get(8), R.raw.depremsja),
                new SoundObject(nameList.get(9), R.raw.dobre_pomaranczowe),
                new SoundObject(nameList.get(10), R.raw.julka_moment),
                new SoundObject(nameList.get(11), R.raw.karasiuk),
                new SoundObject(nameList.get(12), R.raw.karzel),
                new SoundObject(nameList.get(13), R.raw.kebabwe),
                new SoundObject(nameList.get(14), R.raw.komtumzja),
                new SoundObject(nameList.get(15), R.raw.na_morza_dnie),
                new SoundObject(nameList.get(16), R.raw.papryka),
                new SoundObject(nameList.get(17), R.raw.polak_robak),
                new SoundObject(nameList.get(18), R.raw.polak_rodak),
                new SoundObject(nameList.get(19), R.raw.potramfie_tylko_zawomdzic),
                new SoundObject(nameList.get(20), R.raw.rabarbar),
                new SoundObject(nameList.get(21), R.raw.rejemcted),
                new SoundObject(nameList.get(22), R.raw.skad_to_zwatpienie),
                new SoundObject(nameList.get(23), R.raw.szymon_cieply),
                new SoundObject(nameList.get(24), R.raw.udajesz),
                new SoundObject(nameList.get(25), R.raw.weebledon),
                new SoundObject(nameList.get(26), R.raw.yone_ahonen),
                new SoundObject(nameList.get(27), R.raw.zdziadziales)
        };

        for(int i=0; i<soundObjects.length; i++)

            putIntoMain(soundObjects[i]);     // adding sounds to a main table

    }

    private void putIntoMain(SoundObject soundObject) {


        SQLiteDatabase database = this.getWritableDatabase();

        if(!verification(database, MAIN_TABLE, MAIN_ITEM_ID, soundObject.getItemID()))      // if there is no such sound in main
        {
            try
            {
                ContentValues contentValues = new ContentValues();

                contentValues.put(MAIN_NAME, soundObject.getItemName());
                contentValues.put(MAIN_ITEM_ID, soundObject.getItemID());

                database.insert(MAIN_TABLE, null, contentValues);
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, e.getMessage());
            }
            finally
            {
                database.close();
            }
        }
    }

    // this function checks whether there is a certain sound in database
    private boolean verification (SQLiteDatabase database, String tableName, String idColumn, Integer soundID)
    {
        int count = -1;
        Cursor cursor = null;

        try
        {
            String query = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = " + soundID;
            cursor = database.rawQuery(query, null);

            if(cursor.moveToFirst())    // moving cursor to first record; returns true if completed

                count = cursor.getInt(0);


            return (count > 0);
        }
        finally
        {
            if(cursor != null)
            {
                cursor.close();
            }
        }
    }


    public Cursor getSoundCollection()
    {
        SQLiteDatabase database = this.getReadableDatabase();

        return database.rawQuery("SELECT * FROM " + MAIN_TABLE + " ORDER BY " + MAIN_NAME, null);
    }

    public void addFavourite(SoundObject soundObject)
    {
        SQLiteDatabase database = this.getWritableDatabase();

        if(!verification(database, FAVORITES_TABLE, FAVORITES_ITEM_ID, soundObject.getItemID()))
        {
            try
            {
                ContentValues contentValues = new ContentValues();

                contentValues.put(FAVORITES_NAME, soundObject.getItemName());
                contentValues.put(FAVORITES_ITEM_ID, soundObject.getItemID());

                database.insert(FAVORITES_TABLE, null, contentValues);
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, e.getMessage());
            }
            finally
            {
                database.close();
            }
        }
    }

    public void removeFavourite(Context context, SoundObject soundObject)
    {
        SQLiteDatabase database = this.getWritableDatabase();

        if(verification(database, FAVORITES_TABLE, FAVORITES_ITEM_ID, soundObject.getItemID()))
        {
            try
            {
                database.delete(FAVORITES_TABLE, FAVORITES_ITEM_ID + " = " + soundObject.getItemID(), null);

                Activity activity = (Activity) context;
                Intent intent = activity.getIntent();
                activity.overridePendingTransition(0,0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                activity.finish();
                activity.overridePendingTransition(0,0);
                context.startActivity(intent);
            }
            catch(Exception e)
            {
                Log.e(LOG_TAG, e.getMessage());
            }
            finally
            {
                database.close();
            }
        }
    }

    public Cursor getFavorites()
    {
        SQLiteDatabase database = this.getReadableDatabase();

        return database.rawQuery("SELECT * FROM " + FAVORITES_TABLE + " ORDER BY " + FAVORITES_NAME, null);

    }
}
