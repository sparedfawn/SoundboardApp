package com.example.kkali.soundboardapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// view is a representation of a "rectangle area of the screen"

public class EventHandler {

    private static final String LOG_TAG = "EVENTHANDLER";

    private static MediaPlayer mp;

    private static Database database;

    // playing sound
    public static void startMediaPlayer(View view, Integer soundID)
    {
        try
        {
            if(soundID != null)
            {
                if(mp != null)
                {
                    mp.reset();
                }
                mp = MediaPlayer.create(view.getContext(), soundID);
                mp.start();
            }
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    // stopping it
    public static void releaseMediaPlayer()
    {
        if(mp != null)
        {
            mp.release();
            mp = null;
        }
    }

    //popup menu when button represetnation held
    public static void popupManager(final View view, final SoundObject soundObject)
    {
        database = new Database(view.getContext());

        PopupMenu popup = new PopupMenu(view.getContext(), view);
        //popup.getMenuInflater().inflate(R.menu.longclick, popup.getMenu());

        // check if we are in favourite sounds (we will have different options -> either add to favorites or remove from favorites)
        if(view.getContext() instanceof FavouriteActivity)
        {
            popup.getMenuInflater().inflate(R.menu.favo_longclick, popup.getMenu());
        }
        else
        {
            popup.getMenuInflater().inflate(R.menu.longclick, popup.getMenu());
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(menuItem.getItemId() == R.id.action_send || menuItem.getItemId() == R.id.action_ringtone)    // action chosen
                {
                    final String fileName = soundObject.getItemName() + ".mp3";

                    // placing sound as a physical object on our phone
                    File storage = Environment.getExternalStorageDirectory();
                    File directory = new File(storage.getAbsolutePath() + "/my_soundboard/");
                    directory.mkdirs();

                    final File file = new File(directory, fileName);

                    InputStream in = view.getContext().getResources().openRawResource(soundObject.getItemID());

                    try
                    {
                        OutputStream out = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];

                        int len;

                        while((len=in.read(buffer, 0, buffer.length)) != -1)
                        {
                            out.write(buffer, 0, len);
                        }

                        in.close();
                        out.close();
                    }
                    catch(IOException e)
                    {
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    if(menuItem.getItemId() == R.id.action_send)    // sending
                    {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)   // for newer versions
                        {
                            final String AUTHORITY = view.getContext().getPackageName() + ".fileprovider";

                            Uri contentUrl = FileProvider.getUriForFile(view.getContext(), AUTHORITY, file);

                            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUrl);
                            shareIntent.setType("audio/mp3");
                            view.getContext().startActivity(Intent.createChooser(shareIntent, "Share sound via..."));
                        }
                        else    // for older versions
                        {
                            final Intent shareIntent = new Intent(Intent.ACTION_SEND);

                            Uri fileUri = Uri.parse(file.getAbsolutePath());

                            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                            shareIntent.setType("audio/mp3");
                            view.getContext().startActivity(Intent.createChooser(shareIntent, "Share sound via..."));
                        }
                    }
                    if(menuItem.getItemId() == R.id.action_ringtone)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle(Html.fromHtml("<font color='#212121'>Save as...</font>"));
                        builder.setItems(new CharSequence[]{"Ringtone", "Notification", "Alarm"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i)
                                {
                                    case 0:
                                    {
                                        changeSystemAudio(view, fileName, file, i);
                                        break;
                                    }
                                    case 1:
                                    {
                                        changeSystemAudio(view, fileName, file, i);
                                        break;
                                    }
                                    case 2:
                                    {
                                        changeSystemAudio(view, fileName, file, i);
                                        break;
                                    }
                                }
                            }
                        });

                        builder.create();
                        builder.show();
                    }
                }

                if(menuItem.getItemId() == R.id.action_favorite)   // if adding or removing from favorites
                {
                    if(view.getContext() instanceof FavouriteActivity)   // removing sound if we are in favorite sounds
                    {
                        database.removeFavourite(view.getContext(), soundObject);
                    }
                    else
                    {
                        database.addFavourite(soundObject);
                    }
                }

                return true;
            }
        });

        popup.show();
    }

    private static void changeSystemAudio(View view, String fileName, File file, int action)
    {
        try
        {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");

            switch(action)  // adding audio to chosen type of use
            {
                case 1:
                {
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                    values.put(MediaStore.Audio.Media.IS_ALARM, false);
                    break;
                }
                case 2:
                {
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                    values.put(MediaStore.Audio.Media.IS_ALARM, false);
                    break;
                }
                case 3:
                {
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                    values.put(MediaStore.Audio.Media.IS_ALARM, true);
                    break;
                }
            }

            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
            view.getContext().getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + file.getAbsolutePath() + "\"", null);
            Uri finalUri = view.getContext().getContentResolver().insert(uri, values);

            switch(action)
            {
                case 1:
                {
                    RingtoneManager.setActualDefaultRingtoneUri(view.getContext(), RingtoneManager.TYPE_RINGTONE, finalUri);
                    break;
                }
                case 2:
                {
                    RingtoneManager.setActualDefaultRingtoneUri(view.getContext(), RingtoneManager.TYPE_NOTIFICATION, finalUri);
                    break;
                }
                case 3:
                {
                    RingtoneManager.setActualDefaultRingtoneUri(view.getContext(), RingtoneManager.TYPE_ALARM, finalUri);
                    break;
                }
            }
        }
        catch(Exception e)
        {
            Log.e(LOG_TAG, e.getMessage());
        }
    }
}
