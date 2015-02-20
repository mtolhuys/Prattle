package mtolhuys.com.prattle;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.ParseException;

import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class RetrieveAccountActivity extends ActionBarActivity {

    protected EditText mEmail;
    protected Button mValidateButton;
    protected Button mToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getString(R.string.font_family))
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_retrieve_account);

        mEmail = (EditText) findViewById(R.id.emailField);
        mValidateButton = (Button) findViewById(R.id.validationButton);
        mToLoginButton = (Button) findViewById(R.id.backToLoginButton);

        mValidateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();

                if (email.isEmpty()) {
                    AlertDialogs.mailAlert(RetrieveAccountActivity.this);
                }
                else {
                    ParseUser.requestPasswordResetInBackground(email,
                        new RequestPasswordResetCallback() {
                            public void done(ParseException e) {
                                if (e == null) {
                                    // An email was successfully sent with reset instructions.
                                    Toast toast= Toast.makeText(getApplicationContext()
                                            , getString(R.string.email_sent_message)
                                            , Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.BOTTOM| Gravity.CENTER_HORIZONTAL, 0, 1);
                                    toast.show();
                                    }
                                    else {
                                    // Something went wrong. Look at the ParseException to see what's up.
                                    AlertDialogs.overallAlert(RetrieveAccountActivity.this);
                                    }
                                }
                            });
                }
            }
        });

        mToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RetrieveAccountActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
