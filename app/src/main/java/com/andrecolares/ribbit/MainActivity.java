package com.andrecolares.ribbit;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// ***The TabListener has been deprecated, will look for update later***
// ***The MainActivity class is implementing TabListener, so the     ***
// *** activity itself is listening for tab changes. We'll have to   ***
// *** replace this later. Remember that TabListener is an interface,***
// *** so we'll be looking for a replacement interface later.        ***
public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int PICK_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final int FILE_SIZE_LIMIT = 1024*1024*10; //This is 10MB

    protected Uri mMediaUri;


    protected DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0: //Take Picture
                            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            // get path to save images
                            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                            if(mMediaUri==null){
                                //display an error
                                Toast.makeText(MainActivity.this,
                                        R.string.error_external_storage,
                                        Toast.LENGTH_LONG).show();
                            }else {
                                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);

                                startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                            }
                            break;

                        case 1:  // take video
                            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                            mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                            if (mMediaUri == null) {
                                Toast.makeText(MainActivity.this,
                                        R.string.error_external_storage,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                                // limit video captures to 10 seconds / LQ
                                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
                                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);// 0= lowest res / 1=highest resolution
                                startActivityForResult(takeVideoIntent, TAKE_VIDEO_REQUEST);
                            }
                            break;

                        case 2:  // choose picture
                            //This intent search for all types of files
                            Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            //this code limit the type for only images
                            choosePhotoIntent.setType("image/*");

                            startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                            break;

                        case 3:  // choose video
                            Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            chooseVideoIntent.setType("video/*");

                            Toast.makeText(MainActivity.this, R.string.video_limit, Toast.LENGTH_LONG).show();

                            startActivityForResult(chooseVideoIntent, PICK_VIDEO_REQUEST);
                            break;
                    }
                }
                private Uri getOutputMediaFileUri(int mediaType) {
                    //To be safe, you should check that the SDCard is mounted
                    //using Environment.getExternalStorageState() before doing this

                    if(isExternalStorageAvaible()){
                        //Get the URI

                        //1. Get the external storage directory
                        String appName = MainActivity.this.getString(R.string.app_name);
                        File mediaStorageDir = new File(
                                Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_PICTURES),
                                getString(R.string.app_name)
                        );

                        // create subdirectory
                        if ( ! mediaStorageDir.exists() ) {
                            if ( ! mediaStorageDir.mkdirs() ) {
                                Log.e(TAG, "Failed to create directory");
                                return null;
                            }
                        }
                        //3.Create a file name
                        File mediaFile;

                        //4. Create the file
                        Date now = new Date();
                        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

                        String path = mediaStorageDir.getPath() + File.separator;

                        switch (mediaType) {
                            case MEDIA_TYPE_IMAGE:
                                mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                                break;
                            case MEDIA_TYPE_VIDEO:
                                mediaFile = new File(path + "VID_" + timestamp + ".mp4");
                                break;
                            default:
                                return null;
                        }

                        Log.d(TAG, "File: " + Uri.fromFile(mediaFile));

                        //5. Return the file's Uri
                        return Uri.fromFile(mediaFile);
                    }else {
                        return null;
                    }
                }

                private boolean isExternalStorageAvaible(){
                    String state = Environment.getExternalStorageState();
                    return state.equals(Environment.MEDIA_MOUNTED);
                }
            };
    /* Handle result of camera actions here */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "Request code: "+requestCode);

        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_PHOTO_REQUEST || requestCode == PICK_VIDEO_REQUEST) {
                if(data==null){
                    Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
                    return;
                }else {
                    mMediaUri = data.getData();
                    Log.i(TAG, "Media URI: "+mMediaUri);

                }
                if ( requestCode == PICK_VIDEO_REQUEST ) {
                    //make sure the file is less than 10MB
                    int fileSize = 0;
                    InputStream inputStream = null;

                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();

                    } catch (java.io.FileNotFoundException e) {
                        Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
                        return;
                    }catch (IOException e){
                        Toast.makeText(this, R.string.error_opening_file, Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        try { inputStream.close(); }
                        catch (IOException e) { }
                    }

                    if (fileSize>=FILE_SIZE_LIMIT){
                        Toast.makeText(this, R.string.error_file_size_too_large, Toast.LENGTH_LONG).show();
                        return;

                    }
                }
            }
            else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
                //add it to the Gallery
            }
        }

        else if(requestCode != RESULT_CANCELED){
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        // Get the user cached on disk if present
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            // If no one is logged in, go to the login screen
            navigateToLogin();
        } else {
            Log.i(TAG, currentUser.getUsername());
        }


        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();

        // ***This is where setNavigationMode has been deprecated***


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (itemId){
            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;
            case R.id.action_edit_friends:
                Intent intent = new Intent(this, EditFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_camera:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(R.array.camera_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();

        }

        return super.onOptionsItemSelected(item);
    }


    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}