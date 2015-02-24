package mtolhuys.com.prattle;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by mtolhuys on 14/02/15.
 */
public class RequestsFragment extends ListFragment {

    protected List<ParseObject> mContacts;
    protected ParseObject mParseObject;
    protected String mUserId;
    protected String mUsername;
    protected int mPosition;
    protected ListView mListView;
    protected ProgressDialog mProgressDialog;
    protected ArrayAdapter<String> mArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_requests, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_ADD_REQUESTS);
        query.orderByAscending(ParseConstants.KEY_SENDER_NAME);
        query.whereEqualTo(ParseConstants.KEY_REQUEST_TO, ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> contacts, ParseException e) {

                if (e == null && contacts != null) {
                    // We found messages!
                    mContacts = contacts;

                    String[] usernames = new String[mContacts.size()];
                    int i = 0;
                    for (ParseObject contact : mContacts) {
                        usernames[i] = contact.getString(ParseConstants.KEY_SENDER_NAME);
                        mUserId = contact.getString(ParseConstants.KEY_REQUEST_FROM);
                        mUsername = contact.getString(ParseConstants.KEY_SENDER_NAME);
                        mParseObject = contact;
                        i++;
                    }
                    mArrayAdapter = new ArrayAdapter<String>(
                            getListView().getContext(),
                            android.R.layout.simple_list_item_1,
                            usernames);
                    setListAdapter(mArrayAdapter);
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);

        mPosition = position;
        mListView = l;

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.update_contacts));

        if (!getListView().isItemChecked(position)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.are_you_sure))
                    .setMessage(getString(R.string.add_contact_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            mProgressDialog.show();

                            ParseObject confirmTo = new ParseObject(ParseConstants.CLASS_CONTACTS);
                            confirmTo.put(ParseConstants.KEY_RECIPIENT_NAME, mUsername);
                            confirmTo.put(ParseConstants.KEY_RECIPIENT_ID, mUserId);
                            confirmTo.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
                            confirmTo.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
                            confirmTo.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(getActivity(),
                                                "Added Contact!",
                                                Toast.LENGTH_SHORT).show();
                                        mParseObject.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                mProgressDialog.dismiss();
                                                getActivity().recreate();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(getActivity(),
                                                "Adding Recipient Info Failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            ParseObject confirmFrom = new ParseObject(ParseConstants.CLASS_CONTACTS);
                            confirmFrom.put(ParseConstants.KEY_RECIPIENT_NAME, ParseUser.getCurrentUser().getUsername());
                            confirmFrom.put(ParseConstants.KEY_RECIPIENT_ID, ParseUser.getCurrentUser().getObjectId());
                            confirmFrom.put(ParseConstants.KEY_SENDER_NAME, mUsername);
                            confirmFrom.put(ParseConstants.KEY_SENDER_ID, mUserId);
                            confirmFrom.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Toast.makeText(getActivity(),
                                                "Adding Sender Info Failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
//                            mContactRelation.add(mUsers.get(mPosition));
//                            saveInBackground();
                        }
                    })
                    .setNegativeButton(getString(R.string.no_button),
                            new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, int id) {

                                    mProgressDialog.show();

                                    mParseObject.deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            mProgressDialog.dismiss();
                                            getActivity().recreate();
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
