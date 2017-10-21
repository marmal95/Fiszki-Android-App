package fiszki.xyz.fiszkiapp.activities;

import android.content.Intent;
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
import fiszki.xyz.fiszkiapp.source.AppPreferences;
import fiszki.xyz.fiszkiapp.source.RequestBuilder;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.utils.Functions;
import fiszki.xyz.fiszkiapp.utils.IntentKey;

public class LoginActivity extends AppCompatActivity implements AsyncResponse {

    private EditText userEmailArea;
    private EditText userPasswordArea;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!Functions.isOnline(getApplicationContext()))
            Functions.showToast(LoginActivity.this, getString(R.string.noConnectionWarning));

        this.userEmailArea = (EditText) findViewById(R.id.emailArea);
        this.userPasswordArea = (EditText) findViewById(R.id.passwordArea);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (getIntent().getStringExtra(IntentKey.EMAIL.name()) != null) {
            this.userEmailArea.setText(getIntent().getStringExtra(IntentKey.EMAIL.name()));
            this.userPasswordArea.requestFocus();
        }
    }

    public void loginButton_onClick(View view) {
        String userEmail = this.userEmailArea.getText().toString();
        String userPassword = this.userPasswordArea.getText().toString();

        if (userEmail.isEmpty() || userPassword.isEmpty())
            Functions.showToast(LoginActivity.this, getString(R.string.fillEmailAndPassword));
        else if (!Functions.validateEmail(userEmail))
            Functions.showToast(LoginActivity.this, getString(R.string.emailFormatIncorrect));
        else {
            if (!Functions.isOnline(getApplicationContext()))
                Functions.showToast(LoginActivity.this, getString(R.string.noConnectionWarning));
            else {
                RequestBuilder requestBuilder = new RequestBuilder();
                requestBuilder.putParameter("email", userEmail);
                requestBuilder.putParameter("password", userPassword);
                requestBuilder.encodeParameters("UTF-8");

                String request = requestBuilder.buildRequest();
                progressBar.setVisibility(View.VISIBLE);

                final ConnectionTask connection = new ConnectionTask(this, Constants.LOGIN_TASK);
                connection.execute(getString(R.string.getTokenPhp), request);
                createCancelConnectionHandler(connection);
            }
        }
    }

    public void forgotPassword_onClick(View view) {
        Intent intent = new Intent(LoginActivity.this, RestorePasswordActivity.class);
        startActivity(intent);
    }

    public void createAccount_onClick(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void activateAccount_onClick(View view) {
        Intent intent = new Intent(LoginActivity.this, ActivateActivity.class);
        startActivity(intent);
    }

    @Override
    public void processFinish(HashMap<String, String> result) {
        progressBar.setVisibility(View.GONE);

        int responseCode = ResponseCode.INIT_CODE;
        String userToken = null;
        try {
            JSONObject c = new JSONObject(result.get(Constants.RESULT));
            responseCode = c.getInt("status");
            userToken = c.optString("token", null);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (responseCode) {
            case ResponseCode.INIT_CODE:
                Functions.showToast(this, getString(R.string.connectionProblem));
                break;

            case ResponseCode.SUCCESS:
                handleSuccessLogon(userToken);
                break;

            case ResponseCode.WRONG_DATA_FORMAT:
                Functions.showToast(this, getString(R.string.dataFormatIncorrect));
                break;

            case ResponseCode.WRONG_DATA:
                Functions.showToast(this, getString(R.string.emailOrPasswIncorrect));
                break;
        }
    }

    private void handleSuccessLogon(String userToken) {
        AppPreferences appPreferences = AppPreferences.getInstance(getApplicationContext());
        appPreferences.put(AppPreferences.Key.USER_TOKEN, userToken);

        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void createCancelConnectionHandler(final ConnectionTask connection) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(connection.getStatus() == AsyncTask.Status.RUNNING) {
                    connection.cancel(true);
                    progressBar.setVisibility(View.GONE);
                    Functions.showToast(LoginActivity.this, getString(R.string.connectionProblem));
                }
            }
        }, 10000);
    }

    private class ResponseCode {
        static final int INIT_CODE = -1;
        static final int SUCCESS = 1;
        static final int WRONG_DATA_FORMAT = 2;
        static final int WRONG_DATA = 3;
    }
}
