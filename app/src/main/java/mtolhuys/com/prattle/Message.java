package mtolhuys.com.prattle;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by mtolhuys on 08/03/15.
 */
@ParseClassName("Message")
public class Message extends ParseObject {

    // GETTERS

    public String getSenderId() {
        return getString(ParseConstants.KEY_SENDER_ID);
    }

    public String getSenderName() {
        return getString(ParseConstants.KEY_SENDER_NAME);
    }

    public String getRecipientId() {
        return getString(ParseConstants.KEY_RECIPIENT_ID);
    }

    public String getRecipientName() {
        return getString(ParseConstants.KEY_RECIPIENT_NAME);
    }

    public String getBody() {
        return getString(ParseConstants.MESSAGE_BODY);
    }

    // SETTERS

    public void setSenderId(String userId) {
        put(ParseConstants.KEY_SENDER_ID, userId);
    }

    public void setSenderName(String userName) {
        put(ParseConstants.KEY_SENDER_NAME, userName);
    }

    public void setRecipientId(String contactId) {
        put(ParseConstants.KEY_RECIPIENT_ID, contactId);
    }

    public void setRecipientName(String contactName) {
        put(ParseConstants.KEY_RECIPIENT_NAME, contactName);
    }

    public void setBody(String body) {
        put(ParseConstants.MESSAGE_BODY, body);
    }
}
