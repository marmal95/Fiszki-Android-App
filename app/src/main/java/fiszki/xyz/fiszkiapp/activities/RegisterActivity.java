package fiszki.xyz.fiszkiapp.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.HashMap;

import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.source.RequestBuilder;
import fiszki.xyz.fiszkiapp.utils.Functions;
import fiszki.xyz.fiszkiapp.utils.IntentKey;


public class RegisterActivity extends AppCompatActivity implements AsyncResponse {

    private EditText userEmailArea;
    private EditText userNameArea;
    private EditText userPasswordArea;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.userEmailArea = findViewById(R.id.emailArea);
        this.userNameArea = findViewById(R.id.nameArea);
        this.userPasswordArea = findViewById(R.id.passwordArea);
        this.progressBar = findViewById(R.id.progressBar);
    }

    public void registerButton_onClick(View view) {
        String userEmail = this.userEmailArea.getText().toString();
        String userName = this.userNameArea.getText().toString();
        String userPassword = this.userPasswordArea.getText().toString();

        if(userEmail.isEmpty() || userName.isEmpty() || userPassword.isEmpty())
            Functions.showToast(this, getString(R.string.fillAllAreas));
        else if(!Functions.validateEmail(userEmail))
            Functions.showToast(this, getString(R.string.emailFormatIncorrect));
        else{
            if(!Functions.isOnline(getApplicationContext()))
                Functions.showToast(this, getString(R.string.noConnectionWarning));
            else {
                RequestBuilder requestBuilder = new RequestBuilder();
                requestBuilder.putParameter("email", userEmail);
                requestBuilder.putParameter("name", userName);
                requestBuilder.putParameter("password", userPassword);
                requestBuilder.encodeParameters("UTF-8");

                String request = requestBuilder.buildRequest();
                progressBar.setVisibility(View.VISIBLE);

                final ConnectionTask connection = new ConnectionTask(this, ConnectionTask.Mode.REGISTER_ACCOUNT);
                connection.execute(getString(R.string.registerPhp), request);
                createCancelConnectionHandler(connection);
            }
        }
    }

    public void clearForm_onClick(View view) {
        this.userEmailArea.setText("");
        this.userNameArea.setText("");
        this.userPasswordArea.setText("");
    }

    @Override
    public void processRequestResponse(HashMap<ConnectionTask.Key, String> result) {
        progressBar.setVisibility(View.GONE);

        String responseRequest = result.get(ConnectionTask.Key.REQUEST_RESPONSE);
        int responseCode = Integer.valueOf(responseRequest);

        switch(responseCode){
            case ResponseCode.SUCCESS:
                Functions.showToast(this, getString(R.string.accountCreated));
                Intent intent = new Intent(RegisterActivity.this, ActivateActivity.class);
                intent.putExtra(IntentKey.EMAIL.name(), this.userEmailArea.getText().toString());
                startActivity(intent);
                break;

            case ResponseCode.ERROR:
                Functions.showToast(this, getString(R.string.errorOccurred));
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
                    Functions.showToast(RegisterActivity.this, getString(R.string.connectionProblem));
                }
            }
        }, ConnectionTask.TIME_LIMIT_MS);
    }

    private class ResponseCode {
        static final int INIT_CODE = -1;
        static final int SUCCESS = 2;
        static final int ERROR = 1;
    }
}
