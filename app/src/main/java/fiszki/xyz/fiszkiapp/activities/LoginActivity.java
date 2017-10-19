package fiszki.xyz.fiszkiapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import fiszki.xyz.fiszkiapp.source.AppPreferences;
import fiszki.xyz.fiszkiapp.source.Functions;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.R;

/**
 * Class allows user to log in into their account.
 * Implement AsyncResponse to get callback
 * from the ConnectionTask.
 */
public class LoginActivity extends AppCompatActivity implements AsyncResponse {

    // GUI Components
    private EditText userEmailArea;
    private EditText userPasswArea;
    private ProgressBar progressBar;

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if device is connected to the internet
        if (!Functions.isOnline(getApplicationContext()))
            Toast.makeText(LoginActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();

        // Initialize GUI components
        this.userEmailArea = (EditText) findViewById(R.id.emailArea);
        this.userPasswArea = (EditText) findViewById(R.id.passwordArea);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Check if e-mail was passed from Activate Activity
        // if yes - fill inputs with data
        // else - leave them blank
        if (getIntent().getStringExtra("email") != null) {
            this.userEmailArea.setText(getIntent().getStringExtra("email"));
            this.userPasswArea.requestFocus();
        }

    }

    /**
     * Handles Login Button click.
     * Builds POST request and makes server connection
     * via ConnectionTask.
     *
     * @param view clicked view
     */
    public void loginButton_onClick(View view) {

        // Get user email and password from login screen
        String userEmail = this.userEmailArea.getText().toString();
        String userPassw = this.userPasswArea.getText().toString();

        if (userEmail.equals("") || userPassw.equals(""))
            Toast.makeText(LoginActivity.this, getString(R.string.fillEmailAndPassword), Toast.LENGTH_LONG).show();
        else if (!Functions.validateEmail(userEmail))
            Toast.makeText(LoginActivity.this, getString(R.string.emailFormatIncorrect), Toast.LENGTH_LONG).show();
        else {
            if (!Functions.isOnline(getApplicationContext()))
                Toast.makeText(LoginActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
            else {

                // Encode both POST arguments(email and password) with UTF-8 Encoder
                try {
                    userEmail = URLEncoder.encode(userEmail, "UTF-8");
                    userPassw = URLEncoder.encode(userPassw, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Build POST Request
                String mRequest = "email=" + userEmail + "&password=" + userPassw;

                // Create and run ConnectionTask
                final ConnectionTask mConn = new ConnectionTask(this, Constants.LOGIN_TASK);
                mConn.execute(getString(R.string.getTokenPhp), mRequest);

                progressBar.setVisibility(View.VISIBLE);

                Log.d("SEND_POST", "Seinding POST" + mRequest);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mConn.getStatus() == AsyncTask.Status.RUNNING) {
                            mConn.cancel(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                        }
                    }
                }, 10000);
            }
        }
    }

    /**
     * Handles ForgotPassword click action.
     *
     * @param view clicked view
     */
    public void forgotPassword_onClick(View view) {
        Intent intent = new Intent(LoginActivity.this, RestorePasswordActivity.class);
        startActivity(intent);
    }

    /**
     * Handles CreateAccount click action.
     *
     * @param view clicked view
     */
    public void createAccount_onClick(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Handles ActivatAccount click action.
     *
     * @param view clicked view
     */
    public void activateAccount_onClick(View view) {
        Intent intent = new Intent(LoginActivity.this, ActivateActivity.class);
        startActivity(intent);
    }

    /**
     * ConnectionTask callback.
     * Updates data and ui.
     *
     * @param result task(MODE) and server response(RESULT)
     */
    @Override
    public void processFinish(HashMap<String, String> result) {
        String output = result.get(Constants.RESULT);
        progressBar.setVisibility(View.GONE);

        switch (output) {
            case "0":
                Toast.makeText(LoginActivity.this, getResources().
                        getString(R.string.emailOrPasswIncorrect), Toast.LENGTH_LONG).show();
                break;

            case "":
                Toast.makeText(LoginActivity.this, getResources().
                        getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                break;

            case "-1":
                Toast.makeText(LoginActivity.this, getResources().
                        getString(R.string.accountNotActive), Toast.LENGTH_LONG).show();
                break;

            default:
                AppPreferences appPreferences = AppPreferences.getInstance(getApplicationContext());
                appPreferences.put(AppPreferences.Key.USER_TOKEN, output);

                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                break;
        }
    }
}
