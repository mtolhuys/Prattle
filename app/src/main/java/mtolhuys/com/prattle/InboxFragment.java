package mtolhuys.com.prattle;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mtolhuys on 14/02/15.
 */
public class InboxFragment extends ListFragment {

    public static final String TAG = InboxFragment.class.getSimpleName();

    protected List<Message> mMessages;
    protected ParseUser mCurrentUser;
    protected List<String> mContactNames;
    protected List<String> mContactIds;
    protected ListView mListView;
    protected int mPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        if (ParseUser.getCurrentUser() == null) {
            goToLogin();
        }
        else {
            mCurrentUser = ParseUser.getCurrentUser();
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView = getListView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ParseUser.getCurrentUser() == null) {
            goToLogin();
        }
        else {
            updateList();
        }
    }

    private void updateList() {
        ParseQuery<Message> toMe = ParseQuery.getQuery(Message.class);
        ParseQuery<Message> toYou = ParseQuery.getQuery(Message.class);
        // Configure limit and sort order
        toMe.whereEqualTo(ParseConstants.KEY_RECIPIENT_ID, mCurrentUser.getObjectId());
        toYou.whereEqualTo(ParseConstants.KEY_SENDER_ID, mCurrentUser.getObjectId());
        // Construct query to execute
        List<ParseQuery<Message>> combined = new ArrayList<ParseQuery<Message>>();
        combined.add(toMe);
        combined.add(toYou);
        ParseQuery<Message> chat = ParseQuery.or(combined);
        chat.setLimit(ParseConstants.MAX_CHAT_MESSAGES_TO_SHOW);
        chat.orderByDescending(ParseConstants.KEY_CREATED_AT);
        chat.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, ParseException e) {
                if (e == null) {
                    // We found messages!
                    mMessages = messages;

                    mContactNames = new ArrayList<>();
                    mContactIds = new ArrayList<>();

                    int i = 0;
                    for (ParseObject message : mMessages) {
                        if (!mContactIds.contains(message.getString(ParseConstants.KEY_RECIPIENT_ID)) &&
                                !mContactIds.contains(message.getString(ParseConstants.KEY_SENDER_ID)))
                        {
                            if (message.getString(ParseConstants.KEY_RECIPIENT_ID)
                                    .equals(mCurrentUser.getObjectId())) {
                                mContactIds.add(message.getString(ParseConstants.KEY_SENDER_ID));
                            } else {
                                mContactIds.add(message.getString(ParseConstants.KEY_RECIPIENT_ID));
                            }
                            if (message.getString(ParseConstants.KEY_RECIPIENT_NAME)
                                    .equals(ParseUser.getCurrentUser().getUsername())) {
                                mContactNames.add(message.getString(ParseConstants.KEY_SENDER_NAME));
                            } else {
                                mContactNames.add(message.getString(ParseConstants.KEY_RECIPIENT_NAME));
                            }
                        }
                        i++;
                    }
                    //MessageAdapter adapter = new MessageAdapter(mListView.getContext(), mMessages);
                    ArrayAdapter adapter = new ArrayAdapter<>(
                            getListView().getContext(),
                            android.R.layout.simple_list_item_1,
                            mContactNames);
                    setListAdapter(adapter);
                }
            }
        });

//        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
//        query.whereEqualTo(ParseConstants.KEY_RECIPIENT_ID, ParseUser.getCurrentUser().getObjectId());
//        query.whereEqualTo(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
//        query.orderByDescending(ParseConstants.KEY_CREATED_AT);
//        query.findInBackground(new FindCallback<ParseObject>() {
//            @Override
//            public void done(List<ParseObject> messages, ParseException e) {
//
//                if (e == null) {
//                    // We found messages!
//                    mMessages = messages;
//
//                    String[] usernames = new String[mMessages.size()];
//                    int i = 0;
//                    for (ParseObject message : mMessages) {
//                        if (message.getString(ParseConstants.KEY_RECIPIENT_NAME)
//                                .equals(ParseUser.getCurrentUser().getUsername())) {
//                            usernames[i] = message.getString(ParseConstants.KEY_SENDER_NAME);
//                        } else {
//                            usernames[i] = message.getString(ParseConstants.KEY_RECIPIENT_NAME);
//                        }
//                        i++;
//                    }
//                    //MessageAdapter adapter = new MessageAdapter(mListView.getContext(), mMessages);
//                    ArrayAdapter adapter = new ArrayAdapter<>(
//                            getListView().getContext(),
//                            android.R.layout.simple_list_item_1,
//                            usernames);
//                    setListAdapter(adapter);
//                }
//            }
//        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        mPosition = position;

        Chat();

//        ParseObject message = mMessages.get(position);
//        String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);
//        ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
//        Uri fileUri = Uri.parse(file.getUrl());
//
//        if (messageType.equals(ParseConstants.TYPE_IMAGE)) {
//            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
//            intent.setData(fileUri);
//            startActivity(intent);
//        }
//        else {
//
//        }
    }

    private void Chat() {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("contactId", mContactIds.get(mPosition));
        intent.putExtra("contactName", mContactNames.get(mPosition));
        startActivity(intent);
    }

    private void goToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
