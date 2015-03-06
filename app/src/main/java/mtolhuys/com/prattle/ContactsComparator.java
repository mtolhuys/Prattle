package mtolhuys.com.prattle;

import java.util.Comparator;

/**
 * Created by mtolhuys on 05/03/15.
 */
public class ContactsComparator implements Comparator<ContactsObject> {
    public int compare(ContactsObject f1, ContactsObject f2) {
        return (f1.getContactName().toUpperCase()).compareTo((f2.getContactName().toUpperCase()));
    }
}
