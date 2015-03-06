package mtolhuys.com.prattle;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class EditContactsActivity extends ListActivity {

    public static final String TAG = EditContactsActivity.class.getSimpleName();

    protected String mCurrentUserName = ParseUser.getCurrentUser().getUsername();
    protected String mCurrentUserId = ParseUser.getCurrentUser().getObjectId();
    protected List<ParseUser> mUsers;
    protected List<ParseObject> mContacts;
    protected ParseRelation<ParseUser> mContactRelation;
    protected ParseUser mCurrentUser;
    protected ParseObject mContact;
    protected ProgressBar mProgressBar;
    protected ProgressDialog mProgressDialog;
    protected EditText mSearchField;
    protected TextView mNoResult;
    protected ImageButton mSearchButton;
    protected ListView mListView;
    protected String[] mContactIds;
    protected ArrayAdapter<String> mAdapter;
    protected int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getString(R.string.font_family))
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_edit_contacts);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(android.R.color.transparent);

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mNoResult = (TextView) findViewById(R.id.noResultLabel);

        mProgressBar.setVisibility(View.INVISIBLE);
        mNoResult.setVisibility(View.INVISIBLE);

        mProgressDialog = new ProgressDialog(EditContactsActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.update_contacts));
    }

    @Override
    protected void onResume() {

        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mContactRelation = mCurrentUser.getRelation(ParseConstants.KEY_CONTACT_RELATION);

        mSearchField = (EditText) findViewById(R.id.searchUser);
        mSearchButton = (ImageButton) findViewById(R.id.searchButton);

        mSearchField.setImeActionLabel(getString(R.string.search), KeyEvent.KEYCODE_ENTER);
        mSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == KeyEvent.ACTION_DOWN) {
                    updateList();
                    return false;
                } else if (actionId == KeyEvent.ACTION_UP) {
                    return false;
                }
                return false;
            }
        });

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList();
            }
        });

    }

    private void search() {
        mProgressBar.setVisibility(View.VISIBLE);
        mNoResult.setVisibility(View.INVISIBLE);

        mProgressDialog.show();

        final String searchItem = mSearchField.getText().toString().trim().toLowerCase();

        ParseUser.getQuery()
                .setLimit(1000)
                .orderByAscending(ParseConstants.KEY_USERNAME)
                .whereContains(ParseConstants.KEY_SEARCH_NAME, searchItem)
                .whereNotEqualTo(ParseConstants.KEY_USERNAME, mCurrentUserName)
                .findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {

                mProgressBar.setVisibility(View.INVISIBLE);

                if (searchItem.isEmpty()) {
                    AlertDialogs.noSearchItemAlert(EditContactsActivity.this);
                }
                else if (users.isEmpty() && searchItem.equals(mCurrentUserName)) {
                    AlertDialogs.sameAsSearchItemAlert(EditContactsActivity.this);
                    setListAdapter(null);
                }
                else if (users.isEmpty() && !searchItem.equals(mCurrentUserName)) {
                    mNoResult.setVisibility(View.VISIBLE);
                    setListAdapter(null);
                }
                else if (e == null) {
                    mUsers = users;
                    String[] usernames = new String[mUsers.size()];
                    int i = 0;
                    for (ParseUser user : mUsers) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
                    mAdapter = new ArrayAdapter<>(
                            EditContactsActivity.this,
                            android.R.layout.simple_list_item_checked,
                            usernames);
                    setListAdapter(mAdapter);
                    // Add Contact Checkmarks
                    for (i = 0; i < mUsers.size(); i++) {
                        if (mContactIds != null) {
                            if (Arrays.asList(mContactIds).contains(mUsers.get(i).getObjectId())) {
                                getListView().setItemChecked(i, true);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                    exceptionErrorAlert(e);
                }
            }
        });

        mProgressDialog.dismiss();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void updateList() {

        ParseQuery contactQuery = ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS);
        contactQuery.setLimit(1000);
        contactQuery.whereEqualTo(ParseConstants.KEY_USERS_IDS, ParseUser.getCurrentUser().getObjectId());
        contactQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> contacts, ParseException e) {
                if (e == null && contacts != null) {

                    mContacts = contacts;

                    mContactIds = new String[mContacts.size()];

                    for (int i = 0; i < mContacts.size(); i++) {
                        mContact = mContacts.get(i);

                        if (mContact.getList(ParseConstants.KEY_USERS_IDS).get(0)
                                .equals(mCurrentUserId)) {
                            mContactIds[i] = mContact.getList(ParseConstants.KEY_USERS_IDS).get(1).toString();
                        } else {
                            mContactIds[i] = mContact.getList(ParseConstants.KEY_USERS_IDS).get(0).toString();
                        }
                    }
                    search();
                }
                else {
                    search();
                }
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);

        mPosition = position;
        mListView = l;

        if (getListView().isItemChecked(mPosition)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.are_you_sure))
                    .setMessage(getString(R.string.add_contact_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            mProgressDialog.show();

                            ParseObject addRequest = new ParseObject(ParseConstants.CLASS_CONTACTS);
                            addRequest.add(ParseConstants.KEY_USERS_IDS, ParseUser.getCurrentUser().getObjectId());
                            addRequest.add(ParseConstants.KEY_USERS_IDS, mUsers.get(mPosition).getObjectId());
                            addRequest.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
                            addRequest.put(ParseConstants.KEY_RECIPIENT_NAME, mUsers.get(mPosition).getUsername());
                            addRequest.put(ParseConstants.KEY_CONTACT_STATUS, false);
                            addRequest.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(EditContactsActivity.this,
                                                getString(R.string.request_sent),
                                                Toast.LENGTH_SHORT).show();
                                        updateList();
                                    }
                                    else {
                                        Toast.makeText(EditContactsActivity.this,
                                                getString(R.string.request_sent_failed),
                                                Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton(getString(R.string.no_button),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    mListView.setItemChecked(mPosition, false);
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.delete_request_contact_title))
                    .setMessage(getString(R.string.delete_request_contact_question))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mProgressDialog.show();
                            delete();
                            updateList();
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(getString(R.string.no_button),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    mListView.setItemChecked(mPosition, true);
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void delete() {

        String deleteId = mUsers.get(mPosition).getObjectId();

        for (int i = 0; i < mContacts.size(); i++) {
            if (Arrays.asList(mContactIds).get(i).contains(deleteId)) {
                if (mContacts.get(i).getBoolean(ParseConstants.KEY_CONTACT_STATUS)) {
                    mContacts.get(i).deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(EditContactsActivity.this,
                                        getString(R.string.contact_deleted),
                                        Toast.LENGTH_SHORT).show();
                            } else if (e != null) {
                                Toast.makeText(EditContactsActivity.this,
                                        getString(R.string.delete_contact_failed),
                                        Toast.LENGTH_SHORT).show();
                                getListView().setItemChecked(mPosition, true);
                            }
                        }
                    });
                }
                else {
                    mContacts.get(i).deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(EditContactsActivity.this,
                                        getString(R.string.request_deleted),
                                        Toast.LENGTH_SHORT).show();
                            } else if (e != null) {
                                Toast.makeText(EditContactsActivity.this,
                                        getString(R.string.delete_request_failed),
                                        Toast.LENGTH_SHORT).show();
                                getListView().setItemChecked(mPosition, true);
                            }
                        }
                    });
                }
            }
        }
        mProgressDialog.dismiss();
    }

    private void exceptionErrorAlert(ParseException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.oops_title))
                .setMessage(e.getMessage())
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}