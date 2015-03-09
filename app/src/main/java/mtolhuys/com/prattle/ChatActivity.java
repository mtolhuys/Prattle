package mtolhuys.com.prattle;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends ActionBarActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();

    public static final String USER_ID_KEY = "userId";

    private Handler handler = new Handler();

    private static String sMyId;
    private static String sMyName;
    private static String sContactId;
    private static String sContactName;

    private ListView lvChat;
    private ArrayList<Message> mMessages;
    private ChatListAdapter mAdapter;

    private EditText etMessage;
    private Button btSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (ParseUser.getCurrentUser() == null) {
            goToLogin();
        }
        else {
            startChat();
        }

        // Run the runnable object defined every 100ms
        //handler.postDelayed(runnable, 100);

    }

    private void startChat() {
        sMyId = ParseUser.getCurrentUser().getObjectId();
        sMyName = ParseUser.getCurrentUser().getUsername();
        sContactId = getIntent().getStringExtra("contactId");
        sContactName = getIntent().getStringExtra("contactName");
        setupMessagePosting();
        receiveMessage();
    }

    // Defines a runnable which is run every 100ms
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            handler.postDelayed(this, 100);
        }
    };

    private void refreshMessages() {
        receiveMessage();
    }

    // Setup message field and posting
    private void setupMessagePosting() {
        etMessage = (EditText) findViewById(R.id.etMessage);
        btSend = (Button) findViewById(R.id.btSend);
        lvChat = (ListView) findViewById(R.id.lvChat);
        mMessages = new ArrayList<Message>();
        mAdapter = new ChatListAdapter(ChatActivity.this, sMyId, mMessages);
        lvChat.setAdapter(mAdapter);
        btSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String body = etMessage.getText().toString();
                // Use Message model to create new messages now
                Message message = new Message();
                message.setSenderId(sMyId);
                message.setSenderName(sMyName);
                message.setRecipientId(sContactId);
                message.setRecipientName(sContactName);
                message.setBody(body);
                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(com.parse.ParseException e) {
                        receiveMessage();
                    }
                });
                etMessage.setText("");
            }
        });
    }

    // Query messages from Parse so we can load them into the chat adapter
    private void receiveMessage() {
        ParseQuery<Message> toMe = ParseQuery.getQuery(Message.class);
        ParseQuery<Message> toYou = ParseQuery.getQuery(Message.class);
        // Configure limit and sort order
        toMe.whereEqualTo(ParseConstants.KEY_SENDER_ID, sContactId);
        toMe.whereEqualTo(ParseConstants.KEY_RECIPIENT_ID, sMyId);
        toYou.whereEqualTo(ParseConstants.KEY_SENDER_ID, sMyId);
        toYou.whereEqualTo(ParseConstants.KEY_RECIPIENT_ID, sContactId);
        // Construct query to execute
        List<ParseQuery<Message>> combined = new ArrayList<ParseQuery<Message>>();
        combined.add(toMe);
        combined.add(toYou);
        ParseQuery<Message> chat = ParseQuery.or(combined);
        chat.setLimit(ParseConstants.MAX_CHAT_MESSAGES_TO_SHOW);
        chat.orderByAscending(ParseConstants.KEY_CREATED_AT);
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        chat.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> messages, com.parse.ParseException e) {
                if (e == null) {
                    mMessages.clear();
                    mMessages.addAll(messages);
                    mAdapter.notifyDataSetChanged(); // update adapter
                    lvChat.invalidate(); // redraw listview
                } else {
                    Log.d("message", "Error: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refreshMessages();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
