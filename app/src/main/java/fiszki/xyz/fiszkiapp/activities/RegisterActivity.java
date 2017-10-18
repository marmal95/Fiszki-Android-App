package fiszki.xyz.fiszkiapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.R;

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
        this.userEmailArea = (EditText)findViewById(R.id.emailArea);
        this.userNameArea = (EditText)findViewById(R.id.nameArea);
        this.userPasswArea = (EditText)findViewById(R.id.passwordArea);
        this.progressBar = (ProgressBar)findViewById(R.id.progressBar);
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
        else if(!Constants.validateEmail(userEmail))
            Toast.makeText(RegisterActivity.this, getString(R.string.emailFormatIncorrect), Toast.LENGTH_LONG).show();
        else{
            if(!isOnline())
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
                final ConnectionTask mConn = new ConnectionTask(this, Constants.REGISTER_TASK);
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
     * Verifies if device is connected to the internet.
     * @return true - if online, false - otherwise
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * {@inheritDoc}
     * @param result
     */
    @Override
    public void processFinish(HashMap<String, String> result) {
        /*
        Check result from server to know if register was successful
        Server answer: output
            0 - ???? TODO
            1 - ???? TODO
            2 - account has been created
         */

        String output = result.get(Constants.RESULT);
        progressBar.setVisibility(View.GONE);

        Log.d("REGISTER_RESULT", output);

        switch(output){

            case "2":
                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.accountCreated), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegisterActivity.this, ActivateActivity.class);
                intent.putExtra("email", this.userEmailArea.getText().toString());
                startActivity(intent);
                break;

            // TODO: KIEDY KTORE ECHO 1 KTORE ECHO 2
            case "1":
                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
                break;

            default:
                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
