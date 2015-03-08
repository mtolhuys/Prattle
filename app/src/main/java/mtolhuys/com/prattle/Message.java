package mtolhuys.com.prattle;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by mtolhuys on 08/03/15.
 */
@ParseClassName("Message")
public class Message extends ParseObject {
    public String getUserId() {
        return getString("userId");
    }

    // GETTERS

    public String getUserName() {
        return getString("userName");
    }

    public String getContactId() {
        return getString("contactId");
    }

    public String getContactName() {
        return getString("contactName");
    }

    public String getBody() {
        return getString("body");
    }

    // SETTERS

    public void setUserId(String userId) {
        put("userId", userId);
    }

    public void setUserName(String userName) {
        put("userName", userName);
    }

    public void setContactId(String contactId) {
        put("contactId", contactId);
    }

    public void setContactName(String contactName) {
        put("contactName", contactName);
    }

    public void setBody(String body) {
        put("body", body);
    }
}
