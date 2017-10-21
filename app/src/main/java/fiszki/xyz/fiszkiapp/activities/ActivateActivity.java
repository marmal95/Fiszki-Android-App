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

import java.util.HashMap;

import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.source.RequestBuilder;
import fiszki.xyz.fiszkiapp.utils.Functions;
import fiszki.xyz.fiszkiapp.utils.IntentKey;

public class ActivateActivity extends AppCompatActivity implements AsyncResponse {

    private EditText userEmailArea;
    private EditText userCodeArea;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate);

        this.userEmailArea = findViewById(R.id.emailArea);
        this.userCodeArea = findViewById(R.id.codeArea);
        this.progressBar = findViewById(R.id.progressBar);

        if(getIntent().getStringExtra(IntentKey.EMAIL.name()) != null) {
            this.userEmailArea.setText(getIntent().getStringExtra(IntentKey.EMAIL.name()));
            this.userCodeArea.requestFocus();
        }
    }

    public void confirmButton_onClick(View view) {
        String userEmail = this.userEmailArea.getText().toString();
        String userCode = this.userCodeArea.getText().toString();

        if(userEmail.isEmpty() || userCode.isEmpty())
            Toast.makeText(ActivateActivity.this, getString(R.string.fillEmailAndCode), Toast.LENGTH_LONG).show();
        else if(!Functions.validateEmail(userEmail))
            Toast.makeText(ActivateActivity.this, getString(R.string.emailFormatIncorrect), Toast.LENGTH_LONG).show();
        else {
            if(!Functions.isOnline(getApplicationContext()))
                Toast.makeText(ActivateActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
            else {
                RequestBuilder requestBuilder = new RequestBuilder();
                requestBuilder.putParameter("email", userEmail);
                requestBuilder.putParameter("code", userCode);
                requestBuilder.encodeParameters("UTF-8");

                String request = requestBuilder.buildRequest();
                progressBar.setVisibility(View.VISIBLE);

                final ConnectionTask connection = new ConnectionTask(this, ConnectionTask.Mode.ACTIVATE_ACCOUNT);
                connection.execute(getString(R.string.activateAccountPhp), request);
                createCancelConnectionHandler(connection);
            }
        }
    }

    public void clearForm_onClick(View view) {
        this.userEmailArea.setText("");
        this.userCodeArea.setText("");
    }

    @Override
    public void processRequestResponse(HashMap<ConnectionTask.Key, String> result) {
        progressBar.setVisibility(View.GONE);

        String requestResponse = result.get(ConnectionTask.Key.REQUEST_RESPONSE);
        int responseCode = Integer.valueOf(requestResponse);

        switch(responseCode) {
            case ResponseCode.SUCCESS:
                Toast.makeText(ActivateActivity.this, getString(R.string.accountActivated), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ActivateActivity.this, LoginActivity.class);
                intent.putExtra(IntentKey.EMAIL.name(), userEmailArea.getText().toString());
                startActivity(intent);
                break;
            case ResponseCode.CODE_INCORRECT:
                Toast.makeText(ActivateActivity.this, getString(R.string.codeIncorrect), Toast.LENGTH_LONG).show();
                break;
            case ResponseCode.EMAIL_INCORRECT:
                Toast.makeText(ActivateActivity.this, getString(R.string.emailIncorrect), Toast.LENGTH_LONG).show();
                break;
            case ResponseCode.DATE_FORMAT_INCORRECT:
                Toast.makeText(ActivateActivity.this, getString(R.string.dataFormatIncorrect), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(ActivateActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                }
            }
        }, 10000);
    }

    private class ResponseCode {
        static final int SUCCESS = 1;
        static final int CODE_INCORRECT = 2;
        static final int EMAIL_INCORRECT = 3;
        static final int DATE_FORMAT_INCORRECT = 4;
    }
}
