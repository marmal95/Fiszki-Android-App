package fiszki.xyz.fiszkiapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.source.RequestBuilder;
import fiszki.xyz.fiszkiapp.source.User;
import fiszki.xyz.fiszkiapp.utils.Functions;

public class MenuActivity extends AppCompatActivity implements AsyncResponse {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setTitle(getString(R.string.fiszkiMenu));

        this.progressBar = findViewById(R.id.progressBar);

        String user_token = User.getInstance(this).getUserToken();
        if(user_token == null)
        {
            // TODO: Old preferences - remove after some time
            String oldToken = getSharedPreferences("user_data", 0).getString("user_token", null);
            if(oldToken == null)
                logout();
            else
                getUserInfo(oldToken);
        }
        else
            this.getUserInfo(user_token);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_bugReport:
                this.reportBug();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void myFlashcards_onClick(View view) {
        Intent intent = new Intent(MenuActivity.this, MyFlashcardsActivity.class);
        startActivity(intent);
    }

    public void favouriteFlashcards_onClick(View view) {
        Intent intent = new Intent(MenuActivity.this, FavouriteFlashcardsActivity.class);
        startActivity(intent);
    }

    public void recommendedFlashcards_onClick(View view) {
        Intent intent = new Intent(MenuActivity.this, RecommendedFlashcardsActivity.class);
        startActivity(intent);
    }

    public void localFlashcards_onClick(View view) {
        Intent intent = new Intent(MenuActivity.this, LocalFlashcardsActivity.class);
        startActivity(intent);
    }

    public void findFlashcards_onClick(View view) {
        Intent intent = new Intent(MenuActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    public void settings_onClick(View view) {
        Intent intent = new Intent(MenuActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void processRequestResponse(HashMap<ConnectionTask.Key, String> result) {
        String requestResponse = result.get(ConnectionTask.Key.REQUEST_RESPONSE);

        try {
            JSONObject c = new JSONObject(requestResponse);
            User user = User.getInstance(getApplicationContext());
            user.setName(c.getString("name"));
            user.setUserId(c.getString("user_id"));
            user.setEmail(c.getString("email"));
            user.setFullName(c.getString("full_name"));
            user.setPermission(c.getString("permission"));
            user.setTimeCreated(c.getString("time_created"));
            user.setLastActivity(c.getString("last_list_activity"));
        } catch (JSONException e) {
            logout();
        }
    }

    private void logout() {
        User.getInstance(this).clearUserData(this);
        Functions.showToast(this, getString(R.string.userLogout));

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void reportBug(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_report_bug);

        TextView header = dialog.findViewById(R.id.dialogHeader);
        header.setText(getString(R.string.reportBug));

        final EditText topic = dialog.findViewById(R.id.messageSubject);
        final EditText message = dialog.findViewById(R.id.messageContent);

        Button sendButton = dialog.findViewById(R.id.okButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"dev.marmal195@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "[" + User.getInstance(MenuActivity.this).getName() + "]: " + topic.getText().toString());
                i.putExtra(Intent.EXTRA_TEXT, message.getText().toString());
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.sendEmail)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Functions.showToast(MenuActivity.this, getString(R.string.noEmailClientInstalled));
                }
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void getUserInfo(String token){
        if(!Functions.isOnline(getApplicationContext()))
            Functions.showToast(this, getString(R.string.noConnectionWarning));
        else{
            RequestBuilder requestBuilder = new RequestBuilder();
            requestBuilder.putParameter("token", token);

            String request = requestBuilder.buildRequest();
            progressBar.setVisibility(View.VISIBLE);

            final ConnectionTask connection = new ConnectionTask(this, ConnectionTask.Mode.GET_USER_INFO);
            connection.execute(getString(R.string.userInfoByToken), request);
            createCancelConnectionHandler(connection);
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
                    Functions.showToast(MenuActivity.this, getString(R.string.connectionProblem));
                }
            }
        }, ConnectionTask.TIME_LIMIT_MS);
    }
}
