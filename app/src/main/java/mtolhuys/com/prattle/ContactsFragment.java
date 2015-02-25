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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;

/**
 * Created by mtolhuys on 14/02/15.
 */
public class ContactsFragment extends ListFragment {

    protected List<ParseObject> mContacts;
    protected int mPosition;
    protected ListView mListView;
    protected ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS);
        query.orderByAscending(ParseConstants.KEY_SENDER_NAME);
        query.whereContains(ParseConstants.KEY_RECIPIENT_ID, ParseUser.getCurrentUser().getObjectId());
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
                        i++;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            getListView().getContext(),
                            android.R.layout.simple_list_item_1,
                            usernames);
                    setListAdapter(adapter);
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);

        mPosition = position;
        mListView = l;

            final String[] items = new String[]{getString(R.string.send_message),
                    getString(R.string.delete_contact)};
            final Integer[] icons = new Integer[]{R.drawable.ic_action_email_dark,
                    R.drawable.ic_action_discard};
            ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    mDialogListener.onClick(dialog, item);
                }
            }).show();

    }

    protected DialogInterface.OnClickListener mDialogListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0: // Send Message

                            break;
                        case 1: // Delete Contact
                            deleteContact();
                            break;
                }
            }
    };

    private void deleteContact() {

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.update_contacts));
        mProgressDialog.show();

        mContacts.get(mPosition).deleteInBackground(new DeleteCallback() {

            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(getActivity(), "Delete Contact Failed", Toast.LENGTH_LONG).show();
                }
            }
        });

        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS);
        query.orderByAscending(ParseConstants.KEY_SENDER_NAME);
        query.whereContains(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> contacts, ParseException e) {

                if (e == null && contacts != null) {
                    mContacts = contacts;

                    String[] usernames = new String[mContacts.size()];

                    int i = 0;
                    for (ParseObject contact : mContacts) {
                        usernames[i] = contact.getString(ParseConstants.KEY_SENDER_NAME);
                        i++;
                    }

                    mContacts.get(mPosition).deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Toast.makeText(getActivity(), "Delete Contact Failed", Toast.LENGTH_LONG).show();
                            }
                            Toast.makeText(getActivity(), "Deleted Contact!", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                            getActivity().recreate();
                        }
                    });
                }
            }
        });
    }
}
