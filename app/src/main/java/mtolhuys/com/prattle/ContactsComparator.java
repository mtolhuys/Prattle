package mtolhuys.com.prattle;

import java.util.Comparator;

/**
 * Created by mtolhuys on 05/03/15.
 */
public class ContactsComparator implements Comparator<ContactsObject> {
    public int compare(ContactsObject name1, ContactsObject name2) {
        return (name1.getContactName().toUpperCase()).compareTo((name2.getContactName().toUpperCase()));
    }
}
