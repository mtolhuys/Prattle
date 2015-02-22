package mtolhuys.com.prattle;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.LauncherActivity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
    protected int mPosition;
    protected ListView mListView;

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
        query.setLimit(100);
        query.whereContains(ParseConstants.KEY_USERNAME, searchItem);
        query.whereNotEqualTo(ParseConstants.KEY_USERNAME, mCurrentUserName);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {

                mProgressBar.setVisibility(View.INVISIBLE);

                if (searchItem.isEmpty()) {
                    noSearchItemAlert();
                } else if (searchItem.equals(mCurrentUserName)) {
                    sameAsSearchItemAlert();
                    setListAdapter(null);
                } else if (users.isEmpty()) {
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

        mContactRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> contacts, ParseException e) {
                if (e == null) {
                    // list returned, look for match
                    for (int i = 0; i < mUsers.size(); i++) {
                        ParseUser user = mUsers.get(i);

                        for (ParseUser contact : contacts) {
                            if (contact.getObjectId().equals(user.getObjectId())) {
                                mListView = getListView();
                                mListView.setItemChecked(i, true);
                            }
                        }
                    }
                } else {
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

        if (getListView().isItemChecked(position)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.are_you_sure))
                    .setMessage(getString(R.string.add_contact_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mContactRelation.add(mUsers.get(mPosition));
                            saveInBackground();
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
            builder.setTitle(getString(R.string.are_you_sure))
                    .setMessage(getString(R.string.delete_contact_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mContactRelation.remove(mUsers.get(mPosition));
                            saveInBackground();
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

    private void saveInBackground() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.update_contacts));
        mProgressDialog.show();
        mCurrentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                mProgressDialog.dismiss();
                if (e != null) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void sameAsSearchItemAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.oops_title))
                .setMessage(getString(R.string.search_same_as_user))
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void noSearchItemAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.oops_title))
                .setMessage(getString(R.string.search_no_item_message))
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
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
