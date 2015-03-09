package mtolhuys.com.prattle;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by mtolhuys on 12/02/15.
 */
public class PrattleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Message.class);

        Parse.initialize(this, "40FqATAfVa3oWHWmOfSV4apzrEqaiILK6HowjrGh", "Sfw8ljaxRDTY3MJIZVq2jL34E0d9Ux0xB3I9pA2D");

    }
}
