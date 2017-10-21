package fiszki.xyz.fiszkiapp.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.utils.Functions;
import fiszki.xyz.fiszkiapp.utils.IntentKey;

/**
 * RegisterActivity registers new users.
 * Implementing AsyncResponse to get callback from ConnectionTask
 */
public class RegisterActivity extends AppCompatActivity implements AsyncResponse {

    // GUI Components
    private EditText userEmailArea;
    private EditText userNameArea;
    private EditText userPasswArea;
    private ProgressBar progressBar;

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize GUI Components
        this.userEmailArea = findViewById(R.id.emailArea);
        this.userNameArea = findViewById(R.id.nameArea);
        this.userPasswArea = findViewById(R.id.passwordArea);
        this.progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Handles Register Button click event.
     * Builds POST request and runs ConnectionTask
     * @param view clicked view
     */
    public void registerButton_onClick(View view) {

        // Get user email and password from login screen
        String userEmail = this.userEmailArea.getText().toString();
        String userName = this.userNameArea.getText().toString();
        String userPassw = this.userPasswArea.getText().toString();

        if(userEmail.equals("") || userName.equals("") || userPassw.equals(""))
            Toast.makeText(RegisterActivity.this, getString(R.string.fillAllAreas), Toast.LENGTH_LONG).show();
        else if(!Functions.validateEmail(userEmail))
            Toast.makeText(RegisterActivity.this, getString(R.string.emailFormatIncorrect), Toast.LENGTH_LONG).show();
        else{
            if(!Functions.isOnline(getApplicationContext()))
                Toast.makeText(RegisterActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
            else {

                // Encode both POST arguments(email and password) with UTF-8 Encoder
                try{
                    userEmail = URLEncoder.encode(userEmail, "UTF-8");
                    userName = URLEncoder.encode(userName, "UTF-8");
                    userPassw = URLEncoder.encode(userPassw, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Build POST Request
                String mRequest = "email=" + userEmail + "&name=" + userName + "&password=" + userPassw;

                // Create and run ConnectionTask
                final ConnectionTask mConn = new ConnectionTask(this, ConnectionTask.Mode.REGISTER_ACCOUNT);
                mConn.execute(getString(R.string.registerPhp), mRequest);

                progressBar.setVisibility(View.VISIBLE);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                            mConn.cancel(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                        }
                    }
                }, 10000);
            }
        }
    }

    /**
     * ClearForm click event.
     * Clears all inputs.
     * @param view clicked view
     */
    public void clearForm_onClick(View view) {
        this.userEmailArea.setText("");
        this.userNameArea.setText("");
        this.userPasswArea.setText("");
    }

    /**
     * {@inheritDoc}
     * @param result
     */
    @Override
    public void processRequestResponse(HashMap<ConnectionTask.Key, String> result) {
        String responseRequest = result.get(ConnectionTask.Key.REQUEST_RESPONSE);
        progressBar.setVisibility(View.GONE);

        switch(responseRequest){
            case "2":
                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.accountCreated), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegisterActivity.this, ActivateActivity.class);
                intent.putExtra(IntentKey.EMAIL.name(), this.userEmailArea.getText().toString());
                startActivity(intent);
                break;

            case "1":
                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
                break;

            default:
                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
