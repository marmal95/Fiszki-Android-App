package fiszki.xyz.fiszkiapp.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

/*
RestorePasswordActivity class allows user
to restore their password in case
they forget it.

Implements AsyncResponse to get callback
from ConnectionTask with the server result.
*/
public class RestorePasswordActivity extends AppCompatActivity implements AsyncResponse {

    // GUI Components
    private EditText userEmailArea;
    private EditText userNameArea;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_password);

        // Initialize GUI components
        this.userEmailArea = (EditText) findViewById(R.id.emailArea);
        this.userNameArea = (EditText) findViewById(R.id.nameArea);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    public void restoreButton_onClick(View view) {
        // Get user email and password from login screen
        String userEmail = this.userEmailArea.getText().toString();
        String userName = this.userNameArea.getText().toString();

        if(userEmail.equals("") || userName.equals(""))
            Toast.makeText(RestorePasswordActivity.this, getString(R.string.fillEmailAndPassword), Toast.LENGTH_LONG).show();
        else if(!Constants.validateEmail(userEmail))
            Toast.makeText(RestorePasswordActivity.this, getString(R.string.emailFormatIncorrect), Toast.LENGTH_LONG).show();
        else{
            if(!isOnline())
                Toast.makeText(RestorePasswordActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
            else {

                // Encode both POST arguments(email and password) with UTF-8 Encoder
                try{
                    userEmail = URLEncoder.encode(userEmail, "UTF-8");
                    userName = URLEncoder.encode(userName, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Build POST Request
                String mRequest = "email=" + userEmail + "&name=" + userName;

                // Create and run ConnectionTask
                final ConnectionTask mConn = new ConnectionTask(this, Constants.RESTORE_PASS_TASK);
                mConn.execute(getString(R.string.forgotPasswordPhp), mRequest);

                progressBar.setVisibility(View.VISIBLE);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                            mConn.cancel(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RestorePasswordActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                        }
                    }
                }, 10000);
            }
        }
    }

    public void clearForm_onClick(View view) {
        this.userEmailArea.setText("");
        this.userNameArea.setText("");
    }

    /*
        Function checks if device is connected to the internet.
        Returns: true - if connected, false - otherwise.
    */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /*
    Function if called at the end of ConnectionTask running.
    Output params contain:
            response from the server to the request
     */
    @Override
    public void processFinish(HashMap<String, String> result) {
        /*
        Check result from server to know if logging was successful
        Server answer: output
            0 - Error while sending email
            1 - New password sent to email
            2 - Incorrect data format
            3 - Such a user does not exist
            4 - Incorrect name or e-mail
            5 - UNKNOWN Error
         */

        String output = result.get(Constants.RESULT);
        progressBar.setVisibility(View.GONE);

        Log.d("REST_PASS_RESULT", output);

        switch(output){
            case "0":
                Toast.makeText(RestorePasswordActivity.this, getResources().
                        getString(R.string.emailSendError), Toast.LENGTH_LONG).show();
                break;

            case "1":
                Toast.makeText(RestorePasswordActivity.this, getResources().
                        getString(R.string.newPasswordSent), Toast.LENGTH_LONG).show();
                break;

            case "2":
                Toast.makeText(RestorePasswordActivity.this, getResources().
                        getString(R.string.incorrectFormat), Toast.LENGTH_LONG).show();
                break;

            case "3":
                Toast.makeText(RestorePasswordActivity.this, getResources().
                        getString(R.string.userNotExist), Toast.LENGTH_LONG).show();
                break;

            case "4":
                Toast.makeText(RestorePasswordActivity.this, getResources().
                        getString(R.string.nameOrEmailIncorrect), Toast.LENGTH_LONG).show();
                break;

            default:
                Toast.makeText(RestorePasswordActivity.this, getResources().
                        getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
