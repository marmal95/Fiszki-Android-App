package fiszki.xyz.fiszkiapp.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.source.Functions;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.source.User;

public class SettingsActivity extends AppCompatActivity implements AsyncResponse {

    // GUI
    private ProgressBar progressBar;

    // Display Order
    private Switch displayInSequence;
    private Switch displayRevSequence;
    private Switch displayRandomly;

    // Display settings
    private Switch repeatNotKnown;
    private Switch reverseLanguages;
    private Switch createRevList;

    // Input mode
    private Switch decisionMode;
    private Switch writeMode;

    // Text to speech
    private Switch ttsEnabled;
    private SeekBar ttsSpeed;
    private Switch skipBrackets;
    private Switch autoReadWords;
    private Switch autoReadTrans;

    private SeekBar fontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle(getString(R.string.settings));

        initializeGUI();
        loadSettings();
        setListeners();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.saveSettingsQuestion));

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveSettings();
                dialog.dismiss();
                SettingsActivity.super.onBackPressed();
            }
        });

        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SettingsActivity.super.onBackPressed();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * {@inheritDoc}
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveSettings();
                return true;
        }
        return false;
    }


    private void initializeGUI() {
        // Initialize GUI components
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        displayInSequence = (Switch) findViewById(R.id.displayInSequence);
        displayRevSequence = (Switch) findViewById(R.id.displayRevSequence);
        displayRandomly = (Switch) findViewById(R.id.displayRandomly);

        repeatNotKnown = (Switch) findViewById(R.id.repeatNotKnown);
        reverseLanguages = (Switch) findViewById(R.id.reverseLanguages);
        createRevList = (Switch) findViewById(R.id.createRevisionList);

        decisionMode = (Switch) findViewById(R.id.decisionMode);
        writeMode = (Switch) findViewById(R.id.writeMode);

        ttsEnabled = (Switch) findViewById(R.id.ttsEnabled);
        ttsSpeed = (SeekBar) findViewById(R.id.ttsSpeed);
        skipBrackets = (Switch) findViewById(R.id.skipBrackets);
        autoReadWords = (Switch)findViewById(R.id.autoReadWords);
        autoReadTrans = (Switch) findViewById(R.id.autoReadTrans);

        fontSize = (SeekBar)findViewById(R.id.fontSizeBar);
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", 0);

        displayInSequence.setChecked(sharedPreferences.getBoolean(Constants.DISP_SEQUENCE, true));
        displayRevSequence.setChecked(sharedPreferences.getBoolean(Constants.DISP_REV_SEQUENCE, false));
        displayRandomly.setChecked(sharedPreferences.getBoolean(Constants.DISP_RANDOM, false));

        repeatNotKnown.setChecked(sharedPreferences.getBoolean(Constants.REP_NOT_KNOWN, false));
        reverseLanguages.setChecked(sharedPreferences.getBoolean(Constants.REV_LANGUAGES, false));
        createRevList.setChecked(sharedPreferences.getBoolean(Constants.CREATE_REV_LIST, false));

        decisionMode.setChecked(sharedPreferences.getBoolean(Constants.DECISION_MODE, true));
        writeMode.setChecked(sharedPreferences.getBoolean(Constants.WRITE_MODE, false));

        ttsEnabled.setChecked(sharedPreferences.getBoolean(Constants.TTS_ENABLED, false));
        ttsSpeed.setProgress(sharedPreferences.getInt(Constants.TTS_SPEED, 10));
        skipBrackets.setChecked(sharedPreferences.getBoolean(Constants.SKIP_BRACKETS, false));
        autoReadWords.setChecked(sharedPreferences.getBoolean(Constants.AUTO_READ_WORDS, false));
        autoReadTrans.setChecked(sharedPreferences.getBoolean(Constants.AUTO_READ_TRANS, false));

        fontSize.setProgress(sharedPreferences.getInt(Constants.FONT_SIZE, 10));

        if (!ttsEnabled.isChecked()) {
            ttsSpeed.setEnabled(false);
            skipBrackets.setEnabled(false);
            autoReadWords.setEnabled(false);
            autoReadTrans.setEnabled(false);
        }
    }

    private void setListeners() {
        displayInSequence.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    displayRevSequence.setChecked(false);
                    displayRandomly.setChecked(false);
                } else {
                    if (!displayRevSequence.isChecked() && !displayRandomly.isChecked())
                        displayInSequence.setChecked(true);
                }
            }
        });

        displayRevSequence.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    displayInSequence.setChecked(false);
                    displayRandomly.setChecked(false);
                } else {
                    if (!displayInSequence.isChecked() && !displayRandomly.isChecked())
                        displayRevSequence.setChecked(true);
                }
            }
        });

        displayRandomly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    displayInSequence.setChecked(false);
                    displayRevSequence.setChecked(false);
                } else {
                    if (!displayInSequence.isChecked() && !displayRevSequence.isChecked())
                        displayRandomly.setChecked(true);
                }

            }
        });


        decisionMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    writeMode.setChecked(false);
                else {
                    if (!writeMode.isChecked())
                        decisionMode.setChecked(true);
                }
            }
        });

        writeMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    decisionMode.setChecked(false);
                else {
                    if (!decisionMode.isChecked())
                        writeMode.setChecked(true);
                }
            }
        });

        ttsEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ttsSpeed.setEnabled(true);
                    skipBrackets.setEnabled(true);
                    autoReadWords.setEnabled(true);
                    autoReadTrans.setEnabled(true);
                    Toast.makeText(SettingsActivity.this, getString(R.string.ttsOnTextClickInfo), Toast.LENGTH_LONG).show();
                } else {
                    ttsSpeed.setEnabled(false);
                    skipBrackets.setEnabled(false);
                    autoReadWords.setEnabled(false);
                    autoReadTrans.setEnabled(false);
                }
            }
        });
    }


    private void saveSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", 0);
        SharedPreferences.Editor sharedPrefereEditor = sharedPreferences.edit();

        sharedPrefereEditor.putBoolean(Constants.DISP_SEQUENCE, displayInSequence.isChecked());
        sharedPrefereEditor.putBoolean(Constants.DISP_REV_SEQUENCE, displayRevSequence.isChecked());
        sharedPrefereEditor.putBoolean(Constants.DISP_RANDOM, displayRandomly.isChecked());

        sharedPrefereEditor.putBoolean(Constants.REP_NOT_KNOWN, repeatNotKnown.isChecked());
        sharedPrefereEditor.putBoolean(Constants.REV_LANGUAGES, reverseLanguages.isChecked());
        sharedPrefereEditor.putBoolean(Constants.CREATE_REV_LIST, createRevList.isChecked());

        sharedPrefereEditor.putBoolean(Constants.DECISION_MODE, decisionMode.isChecked());
        sharedPrefereEditor.putBoolean(Constants.WRITE_MODE, writeMode.isChecked());

        sharedPrefereEditor.putBoolean(Constants.TTS_ENABLED, ttsEnabled.isChecked());
        sharedPrefereEditor.putInt(Constants.TTS_SPEED, ttsSpeed.getProgress());
        sharedPrefereEditor.putBoolean(Constants.SKIP_BRACKETS, skipBrackets.isChecked());
        sharedPrefereEditor.putBoolean(Constants.AUTO_READ_WORDS, autoReadWords.isChecked());
        sharedPrefereEditor.putBoolean(Constants.AUTO_READ_TRANS, autoReadTrans.isChecked());

        sharedPrefereEditor.putInt(Constants.FONT_SIZE, fontSize.getProgress());

        sharedPrefereEditor.apply();
    }

    public void logout_onClick(View view) {
        logout();
    }

    private void logout() {
        User.getInstance(this).clearUserData(this);

        Toast.makeText(this, getString(R.string.userLogout), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void deleteAccount_onClick(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_password_input);

        TextView header = (TextView) dialog.findViewById(R.id.dialogHeader);
        header.setText(getString(R.string.deleteAccount));

        final EditText password = (EditText) dialog.findViewById(R.id.name);
        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        Button canButton = (Button) dialog.findViewById(R.id.cancelButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                deleteAccount(password.getText().toString());
                logout();
            }
        });

        canButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void changeName(String userName){
        if (!Functions.isOnline(getApplicationContext()))
            Toast.makeText(SettingsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else {

            // Encode both POST arguments(email and password) with UTF-8 Encoder
            String userToken = User.getInstance(this).getUserToken();
            try {
                userToken = URLEncoder.encode(userToken, "UTF-8");
                userName = URLEncoder.encode(userName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "token=" + userToken + "&fullName=" + userName;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.CHANGE_USR_NAME);
            mConn.execute(getString(R.string.changeFullNamePhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SettingsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    private void changeName_callback(String output) {
        switch(output){
            case "1":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.userNameChanged), Toast.LENGTH_LONG).show();
                break;
            case "0":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
                break;
            case "2":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.fillNewName), Toast.LENGTH_LONG).show();
                break;
            case "3":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.incorrectFormat), Toast.LENGTH_LONG).show();
                break;
            case "4":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.userNotExist), Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void deleteAccount(String userPassw) {
        {
            if (!Functions.isOnline(getApplicationContext()))
                Toast.makeText(SettingsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
            else {

                // Encode both POST arguments(email and password) with UTF-8 Encoder
                String userToken = User.getInstance(this).getUserToken();
                String userEmail = User.getInstance(this).getEmail();
                try {
                    userToken = URLEncoder.encode(userToken, "UTF-8");
                    userEmail = URLEncoder.encode(userEmail, "UTF-8");
                    userPassw = URLEncoder.encode(userPassw, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Build POST Request
                String mRequest = "token=" + userToken + "&email=" + userEmail +  "&password=" + userPassw;

                // Create and run ConnectionTask
                final ConnectionTask mConn = new ConnectionTask(this, Constants.DELETE_ACCOUNT);
                mConn.execute(getString(R.string.deleteAccountPhp), mRequest);

                progressBar.setVisibility(View.VISIBLE);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mConn.getStatus() == AsyncTask.Status.RUNNING) {
                            mConn.cancel(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SettingsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                        }
                    }
                }, 10000);
            }
        }
    }

    private void deleteAccount_callback(String output) {
        switch (output) {
            case "0":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
                break;

            case "1":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.accountDeleted), Toast.LENGTH_LONG).show();
                break;

            case "2":
            case "3":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.incorrectFormat), Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void processFinish(HashMap<String, String> output) {
        switch (output.get(Constants.MODE)) {
            case Constants.DELETE_ACCOUNT:
                deleteAccount_callback(output.get(Constants.RESULT));
                break;
            case Constants.CHANGE_USR_NAME:
                changeName_callback(output.get(Constants.RESULT));
                break;
            case Constants.CHANGE_USR_PASS:
                changePassword_callback(output.get(Constants.RESULT));
                        break;
        }

        progressBar.setVisibility(View.GONE);
    }

    public void changeName_onClick(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_name);

        TextView header = (TextView) dialog.findViewById(R.id.dialogHeader);
        header.setText(getString(R.string.changeUsername));

        final EditText name = (EditText) dialog.findViewById(R.id.name);
        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        Button canButton = (Button) dialog.findViewById(R.id.cancelButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                changeName(name.getText().toString());
            }
        });

        canButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void changePassword_onClick(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_change_password);

        TextView header = (TextView) dialog.findViewById(R.id.dialogHeader);
        header.setText(getString(R.string.changeUsername));

        final EditText oldPass = (EditText) dialog.findViewById(R.id.oldPass);
        final EditText newPass = (EditText) dialog.findViewById(R.id.newPass);
        final EditText repNewPass = (EditText) dialog.findViewById(R.id.repNewPass);
        Button okButton = (Button) dialog.findViewById(R.id.okButton);
        Button canButton = (Button) dialog.findViewById(R.id.cancelButton);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    dialog.dismiss();
                    changePassword(oldPass.getText().toString(), newPass.getText().toString(),
                            repNewPass.getText().toString());
            }
        });

        canButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void changePassword(String oldPass, String newPass, String repNewPass){
        if (!Functions.isOnline(getApplicationContext()))
            Toast.makeText(SettingsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else {

            // Encode both POST arguments(email and password) with UTF-8 Encoder
            String userToken = User.getInstance(this).getUserToken();
            String userEmail = User.getInstance(this).getUserToken();
            try {
                userToken = URLEncoder.encode(userToken, "UTF-8");
                userEmail = URLEncoder.encode(userEmail, "UTF-8");
                oldPass = URLEncoder.encode(oldPass, "UTF-8");
                newPass = URLEncoder.encode(newPass, "UTF-8");
                repNewPass = URLEncoder.encode(repNewPass, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "oldPassword=" + oldPass + "&newPassword=" + newPass +
                    "&newPasswordRepeat=" + repNewPass + "&token=" +
                    userToken + "&email=" + userEmail;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.CHANGE_USR_PASS);
            mConn.execute(getString(R.string.changePasswordPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SettingsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    private void changePassword_callback(String output){
        switch (output) {

            case "0":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
                break;

            case "1":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.passwordHasBeenChanged), Toast.LENGTH_LONG).show();
                logout();
                break;

            case "2":
            case "6":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.incorrectActualPassword), Toast.LENGTH_LONG).show();
                break;

            case "3":
                Toast.makeText(SettingsActivity.this,  getResources().getString(R.string.passwordTooShort), Toast.LENGTH_LONG).show();
                break;

            case "4":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.passwordsDifferent), Toast.LENGTH_LONG).show();
                break;

            case "5":
                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.newPasswordTheSameToOld), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
