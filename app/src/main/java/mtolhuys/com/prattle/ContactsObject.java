package mtolhuys.com.prattle;

import com.parse.ParseObject;

import java.util.List;
import java.util.SimpleTimeZone;

/**
 * Created by mtolhuys on 05/03/15.
 */
public class ContactsObject {

    private String contactName;
    private String contactId;
    private ParseObject object;

    public ContactsObject(String contactName, String contactId, ParseObject object) {
        super();
        this.contactName = contactName;
        this.contactId = contactId;
        this.object = object;
    }

    public String getContactName() {
        return contactName;
    }
//    public void setContactName(String contactName) {
//        this.contactName = contactName;
//    }
    public String getContactId() {
        return contactId;
    }
//    public void setContactId(String contactId) {
//        this.contactId = contactId;
//    }
    public ParseObject getContact() {
        return object;
    }
//    public void setContact(ParseObject object) {
//        this.object = object;
//    }
}
