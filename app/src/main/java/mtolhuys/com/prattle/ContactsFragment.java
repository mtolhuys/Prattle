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
import java.util.Collections;
import java.util.List;

/**
 * Created by mtolhuys on 14/02/15.
 */
public class ContactsFragment extends ListFragment {

    public static final String TAG = ContactsFragment.class.getSimpleName();

    protected List<ContactsObject> mContactObjects;
    protected ContactsObject mContactObject;
    protected List<ParseObject> mContactsList;
    protected List<ParseObject> mItem;
    protected List<String> mNames;
    protected List<String> mIds;
    protected ParseObject mListItem;
    protected String mContactName;
    protected String mContactId;
    protected ParseObject mContact;
    protected List<String> mContactNames;
    protected List<String> mContactIds;
    protected List<ParseObject> mContacts;
    protected String mCurrentUserId;
    protected String mCurrentUserName;
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

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(getString(R.string.update_contacts));

        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        if (ParseUser.getCurrentUser() == null) {
            goToLogin();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {

        mProgressDialog.show();

        mCurrentUserName = ParseUser.getCurrentUser().getUsername();
        mCurrentUserId = ParseUser.getCurrentUser().getObjectId();


        ParseQuery.getQuery(ParseConstants.CLASS_CONTACTS)
                .setLimit(1000)
                .whereEqualTo(ParseConstants.KEY_USERS_IDS, ParseUser.getCurrentUser().getObjectId())
                .findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> contacts, ParseException e) {

                        if (e == null && contacts != null) {
                            // We found contacts!
                            mContactsList = contacts;

                            mItem = new ArrayList<ParseObject>();
                            mNames = new ArrayList<String>();
                            mIds = new ArrayList<String>();

                            // Filter the query
                            for (int i = 0; i < mContactsList.size(); i++) {
                                mListItem = mContactsList.get(i);

                                // Find true status items, if true it's a contact of yours
                                if (mListItem.getBoolean(ParseConstants.KEY_CONTACT_STATUS)) {
                                    if (mListItem.getString(ParseConstants.KEY_RECIPIENT_NAME)
                                            .equals(mCurrentUserName)) {
                                        mItem.add(mListItem);
                                        mNames.add(mListItem.getString(ParseConstants.KEY_SENDER_NAME));
                                    } else {
                                        mItem.add(mListItem);
                                        mNames.add(mListItem.getString(ParseConstants.KEY_RECIPIENT_NAME));
                                    }
                                    if (mListItem.getList(ParseConstants.KEY_USERS_IDS).get(0)
                                            .equals(mCurrentUserId)) {
                                        mIds.add(mListItem.getList(ParseConstants.KEY_USERS_IDS).get(1).toString());
                                    } else {
                                        mIds.add(mListItem.getList(ParseConstants.KEY_USERS_IDS).get(0).toString());
                                    }
                                }
                                // Find false status items (meaning it's a request),
                                // I want to hide outgoing requests in the contacts list,
                                // therefore only return sender name
                                else {
                                    if (mListItem.getString(ParseConstants.KEY_RECIPIENT_NAME)
                                            .equals(mCurrentUserName)) {
                                        mItem.add(mListItem);
                                        mNames.add(mListItem.getString(ParseConstants.KEY_SENDER_NAME));
                                        if (mListItem.getList(ParseConstants.KEY_USERS_IDS).get(0)
                                                .equals(mCurrentUserId)) {
                                            mIds.add(mListItem.getList(ParseConstants.KEY_USERS_IDS).get(1).toString());
                                        } else {
                                            mIds.add(mListItem.getList(ParseConstants.KEY_USERS_IDS).get(0).toString());
                                        }
                                    }
                                }
                            }
                            // Obtain all items
                            mContactObjects = new ArrayList<ContactsObject>();
                            for (int i = 0; i < mItem.size(); i++) {
                                mContactName = mNames.get(i);
                                mContactId = mIds.get(i);
                                mContact = mItem.get(i);
                                mContactObject = new ContactsObject(mContactName, mContactId, mContact);
                                mContactObjects.add(mContactObject);
                            }
                            // If there are 2 or more Items, Order the list alphabetically.
                            // Since I use 2 name collumns (senders & recipients),
                            // I can't use the ParseQuery orderBy method. Therefore this method...
                            if (mContactObjects.size() >= 2) {
                                Collections.sort(mContactObjects, new ContactsComparator());
                            }
                            mContactNames = new ArrayList<>();
                            mContactIds = new ArrayList<>();
                            mContacts = new ArrayList<>();
                            for (ContactsObject contact : mContactObjects) {
                                mContactNames.add(contact.getContactName());
                                mContactIds.add(contact.getContactId());
                                mContacts.add(contact.getContact());
                            }
                            mArrayAdapter = new ArrayAdapter<>(
                                    getListView().getContext(),
                                    android.R.layout.simple_list_item_checked,
                                    mContactNames);
                            setListAdapter(mArrayAdapter);

                            // Set checkmarks to contacts (where contact-status equals true)
                            for (int i = 0; i < mContacts.size(); i++) {
                                getListView().setItemChecked(i, mContacts.get(i).getBoolean(ParseConstants.KEY_CONTACT_STATUS));
                            }
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

        mProgressDialog.dismiss();

    }

    @Override
    public void onListItemClick
            (ListView l, View v, final int position, long id) {
        super.onListItemClick(l, v, position, id);

        mPosition = position;
        mListView = l;

        // With a request, only options add contact and delete are available
        if (getListView().isItemChecked(mPosition)) {
            final String[] items = new String[]{getString(R.string.add_contact),
                    getString(R.string.delete_request)};
            final Integer[] icons = new Integer[]{R.drawable.ic_add_person_dark,
                    R.drawable.ic_delete_person_dark};

            ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    requestListener.onClick(dialog, item);
                }
            }).show();
        }

        // With a contact, only options send message and delete are available
        if (!getListView().isItemChecked(mPosition)) {
            getListView().setItemChecked(mPosition, true);
            final String[] items = new String[]{getString(R.string.send_message),
                    getString(R.string.delete_contact)};
            final Integer[] icons = new Integer[]{R.drawable.ic_message_dark,
                    R.drawable.ic_delete_person_dark};

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
                    switch (which) {
                        case 0: // Add Contact
                            addContact();
                            updateList();
                            break;
                        case 1: // Delete Request
                            delete();
                            updateList();
                            break;
                    }
                }
            };

    protected DialogInterface.OnClickListener contactListener =
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0: // Send Message

                            break;
                        case 1: // Delete Contact
                            delete();
                            updateList();
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
                                            getString(R.string.added_contact),
                                            Toast.LENGTH_SHORT).show();
                                    mContacts.get(mPosition).deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            mArrayAdapter.notifyDataSetChanged();
                                            mProgressDialog.dismiss();
                                            updateList();
                                        }
                                    });
                                } else {
                                    Toast.makeText(getActivity(),
                                            getString(R.string.adding_contact_failed),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton(getString(R.string.no_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                return;
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void delete() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.are_you_sure))
                .setMessage(getString(R.string.delete_contact_confirmation))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        mProgressDialog.show();
                        if (mContacts.get(mPosition).getBoolean(ParseConstants.KEY_CONTACT_STATUS)) {
                            mContacts.get(mPosition).deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(getActivity(),
                                                getString(R.string.contact_deleted),
                                                Toast.LENGTH_SHORT).show();
                                    } else if (e != null) {
                                        Toast.makeText(getActivity(),
                                                getString(R.string.delete_contact_failed),
                                                Toast.LENGTH_SHORT).show();
                                        getListView().setItemChecked(mPosition, true);
                                    }
                                }
                            });
                        } else {
                            mContacts.get(mPosition).deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(getActivity(),
                                                getString(R.string.request_deleted),
                                                Toast.LENGTH_SHORT).show();
                                    } else if (e != null) {
                                        Toast.makeText(getActivity(),
                                                getString(R.string.delete_request_failed),
                                                Toast.LENGTH_SHORT).show();
                                        getListView().setItemChecked(mPosition, true);
                                    }
                                }
                            });
                        }
                    }
                })
                .setNegativeButton(getString(R.string.no_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                return;
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

        mProgressDialog.dismiss();

    }

    private void goToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
