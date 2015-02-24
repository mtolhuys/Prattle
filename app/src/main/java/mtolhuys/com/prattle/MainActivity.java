package mtolhuys.com.prattle;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    protected ParseUser mCurrentUser = ParseUser.getCurrentUser();
    protected List<ParseObject> mContacts;

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int PICK_PHOTO_REQUEST = 1;

    public static final int MEDIA_TYPE_IMAGE = 2;

    protected Uri mMediaUri;
    protected ProgressDialog mProgressDialog;

    protected DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0: // Take picture
                            takePicture();
                            break;
                        case 1: // Choose picture
                            Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            choosePhotoIntent.setType("image/*");
                            startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                            break;
                    }
                }
                private void takePicture() {
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputMediaUri(MEDIA_TYPE_IMAGE);
                    if (mMediaUri == null) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.error_external_storage),
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                }

                private Uri getOutputMediaUri(int mediaType) {
                    if (isExternalStorageAvailable()) {
                        // 1. Get the external storage directory
                        String appName = MainActivity.this.getString(R.string.app_name);
                        File mediaStorageDir = new File(Environment
                                                    .getExternalStoragePublicDirectory(Environment
                                                    .DIRECTORY_PICTURES)
                                                    ,appName);

                        // 2. Create our subdirectory
                        if (! mediaStorageDir.exists()) {
                            if (! mediaStorageDir.mkdir()) {
                                Log.e(TAG, getString(R.string.create_directory_fail));
                                return null;
                            }
                        }

                        // 3. Create a filename
                        File mediaFile;
                        Date now = new Date();
                        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(now);

                        // 4. Create the file
                        String path = mediaStorageDir.getPath() + File.separator;
                        if (mediaType == MEDIA_TYPE_IMAGE) {
                            mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
                        }
                        else {
                            return null;
                        }

                        Log.d(TAG, "File: " + Uri.fromFile(mediaFile));

                        // 5. Return the file's URI
                        return Uri.fromFile(mediaFile);
                    }
                    else {

                    }

                    return null;
                }

                private boolean isExternalStorageAvailable() {
                    String state = Environment.getExternalStorageState();

                    if (state.equals(Environment.MEDIA_MOUNTED)) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            };

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
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getString(R.string.font_family))
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_main);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(this.getString(R.string.loading_account));

        ParseAnalytics.trackAppOpened(getIntent());

        if (mCurrentUser == null) {
            goToLogin();
        } else {
            Log.i(TAG, mCurrentUser.getUsername());
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PHOTO_REQUEST) {
                if (data == null) {
                    Toast.makeText(this, getString(R.string.general_error_message)
                                       , Toast.LENGTH_LONG).show();
                }
                else {
                    mMediaUri = data.getData();
                }
            }
            else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
            recipientsIntent.setData(mMediaUri);

            recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, ParseConstants.TYPE_IMAGE);
            startActivity(recipientsIntent);
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, getString(R.string.general_error_message), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                ParseUser.logOut();
                goToLogin();
                break;

            case R.id.action_edit_contacts:
                Intent intent = new Intent(this, EditContactsActivity.class);
                startActivity(intent);
                break;

            case R.id.action_delete_account:
                deleteAccountAlert();
                break;

            case R.id.action_camera:
                final String[] items = new String[]{getString(R.string.camera_label),
                                                    getString(R.string.gallery_label)};
                final Integer[] icons = new Integer[]{R.drawable.ic_action_camera_dark,
                                                      R.drawable.ic_action_collection};
                ListAdapter adapter = new ArrayAdapterWithIcon(this, items, icons);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                mDialogListener.onClick(dialog, item);
                            }
                        }).show();
                break;

            case R.id.action_refresh:
                mProgressDialog.show();
                this.recreate();
                mProgressDialog.dismiss();
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteAccountAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.caution_title))
            .setMessage(getString(R.string.delete_confirmation))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, close
                    // current activity
                    mCurrentUser.deleteInBackground();
                    deleteContact();
                    ParseUser.logOut();
                    Toast.makeText(MainActivity.this, getString(R.string.delete_success), Toast.LENGTH_LONG).show();
                    goToLogin();
                }
            })
            .setNegativeButton(getString(R.string.no_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel();
                }
            });
        AlertDialog alert = builder.create();
        alert.show();
        return;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private void deleteContact() {

        ParseQuery<ParseObject> deleteMyContactList = ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS);
        deleteMyContactList.orderByAscending(ParseConstants.KEY_SENDER_NAME);
        deleteMyContactList.whereContains(ParseConstants.KEY_RECIPIENT_ID, ParseUser.getCurrentUser().getObjectId());
        deleteMyContactList.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> contacts, ParseException e) {

                if (e == null && contacts != null) {

                    for (int i = 0; i < contacts.size(); i++) {
                        ParseObject contact = contacts.get(i);
                        contact.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Toast.makeText(MainActivity.this, "Delete Contact List Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        });

        ParseQuery<ParseObject> deleteMeAtUsersContactList = ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS);
        deleteMeAtUsersContactList.orderByAscending(ParseConstants.KEY_SENDER_NAME);
        deleteMeAtUsersContactList.whereContains(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        deleteMeAtUsersContactList.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> contacts, ParseException e) {

                if (e == null && contacts != null) {

                    for (int i = 0; i < contacts.size(); i++) {
                        ParseObject contact = contacts.get(i);
                        contact.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Toast.makeText(MainActivity.this, "Delete Contact List Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

}
