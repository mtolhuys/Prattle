package mtolhuys.com.prattle;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class RecipientsActivity extends ListActivity {

    public static final String TAG = RecipientsActivity.class.getSimpleName();

    protected ParseObject mContact;
    protected String mCurrentUserName;
    protected String mCurrentUserId;
    protected List<String> mContactIds;
    protected List<String> mContactNames;
    protected ParseRelation<ParseUser> mContactRelation;
    protected ParseUser mCurrentUser;
    protected List<ParseObject> mContacts;
    protected ProgressDialog mProgressDialog;
    protected Uri mMediaUri;
    protected String mFileType;
    protected int mPosition;
    protected MenuItem mSendMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getString(R.string.font_family))
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_recipients);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(android.R.color.transparent);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);

        mCurrentUserName = ParseUser.getCurrentUser().getUsername();
        mCurrentUserId = ParseUser.getCurrentUser().getObjectId();
    }

    @Override
    public void onResume() {

        super.onResume();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(this.getString(R.string.loading_account));
        mProgressDialog.show();

        mCurrentUser = ParseUser.getCurrentUser();
        mContactRelation = mCurrentUser.getRelation(ParseConstants.KEY_CONTACT_RELATION);

        ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS)
                .setLimit(1000)
                .orderByAscending(ParseConstants.KEY_SENDER_NAME)
                .whereEqualTo(ParseConstants.KEY_USERS_IDS, ParseUser.getCurrentUser().getObjectId())
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> contacts, ParseException e) {
                        mProgressDialog.dismiss();

                        if (e == null && contacts != null) {
                            // We found messages!
                            mContacts = contacts;

                            mContactIds = new ArrayList<>();
                            mContactNames = new ArrayList<>();
                            int i = 0;

                            for (i = 0; i < mContacts.size(); i++) {
                                mContact = mContacts.get(i);

                                if (mContact.getBoolean(ParseConstants.KEY_CONTACT_STATUS)) {

                                    if (mContact.getString(ParseConstants.KEY_SENDER_NAME)
                                            .equals(mCurrentUserName)) {
                                        mContactNames.add(mContact.getString(ParseConstants.KEY_RECIPIENT_NAME));
                                    }
                                    else {
                                        mContactNames.add(mContact.getString(ParseConstants.KEY_SENDER_NAME));
                                    }

                                    if (mContact.getList(ParseConstants.KEY_USERS_IDS).get(0)
                                            .equals(mCurrentUserId)) {
                                        mContactIds.add(mContact.getList(ParseConstants.KEY_USERS_IDS).get(1).toString());
                                    }
                                    else {
                                        mContactIds.add(mContact.getList(ParseConstants.KEY_USERS_IDS).get(0).toString());
                                    }
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                        getListView().getContext(),
                                        android.R.layout.simple_list_item_checked,
                                        mContactNames);
                                setListAdapter(adapter);
                            }
                        }
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_recipients, menu);
        mSendMenuItem = menu.getItem(0);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button_right, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_send:
                ParseObject message = createMessage();
                if (message == null) {
                    AlertDialogs.selectedFileAlert(this);
                }
                else {
                    send(message);
                    finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_ID, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes == null) {
            return null;
        }
        else {
            if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
            ParseFile file = new ParseFile(fileName, fileBytes);
            message.put(ParseConstants.KEY_FILE, file);

            return message;
        }
    }

    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<String>();
        for (int i = 0; i < getListView().getCount(); i++) {
            if (getListView().isItemChecked(i)) {
                recipientIds.add(mContactIds.get(i));
            }
        }
        return recipientIds;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
//       super.onListItemClick(l, v, position, id);      <-----  preventing NullPointerException??

        mPosition = position;

        if (l.getCheckedItemCount() > 0) {
            mSendMenuItem.setVisible(true);
        }
        else {
            mSendMenuItem.setVisible(false);
        }
    }

    protected void send(ParseObject message) {
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(RecipientsActivity.this, getString(R.string.message_sent)
                                                          , Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialogs.sendMessageErrorAlert(RecipientsActivity.this);
                }
            }
        });
    }
}
