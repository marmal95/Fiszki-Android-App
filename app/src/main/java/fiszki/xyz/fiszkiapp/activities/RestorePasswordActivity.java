package fiszki.xyz.fiszkiapp.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.source.RequestBuilder;
import fiszki.xyz.fiszkiapp.utils.Functions;

public class RestorePasswordActivity extends AppCompatActivity implements AsyncResponse {

    private EditText userEmailArea;
    private EditText userNameArea;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_password);

        this.userEmailArea = findViewById(R.id.emailArea);
        this.userNameArea = findViewById(R.id.nameArea);
        this.progressBar = findViewById(R.id.progressBar);
    }

    public void restoreButton_onClick(View view) {
        String userEmail = this.userEmailArea.getText().toString();
        String userName = this.userNameArea.getText().toString();

        if(userEmail.isEmpty() || userName.isEmpty())
            Functions.showToast(this, getString(R.string.fillEmailAndPassword));
        else if(!Functions.validateEmail(userEmail))
            Functions.showToast(this, getString(R.string.emailFormatIncorrect));
        else{
            if(!Functions.isOnline(getApplicationContext()))
                Functions.showToast(this, getString(R.string.noConnectionWarning));
            else {
                RequestBuilder requestBuilder = new RequestBuilder();
                requestBuilder.putParameter("email", userEmail);
                requestBuilder.putParameter("name", userName);
                requestBuilder.encodeParameters("UTF-8");

                String request = requestBuilder.buildRequest();
                progressBar.setVisibility(View.VISIBLE);

                final ConnectionTask connection = new ConnectionTask(this, ConnectionTask.Mode.RESTORE_PASSWORD);
                connection.execute(getString(R.string.forgotPasswordPhp), request);
                createCancelConnectionHandler(connection);
            }
        }
    }

    public void clearForm_onClick(View view) {
        this.userEmailArea.setText("");
        this.userNameArea.setText("");
    }

    @Override
    public void processRequestResponse(HashMap<ConnectionTask.Key, String> result) {
        progressBar.setVisibility(View.GONE);

        int responseCode = ResponseCode.INIT_CODE;
        String requestResponse = result.get(ConnectionTask.Key.REQUEST_RESPONSE);
        try {
            JSONObject c = new JSONObject(requestResponse);
            responseCode = c.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch(responseCode){
            case ResponseCode.SEND_EMAIL_ERROR:
                Functions.showToast(this, getString(R.string.emailSendError));
                break;
            case ResponseCode.SUCCESS:
                Functions.showToast(this, getString(R.string.newPasswordSent));
                break;
            case ResponseCode.DATA_INCORRECT:
                Functions.showToast(this, getString(R.string.incorrectFormat));
                break;
            case ResponseCode.EMAIL_INCORRECT:
                Functions.showToast(this, getString(R.string.emailIncorrect));
                break;
            case ResponseCode.NAME_INCORRECT:
                Functions.showToast(this, getString(R.string.nameIncorrect));
                break;
            default:
                Functions.showToast(this, getString(R.string.errorOccurred));
                break;
        }
    }

    private void createCancelConnectionHandler(final ConnectionTask connection) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(connection.getStatus() == AsyncTask.Status.RUNNING) {
                    connection.cancel(true);
                    progressBar.setVisibility(View.GONE);
                    Functions.showToast(RestorePasswordActivity.this, getString(R.string.connectionProblem));
                }
            }
        }, ConnectionTask.TIME_LIMIT_MS);
    }

    private class ResponseCode {
        static final int INIT_CODE = -1;
        static final int SUCCESS = 1;
        static final int SEND_EMAIL_ERROR = 2;
        static final int DATA_INCORRECT = 3;
        static final int EMAIL_INCORRECT = 4;
        static final int NAME_INCORRECT = 5;
    }
}
