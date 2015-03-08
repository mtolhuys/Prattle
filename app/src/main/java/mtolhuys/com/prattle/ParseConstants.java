package mtolhuys.com.prattle;

import com.parse.ParseUser;

/**
 * Created by mtolhuys on 14/02/15.
 */
public final class ParseConstants {
    // Class Names
    public static final String CLASS_MESSAGES = "Messages";
    public static final String CLASS_CONTACTS = "Contacts";

    // Field Names
    public static final String KEY_USERNAME = "username";
    public static final String KEY_SEARCH_NAME = "searchName";
    public static final String KEY_CONTACT_RELATION = "contactRelation";
    public static final String KEY_CONTACT_STATUS = "status";
    public static final String KEY_USERS_IDS = "usersIds";
    public static final String KEY_RECIPIENT_NAME = "recipientName";
    public static final String KEY_RECIPIENT_ID = "recipientId";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_FILE = "file";
    public static final String KEY_FILE_TYPE = "fileType";
    public static final String KEY_CREATED_AT = "createdAt";

    // Miscellaneous values
    public static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;
    public static final String MESSAGE_BODY = "body";
    public static final String TYPE_IMAGE = "image";
}
