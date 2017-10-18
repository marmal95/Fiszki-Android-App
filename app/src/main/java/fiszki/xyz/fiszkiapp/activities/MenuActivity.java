package fiszki.xyz.fiszkiapp.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.source.User;

public class MenuActivity extends AppCompatActivity implements AsyncResponse {

    // GUI Components
    private ProgressBar progressBar;

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        setTitle(getString(R.string.fiszkiMenu));

        // Initialize GUI Components
        this.progressBar = (ProgressBar)findViewById(R.id.progressBar);

        /*
        If use has previous version of application he need to be logout
        to save his token in static variable in User class
        otherwise application may be crashed as User.user_token == null
        or there is no user_token saved needed to download all data
         */
        String user_token = User.getInstance(this).getUser_token();
        if(user_token == null)
            logout();
        else
            this.getUserInfo(user_token);
    }

    /**
     * {@inheritDoc}
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_activity_menu, menu);

        return true;
    }

    /**
     * {@inheritDoc}
     * @param item
     * @return
     */
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

    /**
     * Sends POST request to download user info
     */
    private void getUserInfo(String token){
        if(!isOnline())
            Toast.makeText(MenuActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else{
            // Encode POST arguments(email and password) with UTF-8 Encoder
            try{
                token = URLEncoder.encode(token, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "token=" + token;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.GET_USER_INFO);
            mConn.execute(getString(R.string.userInfoByToken), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MenuActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Checks if device is connected to the internet
     * @return true if device online, false - otherwise
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
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

    /**
     * Logs out the user from Fiszki app.
     * Removes the token from SharedPreferences.
     */
    private void logout() {
        User.getInstance(this).clear_data(this);

        Toast.makeText(this, getString(R.string.userLogout), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void reportBug(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_report_bug);

        TextView header = (TextView)dialog.findViewById(R.id.dialogHeader);
        header.setText(getString(R.string.reportBug));

        final EditText topic = (EditText)dialog.findViewById(R.id.messageSubject);
        final EditText message = (EditText)dialog.findViewById(R.id.messageContent);

        Button sendButton = (Button)dialog.findViewById(R.id.okButton);
        Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"dev.marmal195@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "[" + User.getInstance(MenuActivity.this).getName() + "]: " + topic.getText().toString());
                i.putExtra(Intent.EXTRA_TEXT   , message.getText().toString());
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.sendEmail)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MenuActivity.this, getString(R.string.noEmailClientInstalled), Toast.LENGTH_LONG).show();
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

    /**
     * {@inheritDoc}
     * @param result
     */
    @Override
    public void processFinish(HashMap<String, String> result) {

        Log.d("USER_INFO", result.get(Constants.RESULT));

        String output;
        output = result.get(Constants.RESULT);

        try {
            JSONObject c = new JSONObject(output);

            SharedPreferences pref = getSharedPreferences("user_data", 0);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("user_name", c.getString("name"));
            edit.putString("user_id", c.getString("user_id"));
            edit.putString("user_email", c.getString("email"));
            edit.putString("user_full_name", c.getString("full_name"));
            edit.putString("user_permission", c.getString("permission"));
            edit.putString("user_time_created", c.getString("time_created"));
            edit.putString("user_last_activity", c.getString("last_list_activity"));
            edit.apply();

            User.getInstance(this).get_data(this);

        } catch (JSONException e) {
            logout();
        }
    }
}