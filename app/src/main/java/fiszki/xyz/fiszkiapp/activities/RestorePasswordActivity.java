package fiszki.xyz.fiszkiapp.activities;

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
        this.userEmailArea = findViewById(R.id.emailArea);
        this.userNameArea = findViewById(R.id.nameArea);
        this.progressBar = findViewById(R.id.progressBar);
    }

    public void restoreButton_onClick(View view) {
        // Get user email and password from login screen
        String userEmail = this.userEmailArea.getText().toString();
        String userName = this.userNameArea.getText().toString();

        if(userEmail.equals("") || userName.equals(""))
            Toast.makeText(RestorePasswordActivity.this, getString(R.string.fillEmailAndPassword), Toast.LENGTH_LONG).show();
        else if(!Functions.validateEmail(userEmail))
            Toast.makeText(RestorePasswordActivity.this, getString(R.string.emailFormatIncorrect), Toast.LENGTH_LONG).show();
        else{
            if(!Functions.isOnline(getApplicationContext()))
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
                final ConnectionTask mConn = new ConnectionTask(this, ConnectionTask.Mode.RESTORE_PASSWORD);
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

    @Override
    public void processRequestResponse(HashMap<ConnectionTask.Key, String> result) {

        String requestResponse = result.get(ConnectionTask.Key.REQUEST_RESPONSE);
        progressBar.setVisibility(View.GONE);

        switch(requestResponse){
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
