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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class EditContactsActivity extends ListActivity {

    public static final String TAG = EditContactsActivity.class.getSimpleName();

    protected String mCurrentUserName = ParseUser.getCurrentUser().getUsername();
    protected List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> mContactRelation;
    protected ParseUser mCurrentUser;
    protected ProgressBar mProgressBar;
    protected ProgressDialog mProgressDialog;
    protected EditText mSearchField;
    protected TextView mNoResult;
    protected ImageButton mSearchButton;
    protected ListView mListView;
    protected String mUserIds;
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
    }

    @Override
    protected void onResume() {

        super.onResume();

        mCurrentUser = ParseUser.getCurrentUser();
        mContactRelation = mCurrentUser.getRelation(ParseConstants.KEY_CONTACT_RELATION);

        mSearchField = (EditText) findViewById(R.id.searchUser);
        mSearchButton = (ImageButton) findViewById(R.id.searchButton);

        mSearchField.setImeActionLabel(getString(R.string.search_key_label), KeyEvent.KEYCODE_ENTER);
        mSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                updateList();
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

    private void updateList() {
        mProgressBar.setVisibility(View.VISIBLE);
        mNoResult.setVisibility(View.INVISIBLE);

        final String searchItem = mSearchField.getText().toString().trim();

        final ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        query.setLimit(1000);
        query.whereContains(ParseConstants.KEY_USERNAME, searchItem);
        query.whereNotEqualTo(ParseConstants.KEY_USERNAME, mCurrentUserName);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {

                mProgressBar.setVisibility(View.INVISIBLE);

                if (searchItem.isEmpty()) {
                    AlertDialogs.noSearchItemAlert(EditContactsActivity.this);
                } else if (users.isEmpty() && searchItem.equals(mCurrentUserName)) {
                    AlertDialogs.sameAsSearchItemAlert(EditContactsActivity.this);
                    setListAdapter(null);
                } else if (users.isEmpty() && !searchItem.equals(mCurrentUserName)) {
                    mNoResult.setVisibility(View.VISIBLE);
                    setListAdapter(null);
                } else if (e == null) {
                    mUsers = users;
                    String[] usernames = new String[mUsers.size()];
                    int i = 0;
                    for (ParseUser user : mUsers) {
                        usernames[i] = user.getUsername();
                        i++;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            EditContactsActivity.this,
                            android.R.layout.simple_list_item_checked,
                            usernames);
                    setListAdapter(adapter);

                    addContactCheckmarks();
                } else {
                    Log.e(TAG, e.getMessage());
                    exceptionErrorAlert(e);
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void addContactCheckmarks() {

        ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS)
                .orderByAscending(ParseConstants.KEY_SENDER_NAME)
                .whereContains(ParseConstants.KEY_RECIPIENT_ID, ParseUser.getCurrentUser().getObjectId())
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> contacts, ParseException e) {
                        if (e == null) {
                            // List returned, look for matches
                            String[] contactIds = new String[contacts.size()];
                            String[] userIds = new String[mUsers.size()];
                            mListView = getListView();

                            if (contacts.size() > 0) {

                                for (int i = 0; i < contacts.size(); i++) {
                                    ParseObject contact = contacts.get(i);
                                    contactIds[i] = contact.getString(ParseConstants.KEY_SENDER_NAME);
                                }
                                for (int i = 0; i < mUsers.size(); i++) {
                                    userIds[i] = mUsers.get(i).getUsername();
                                }
                                int i = 0;
                                for (String user : userIds) {
                                    if (Arrays.asList( contactIds).contains(user)) {
                                        mListView.setItemChecked(i, true);
                                    }
                                    i++;
                                }
                            }
                            else {
                                return;
                            }
                        }
                        else {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });

        ParseQuery.getQuery(ParseConstants.CLASS_ADD_REQUESTS)
                .orderByAscending(ParseConstants.KEY_SENDER_NAME)
                .whereContains(ParseConstants.KEY_REQUEST_FROM, ParseUser.getCurrentUser().getObjectId())
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> requests, ParseException e) {
                        if (e == null) {
                            // List returned, look for matches
                            String[] requestsIds = new String[requests.size()];
                            String[] usersIds = new String[mUsers.size()];
                            mListView = getListView();

                            if (requests.size() > 0) {

                                for (int i = 0; i < requests.size(); i++) {
                                    ParseObject contact = requests.get(i);
                                    requestsIds[i] = contact.getString(ParseConstants.KEY_REQUEST_TO);
                                }
                                for (int i = 0; i < mUsers.size(); i++) {
                                    usersIds[i] = mUsers.get(i).getObjectId();
                                }
                                int i = 0;
                                for (String user : usersIds) {
                                    if (Arrays.asList(requestsIds).contains(user)) {
                                        mListView.setItemChecked(i, true);
                                    }
                                    i++;
                                }
                            }
                            else {
                                return;
                            }
                        }
                        else {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });

        ParseQuery.getQuery(ParseConstants.CLASS_ADD_REQUESTS)
                .orderByAscending(ParseConstants.KEY_SENDER_NAME)
                .whereContains(ParseConstants.KEY_REQUEST_TO, ParseUser.getCurrentUser().getObjectId())
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> requests, ParseException e) {
                        if (e == null) {
                            // List returned, look for matches
                            String[] requestsIds = new String[requests.size()];
                            String[] usersIds = new String[mUsers.size()];
                            mListView = getListView();

                            if (requests.size() > 0) {

                                for (int i = 0; i < requests.size(); i++) {
                                    ParseObject contact = requests.get(i);
                                    requestsIds[i] = contact.getString(ParseConstants.KEY_REQUEST_FROM);
                                }
                                for (int i = 0; i < mUsers.size(); i++) {
                                    usersIds[i] = mUsers.get(i).getObjectId();
                                }
                                int i = 0;
                                for (String user : usersIds) {
                                    if (Arrays.asList(requestsIds).contains(user)) {
                                        mListView.setItemChecked(i, true);
                                    }
                                    i++;
                                }
                            }
                            else {
                                return;
                            }
                        }
                        else {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);

        mPosition = position;
        mListView = l;

        mProgressDialog = new ProgressDialog(EditContactsActivity.this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.update_contacts));

        if (getListView().isItemChecked(mPosition)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.are_you_sure))
                    .setMessage(getString(R.string.add_contact_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            mProgressDialog.show();

                            ParseObject addRequest = new ParseObject(ParseConstants.CLASS_ADD_REQUESTS);
                            addRequest.put(ParseConstants.KEY_REQUEST_FROM, ParseUser.getCurrentUser().getObjectId());
                            addRequest.put(ParseConstants.KEY_REQUEST_TO, mUsers.get(mPosition).getObjectId());
                            addRequest.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
                            addRequest.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(EditContactsActivity.this,
                                                "Contact Request Sent",
                                                Toast.LENGTH_SHORT).show();
                                        mProgressDialog.dismiss();
                                    }
                                    else {
                                        Toast.makeText(EditContactsActivity.this,
                                                "Contact Request Failed",
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
                            deleteContactOrRequest();
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

    private void deleteContactOrRequest() {

        ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS)
                .setLimit(1000)
                .orderByAscending(ParseConstants.KEY_SENDER_NAME)
                .whereContains(ParseConstants.KEY_RECIPIENT_ID, ParseUser.getCurrentUser().getObjectId())
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> contacts, ParseException e) {

                        if (e == null && contacts != null) {

                            String[] contactsIds = new String[contacts.size()];
                            ParseObject contact;

                            for (int i = 0; i < contacts.size(); i++) {
                                contact = contacts.get(i);
                                contactsIds[i] = contact.getString(ParseConstants.KEY_SENDER_ID);

                                if (Arrays.asList(contactsIds).contains(mUsers.get(mPosition).getObjectId())) {
                                    contact = contacts.get(mPosition);
                                    contact.deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e != null) {
                                                Toast.makeText(EditContactsActivity.this,
                                                        getString(R.string.delete_contact_failed),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });

        ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS)
                .orderByAscending(ParseConstants.KEY_SENDER_NAME)
                .whereContains(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId())
                .findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> contacts, ParseException e) {

                if (e == null && contacts != null) {

                    String[] contactsIds = new String[contacts.size()];
                    ParseObject contact;

                    for (int i = 0; i < contacts.size(); i++) {
                        contact = contacts.get(i);
                        contactsIds[i] = contact.getString(ParseConstants.KEY_RECIPIENT_ID);

                        if (Arrays.asList(contactsIds).contains(mUsers.get(mPosition).getObjectId())) {
                            contact.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Toast.makeText(EditContactsActivity.this,
                                            getString(R.string.deleted_contact),
                                            Toast.LENGTH_SHORT).show();
                                    if (e != null) {
                                        Toast.makeText(EditContactsActivity.this,
                                                getString(R.string.delete_contact_failed),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }

                }
            }
        });

        ParseQuery.getQuery(ParseConstants.CLASS_ADD_REQUESTS)
                .setLimit(1000)
                .whereContains(ParseConstants.KEY_REQUEST_FROM, ParseUser.getCurrentUser().getObjectId())
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> requests, ParseException e) {
                        if (e == null && requests != null) {

                            String[] requestsIds = new String[requests.size()];
                            ParseObject request;

                            for (int i = 0; i < requests.size(); i++) {
                                request = requests.get(i);
                                requestsIds[i] = request.getString(ParseConstants.KEY_REQUEST_TO);

                                if (Arrays.asList(requestsIds).contains(mUsers.get(mPosition).getObjectId())) {
                                    for (int j = 0; j < mUsers.size(); j++) {
                                        mUserIds = mUsers.get(mPosition).getObjectId();
                                    }
                                    if (mUserIds.equals(request.get(ParseConstants.KEY_REQUEST_TO))) {
                                        request.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                Toast.makeText(EditContactsActivity.this,
                                                        getString(R.string.request_deleted),
                                                        Toast.LENGTH_SHORT).show();
                                                if (e != null) {
                                                    Toast.makeText(EditContactsActivity.this,
                                                            getString(R.string.delete_request_failed),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                });

        ParseQuery.getQuery(ParseConstants.CLASS_ADD_REQUESTS)
                .setLimit(1000)
                .whereContains(ParseConstants.KEY_REQUEST_TO, ParseUser.getCurrentUser().getObjectId())
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> requests, ParseException e) {
                        if (e == null && requests != null) {

                            String[] requestsIds = new String[requests.size()];
                            ParseObject request;

                            for (int i = 0; i < requests.size(); i++) {
                                request = requests.get(i);
                                requestsIds[i] = request.getString(ParseConstants.KEY_REQUEST_FROM);

                                if (Arrays.asList(requestsIds).contains(mUsers.get(mPosition).getObjectId())) {
                                    for (int j = 0; j < mUsers.size(); j++) {
                                        mUserIds = mUsers.get(mPosition).getObjectId();
                                    }
                                    if (mUserIds.equals(request.get(ParseConstants.KEY_REQUEST_FROM))) {
                                        request.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                Toast.makeText(EditContactsActivity.this,
                                                        getString(R.string.request_deleted),
                                                        Toast.LENGTH_SHORT).show();
                                                if (e != null) {
                                                    Toast.makeText(EditContactsActivity.this,
                                                            getString(R.string.delete_request_failed),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                });

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