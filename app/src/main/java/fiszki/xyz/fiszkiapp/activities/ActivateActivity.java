package fiszki.xyz.fiszkiapp.activities;

import android.content.Context;
import android.content.Intent;
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
import fiszki.xyz.fiszkiapp.source.Functions;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.R;

/**
 * Class allows user to activate their account.
 * Implements AsyncResponse to get callback from ConnectionTask
 */
public class ActivateActivity extends AppCompatActivity implements AsyncResponse {

    // GUI Components
    private EditText userEmailArea;
    private EditText userCodeArea;
    private ProgressBar progressBar;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);

        // Initialize GUI Components
        this.userEmailArea = (EditText)findViewById(R.id.emailArea);
        this.userCodeArea = (EditText) findViewById(R.id.codeArea);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if(getIntent().getStringExtra("email") != null) {
            this.userEmailArea.setText(getIntent().getStringExtra("email"));
            this.userCodeArea.requestFocus();
        }
    }

    /**
     * Handles event on user click
     * @param view clicked view
     */
    public void confirmButton_onClick(View view) {
        // Get user email and password from login screen
        String userEmail = this.userEmailArea.getText().toString();
        String userCode = this.userCodeArea.getText().toString();

        if(userEmail.equals("") || userCode.equals(""))
            Toast.makeText(ActivateActivity.this, getString(R.string.fillEmailAndCode), Toast.LENGTH_LONG).show();
        else if(!Functions.validateEmail(userEmail))
            Toast.makeText(ActivateActivity.this, getString(R.string.emailFormatIncorrect), Toast.LENGTH_LONG).show();
        else {
            if(!Functions.isOnline(getApplicationContext()))
                Toast.makeText(ActivateActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
            else {

                // Encode both POST arguments(email and password) with UTF-8 Encoder
                try{
                    userEmail = URLEncoder.encode(userEmail, "UTF-8");
                    userCode = URLEncoder.encode(userCode, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Build POST Request
                String mRequest = "email=" + userEmail + "&code=" + userCode;

                // Create and run ConnectionTask
                final ConnectionTask mConn = new ConnectionTask(this, Constants.ACTIVATE_ACC_TASK);
                mConn.execute(getString(R.string.activateAccountPhp), mRequest);

                progressBar.setVisibility(View.VISIBLE);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                            mConn.cancel(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ActivateActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                        }
                    }
                }, 10000);
            }
        }
    }

    /**
     * Clears all inputs
     * @param view clicked element view
     */
    public void clearForm_onClick(View view) {
        // Clear all input area
        this.userEmailArea.setText("");
        this.userCodeArea.setText("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processFinish(HashMap<String, String> result) {
        /*
        Check result from server to know if register was successful
        Server answer: output
            1 - activation successful
            2 - code was incorrect
            3 - email was incorrect
            4 - incorrect data format
         */

        String output = result.get(Constants.RESULT);
        progressBar.setVisibility(View.GONE);

        Log.d("ACTIVITY_RESULT", output);

        switch(output){
            case "1":
                Toast.makeText(ActivateActivity.this, getString(R.string.accountActivated), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ActivateActivity.this, LoginActivity.class);
                intent.putExtra("email", userEmailArea.getText().toString());
                startActivity(intent);
                break;
            case "2":
                Toast.makeText(ActivateActivity.this, getString(R.string.codeIncorrect), Toast.LENGTH_LONG).show();
                break;
            case "3":
                Toast.makeText(ActivateActivity.this, getString(R.string.emailIncorrect), Toast.LENGTH_LONG).show();
                break;
            case "4":
                Toast.makeText(ActivateActivity.this, getString(R.string.dataFormatIncorrect), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
