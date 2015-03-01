package mtolhuys.com.prattle;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
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
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mtolhuys on 14/02/15.
 */
public class ContactsFragment extends ListFragment {

    public static final String TAG = ContactsFragment.class.getSimpleName();

    protected List<ParseObject> mContactsList;
    protected ParseObject mContact;
    protected String mCurrentUserId;
    protected String mCurrentUserName;
    protected List<String> mContactIds;
    protected List<String> mContactNames;
    protected int mPosition;
    protected ListView mListView;
    protected ProgressDialog mProgressDialog;
    protected ArrayAdapter<String> mArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (ParseUser.getCurrentUser() == null) {
            goToLogin();
        } else {
            Log.i(TAG, ParseUser.getCurrentUser().getUsername());
        }

        mCurrentUserName = ParseUser.getCurrentUser().getUsername();
        mCurrentUserId = ParseUser.getCurrentUser().getObjectId();

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.update_contacts));

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        updateList();

    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {

        ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS)
                .setLimit(1000)
                .orderByAscending(ParseConstants.KEY_SENDER_NAME)
                .whereEqualTo(ParseConstants.KEY_USERS_IDS, ParseUser.getCurrentUser().getObjectId())
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> contacts, ParseException e) {

                        if (e == null && contacts != null) {
                            // We found contacts!
                            mContactsList = contacts;

                            mContactIds = new ArrayList<>();
                            mContactNames = new ArrayList<>();

                            int i;

                            for (i = 0; i < mContactsList.size(); i++) {
                                mContact = mContactsList.get(i);
                                // Find true status items, if true it's a contact of yours
                                if (mContact.getBoolean(ParseConstants.KEY_CONTACT_STATUS)) {
                                    if (mContact.getString(ParseConstants.KEY_RECIPIENT_NAME)
                                            .equals(mCurrentUserName)) {
                                        mContactNames.add(mContact.getString(ParseConstants.KEY_SENDER_NAME));
                                    }
                                    if (mContact.getString(ParseConstants.KEY_SENDER_NAME)
                                            .equals(mCurrentUserName)) {
                                        mContactNames.add(mContact.getString(ParseConstants.KEY_RECIPIENT_NAME));
                                    }
                                }
                                // Find false status items (meaning it's a request), only return sender name
                                if (!mContact.getBoolean(ParseConstants.KEY_CONTACT_STATUS)) {
                                    if (mContact.getString(ParseConstants.KEY_RECIPIENT_NAME)
                                            .equals(mCurrentUserName)) {
                                        mContactNames.add(mContact.getString(ParseConstants.KEY_SENDER_NAME));
                                    }
                                }
                                if (mContact.getList(ParseConstants.KEY_USERS_IDS).get(0)
                                        .equals(mCurrentUserId)) {
                                    mContactIds.add(mContact.getList(ParseConstants.KEY_USERS_IDS).get(1).toString());
                                }
                                if (mContact.getList(ParseConstants.KEY_USERS_IDS).get(1)
                                        .equals(mCurrentUserId)) {
                                    mContactIds.add(mContact.getList(ParseConstants.KEY_USERS_IDS).get(0).toString());
                                }
                            }

                            mArrayAdapter = new ArrayAdapter<>(
                                    getListView().getContext(),
                                    android.R.layout.simple_list_item_checked,
                                    mContactNames);
                            setListAdapter(mArrayAdapter);
                            updateCheckMarks();

                        }
                        else {
                            Log.e(TAG, e.getMessage());
                            AlertDialog.Builder builder = new AlertDialog.Builder(getListView().getContext());
                            builder.setMessage(e.getMessage())
                                    .setTitle(R.string.oops_title)
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });

    }

    private void updateCheckMarks() {

        for (int i = 0; i < mContactNames.size(); i++) {
            getListView().setItemChecked(i, mContactsList.get(i).getBoolean(ParseConstants.KEY_CONTACT_STATUS));
        }

    }

//    @Override
//    public void onResume() {
//        super.onResume();
//
//        ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS)
//                .orderByAscending(ParseConstants.KEY_SENDER_NAME)
//                .whereEqualTo(ParseConstants.KEY_USERS_IDS, ParseUser.getCurrentUser().getObjectId())
//                .findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> contacts, ParseException e) {
//
//                if (e == null && contacts != null) {
//                    // We found contacts!
//                    mContactsList = contacts;
//
//                    String[] usernames = new String[mContactsList.size()];
//
//                    int i = 0;
//                    for (ParseObject contact : mContactsList) {
//                        if (contact.getString(ParseConstants.KEY_RECIPIENT_NAME)
//                                .equals(ParseUser.getCurrentUser().getUsername())) {
//                            usernames[i] = contact.getString(ParseConstants.KEY_SENDER_NAME);
//                        }
//                        else if (contact.getString(ParseConstants.KEY_SENDER_NAME)
//                                .equals(ParseUser.getCurrentUser().getUsername())) {
//                            usernames[i] = contact.getString(ParseConstants.KEY_RECIPIENT_NAME);
//                        }
//                        i++;
//                    }
//                    mArrayAdapter = new ArrayAdapter<String>(
//                            getListView().getContext(),
//                            android.R.layout.simple_list_item_1,
//                            usernames);
//                    mArrayAdapter.setNotifyOnChange(true);
//                    setListAdapter(mArrayAdapter);
//                }
//            }
//        });
//    }

    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);

        mPosition = position;
        mListView = l;

        if (getListView().isItemChecked(mPosition)) {
            final String[] items = new String[]{getString(R.string.add_contact),
                    getString(R.string.delete_request)};
            final Integer[] icons = new Integer[]{R.drawable.ic_action_add_person,
                    R.drawable.ic_action_discard};

            ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    requestListener.onClick(dialog, item);
                }
            }).show();
        }

        if (!getListView().isItemChecked(mPosition)) {
            getListView().setItemChecked(mPosition, true);
            final String[] items = new String[]{getString(R.string.send_message),
                    getString(R.string.delete_contact)};
            final Integer[] icons = new Integer[]{R.drawable.ic_action_email_dark,
                    R.drawable.ic_action_discard};

            ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    contactListener.onClick(dialog, item);
                }
            }).show();
        }

        updateList();

    }

    protected DialogInterface.OnClickListener requestListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0: // Add Contact
                            addContact();
                            updateList();
                            break;
                        case 1: // Delete Request
                            delete();
                            break;
                    }
                }
            };

    protected DialogInterface.OnClickListener contactListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                        case 0: // Send Message

                            break;
                        case 1: // Delete Contact
                            delete();
                            break;
                }
            }
    };

    private void addContact() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.are_you_sure))
                .setMessage(getString(R.string.add_contact_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        mProgressDialog.show();

                        ParseObject confirmTo = new ParseObject(ParseConstants.CLASS_CONTACTS);
                        confirmTo.put(ParseConstants.KEY_SENDER_NAME, mContactNames.get(mPosition));
                        confirmTo.put(ParseConstants.KEY_RECIPIENT_NAME, ParseUser.getCurrentUser().getUsername());
                        confirmTo.add(ParseConstants.KEY_USERS_IDS, mContactIds.get(mPosition));
                        confirmTo.add(ParseConstants.KEY_USERS_IDS, ParseUser.getCurrentUser().getObjectId());
                        confirmTo.put(ParseConstants.KEY_CONTACT_STATUS, true);
                        confirmTo.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(getActivity(),
                                            "Added Contact!",
                                            Toast.LENGTH_SHORT).show();
                                    mContactsList.get(mPosition).deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            mArrayAdapter.notifyDataSetChanged();
                                            mProgressDialog.dismiss();
                                            updateList();
                                        }
                                    });
                                } else {
                                    Toast.makeText(getActivity(),
                                            "Adding Recipient Info Failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(getString(R.string.no_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {

                                mProgressDialog.show();

                                mContactsList.get(mPosition).deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        mProgressDialog.dismiss();
                                        updateList();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void delete() {

        mProgressDialog.show();

        mContactsList.get(mPosition).deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.delete_success),
                            Toast.LENGTH_SHORT).show();
                    updateList();
                }
                if (e != null) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.delete_failed),
                            Toast.LENGTH_SHORT).show();
                    updateList();
                }
            }
        });

        mProgressDialog.dismiss();

    }

    private void goToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
