package mtolhuys.com.prattle;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;


public class EditContactsActivity extends ListActivity {

    public static final String TAG = EditContactsActivity.class.getSimpleName();
    protected String mCurrentUser = ParseUser.getCurrentUser().getUsername();

    protected List<ParseUser> mUsers;
    protected ProgressBar mProgressBar;
    protected EditText mSearchField;
    protected TextView mNoResult;
    protected ImageButton mSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contacts);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mNoResult = (TextView) findViewById(R.id.noResultLabel);

        mProgressBar.setVisibility(View.INVISIBLE);
        mNoResult.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {

        super.onResume();

        mSearchField = (EditText) findViewById(R.id.searchUser);
        mSearchButton = (ImageButton) findViewById(R.id.searchButton);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressBar.setVisibility(View.VISIBLE);
                mNoResult.setVisibility(View.INVISIBLE);

                final String searchItem = mSearchField.getText().toString();

                final ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.orderByAscending(ParseConstants.KEY_USERNAME);
                query.setLimit(100);
                query.whereContains(ParseConstants.KEY_USERNAME, searchItem);
                query.whereNotEqualTo(ParseConstants.KEY_USERNAME, mCurrentUser);
                query.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> users, ParseException e) {

                        mProgressBar.setVisibility(View.INVISIBLE);

                        if (searchItem.isEmpty()) {
                            noSearchItemAlert();
                        }
                        else if (searchItem.equals(mCurrentUser)) {
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
                        } else {
                            Log.e(TAG, e.getMessage());
                            exceptionErrorAlert(e);
                        }
                    }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
