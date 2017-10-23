package fiszki.xyz.fiszkiapp.activities;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.adapters.FlashcardsAdapter;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.source.AppPreferences;
import fiszki.xyz.fiszkiapp.source.Flashcard;
import fiszki.xyz.fiszkiapp.source.RequestBuilder;
import fiszki.xyz.fiszkiapp.source.User;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.utils.Functions;
import fiszki.xyz.fiszkiapp.utils.IntentKey;
import fiszki.xyz.fiszkiapp.utils.IntentValues;


public class MyFlashcardsActivity extends AppCompatActivity implements AsyncResponse {

    private ListView mListView;
    private ProgressBar progressBar;

    private FlashcardsAdapter mAdapter;
    private ArrayList<Flashcard> mFlashcards;

    private SwipeRefreshLayout swipeRefresher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_flashcards);
        setTitle(getString(R.string.myFlashcards));

        this.mFlashcards = new ArrayList<>();

        this.mListView = findViewById(R.id.listView);
        this.progressBar = findViewById(R.id.progressBar);
        this.swipeRefresher = findViewById(R.id.swiperefresh);

        this.mAdapter = new FlashcardsAdapter(this, R.layout.list_view_item_my_flashcard, this.mFlashcards);
        this.mListView.setAdapter(this.mAdapter);
        registerForContextMenu(this.mListView);

        setBackButton();
        setListeners();

        getUserFlashcards();
    }

    private void setBackButton() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(mFlashcards.get(info.position).getName());

            String[] menuOptions = getResources().getStringArray(R.array.myFlashcardsMenuOptions);
            for (int i = 0; i < menuOptions.length; ++i)
                menu.add(Menu.NONE, i, i, menuOptions[i]);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        switch (menuItemIndex) {
            case ContextMenuOption.RUN_FLASHCARD: {
                runFlashcard(info.position);
                break;
            }
            case ContextMenuOption.PREVIEW_FLASHCARD:
                previewFlashcard(info.position);
                break;
            case ContextMenuOption.LIKE_FLASHCARD:
                this.likeFlashcard(this.mFlashcards.get(info.position).getHash());
                break;
            case ContextMenuOption.SHARE_FLASHCARD:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = getString(R.string.shareFlashcardUrl, this.mFlashcards.get(info.position).getHash());
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        this.mFlashcards.get(info.position).getName());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
            case ContextMenuOption.REMOVE_FLASHCARD:
                this.removeFlashcard(this.mFlashcards.get(info.position).getHash());
                this.mFlashcards.remove(info.position);
                break;
            case ContextMenuOption.DOWNLOAD_FLASHCARD:
                this.downloadFlashcard(this.mFlashcards.get(info.position).getHash());
                break;
            default:
                return false;
        }

        return true;
    }

    private void likeFlashcard(String hash) {
        if (!Functions.isOnline(getApplicationContext()))
            Functions.showToast(this, getString(R.string.noConnectionWarning));
        else {
            User user = User.getInstance(getApplicationContext());
            RequestBuilder requestBuilder = new RequestBuilder();
            requestBuilder.putParameter("token", user.getUserToken());
            requestBuilder.putParameter("hash", hash);
            requestBuilder.encodeParameters("UTF-8");

            String request = requestBuilder.buildRequest();
            progressBar.setVisibility(View.VISIBLE);

            final ConnectionTask connection = new ConnectionTask(this, ConnectionTask.Mode.LIKE_FLASHCARD);
            connection.execute(getString(R.string.likeListPhp), request);
            createCancelConnectionHandler(connection);
        }
    }

    private void getUserFlashcards() {
        if (!Functions.isOnline(getApplicationContext()))
            Functions.showToast(this, getString(R.string.noConnectionWarning));
        else {
            User user = User.getInstance(getApplicationContext());
            RequestBuilder requestBuilder = new RequestBuilder();
            requestBuilder.putParameter("token", user.getUserToken());
            requestBuilder.encodeParameters("UTF-8");

            String request = requestBuilder.buildRequest();
            progressBar.setVisibility(View.VISIBLE);

            final ConnectionTask connection = new ConnectionTask(this, ConnectionTask.Mode.GET_MY_FLASHCARDS);
            connection.execute(getString(R.string.listsByTokenPhp), request);
            createCancelConnectionHandler(connection);
        }
    }

    private void removeFlashcard(String hash) {
        if (!Functions.isOnline(getApplicationContext()))
            Functions.showToast(this, getString(R.string.noConnectionWarning));
        else {
            User user = User.getInstance(getApplicationContext());
            RequestBuilder requestBuilder = new RequestBuilder();
            requestBuilder.putParameter("token", user.getUserToken());
            requestBuilder.putParameter("hash", hash);
            requestBuilder.encodeParameters("UTF-8");

            String request = requestBuilder.buildRequest();
            progressBar.setVisibility(View.VISIBLE);

            final ConnectionTask connection = new ConnectionTask(this, ConnectionTask.Mode.REMOVE_FLASHCARD);
            connection.execute(getString(R.string.deleteListPhp), request);
            createCancelConnectionHandler(connection);
        }
    }

    private void downloadFlashcard(String hash) {
        if (!Functions.isOnline(getApplicationContext()))
            Functions.showToast(this, getString(R.string.noConnectionWarning));
        else {
            RequestBuilder requestBuilder = new RequestBuilder();
            requestBuilder.putParameter("hash", hash);
            requestBuilder.encodeParameters("UTF-8");

            String request = requestBuilder.buildRequest();
            progressBar.setVisibility(View.VISIBLE);

            final ConnectionTask connection = new ConnectionTask(this, ConnectionTask.Mode.DOWNLOAD_FLASHCARD);
            connection.execute(getString(R.string.getListContentByHashPhp), request);
            createCancelConnectionHandler(connection);
        }
    }

    private void getFlashcards_callback(String output) {
        JSONObject c;
        try {
            c = new JSONObject(output);
            int listCount = c.getInt("listCount");

            for (int i = 0; i < listCount; ++i) {
                JSONObject mList = c.getJSONObject("list" + String.valueOf(i));
                Flashcard sList = new Flashcard();
                sList.setId(mList.getString("id"));
                sList.setLangFrom(mList.getString("lang"));
                sList.setLangTo(mList.getString("lang2"));
                sList.setName(mList.getString("name"));
                sList.setHash(mList.getString("hash"));
                sList.setTimeCreated(mList.getString("time_created"));
                sList.setTimeEdit(mList.getString("time_edit"));
                sList.setVersion(mList.getString("version"));
                sList.setStatus(mList.getString("status"));
                sList.setRate(mList.getString("rate"));
                this.mFlashcards.add(sList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void likeFlashcard_callback(String result) {
        int responseCode = Integer.valueOf(result);

        switch (responseCode) {
            case ResponseCode.SUCCESS:
                Functions.showToast(this, getString(R.string.likedFlashcard));
                break;
            case ResponseCode.TOKEN_INCORRECT:
                Functions.showToast(this, getString(R.string.tokenIncorrect));
                logout();
                break;
            case ResponseCode.HASH_INCORRECT:
                Functions.showToast(this, getString(R.string.hashIncorrect));
                break;
            case ResponseCode.FLASHCARD_ALREADY_LIKED:
                Functions.showToast(this, getString(R.string.flashcardAlreadyLiked));
                break;
            case ResponseCode.COULD_NOT_LIKE:
                Functions.showToast(this, getString(R.string.couldNotLike));
                break;
        }
    }

    private void removeFlashcard_callback(String output) {
        int responseCode = Integer.valueOf(output);
        switch (responseCode) {
            case ResponseCode.SUCCESS:
                Functions.showToast(this, getString(R.string.flashcardDeleted));
                break;
            case ResponseCode.TOKEN_INCORRECT:
                Functions.showToast(this, getString(R.string.tokenIncorrect));
                logout();
                break;
            case ResponseCode.HASH_INCORRECT:
                Functions.showToast(this, getString(R.string.flashcardNotFound));
                break;
            case ResponseCode.NO_PERMISSION:
                Functions.showToast(this, getString(R.string.nonAuthor));
                break;
        }
    }

    @Override
    public void processRequestResponse(HashMap<ConnectionTask.Key, String> result) {
        ConnectionTask.Mode requestMode = ConnectionTask.Mode.valueOf(result.get(ConnectionTask.Key.REQUEST_MODE));
        String requestResponse = result.get(ConnectionTask.Key.REQUEST_RESPONSE);

        if (requestMode == ConnectionTask.Mode.GET_MY_FLASHCARDS)
            this.getFlashcards_callback(requestResponse);
        else if (requestMode == ConnectionTask.Mode.LIKE_FLASHCARD)
            this.likeFlashcard_callback(requestResponse);
        else if (requestMode == ConnectionTask.Mode.REMOVE_FLASHCARD)
            this.removeFlashcard_callback(requestResponse);
        else if (requestMode == ConnectionTask.Mode.DOWNLOAD_FLASHCARD)
            this.downloadFlashcard_callback(requestResponse);

        mAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        swipeRefresher.setRefreshing(false);
    }

    public void fab_onClick(View view) {
        AppPreferences appPreferences = AppPreferences.getInstance(getApplicationContext());
        boolean first_visit = appPreferences.getBoolean(AppPreferences.Key.FIRST_VISIT, true);
        if (first_visit) {
            appPreferences.put(AppPreferences.Key.FIRST_VISIT, false);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.fiszkiWebInformationAddList));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    addList();
                }
            });
            builder.create().show();
        } else
            addList();
    }

    private String getLangAbbrByIndex(int index) {
        switch (index) {
            case 0:
                return "pl";
            case 1:
                return "en";
            case 2:
                return "de";
            case 3:
                return "es";
            case 4:
                return "fr";
            case 5:
                return "it";
            case 6:
                return "lt";
            default:
                return null;
        }
    }

    private void addList() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_list);

        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, getResources().getStringArray(R.array.languages));

        final EditText name = dialog.findViewById(R.id.cardName);
        final Spinner spinner1 = dialog.findViewById(R.id.spinner1);
        final Spinner spinner2 = dialog.findViewById(R.id.spinner2);
        TextView dialogHeader = dialog.findViewById(R.id.dialogHeader);
        Button ok = dialog.findViewById(R.id.okButton);
        Button cancel = dialog.findViewById(R.id.cancelButton);
        ImageView visit_www_banner = dialog.findViewById(R.id.visit_www_banner);

        dialogHeader.setText(getString(R.string.flashcardName));
        spinner1.setAdapter(langAdapter);
        spinner2.setAdapter(langAdapter);

        if (getString(R.string.locale).equals("pl"))
            visit_www_banner.setImageResource(R.drawable.create_www_pl);
        else
            visit_www_banner.setImageResource(R.drawable.create_www_en);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flashcard flashcard = new Flashcard();
                flashcard.setName(name.getText().toString());
                flashcard.setLangFrom(getLangAbbrByIndex(spinner1.getSelectedItemPosition()));
                flashcard.setLangTo(getLangAbbrByIndex(spinner2.getSelectedItemPosition()));

                dialog.dismiss();

                Intent intent = new Intent(MyFlashcardsActivity.this, PreviewFlashcardActivity.class);
                intent.putExtra(IntentKey.PARENT_ACTIVITY.name(), IntentValues.MY_FLASHCARD_ACTIVITY);
                intent.putExtra(IntentKey.ACTIVITY_MODE.name(), Constants.GLOBAL_MODE);
                intent.putExtra(IntentKey.FLASHCARD.name(), flashcard);
                startActivity(intent);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void downloadFlashcard_callback(String content) {
        File sdCard = new File(android.os.Environment.getExternalStorageDirectory(), "Fiszki");
        if (!sdCard.exists()) {
            if (!sdCard.mkdir()) {
                Functions.showToast(this, getString(R.string.couldNotCreateFolder));
                return;
            }
        }

        try {
            JSONObject c = new JSONObject(content);
            String fileName = c.getString("name");

            BufferedWriter writer = new BufferedWriter(new FileWriter(sdCard.getAbsolutePath()
                    + File.separator + fileName + ".xyz"));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.showToast(this, getString(R.string.downloaded));
    }

    private void logout() {
        User.getInstance(this).clearUserData(this);
        Functions.showToast(this, getString(R.string.userLogout));

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void runFlashcard(int position) {
        Intent intent = new Intent(MyFlashcardsActivity.this, DisplayFlashcardActivity.class);
        intent.putExtra(IntentKey.HASH.name(), mFlashcards.get(position).getHash());
        startActivity(intent);
    }

    private void previewFlashcard(int position) {
        Intent intent = new Intent(MyFlashcardsActivity.this, PreviewFlashcardActivity.class);
        intent.putExtra(IntentKey.FLASHCARD.name(), this.mFlashcards.get(position));
        intent.putExtra(IntentKey.PARENT_ACTIVITY.name(), IntentValues.MY_FLASHCARD_ACTIVITY);
        intent.putExtra(IntentKey.ACTIVITY_MODE.name(), Constants.GLOBAL_MODE);
        startActivity(intent);
    }

    private void setListeners() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                runFlashcard(position);
            }
        });

        swipeRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresher.setRefreshing(true);
                mAdapter.clear();
                getUserFlashcards();
            }
        });
    }

    private void createCancelConnectionHandler(final ConnectionTask connection) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (connection.getStatus() == AsyncTask.Status.RUNNING) {
                    connection.cancel(true);
                    progressBar.setVisibility(View.GONE);
                    Functions.showToast(MyFlashcardsActivity.this, getString(R.string.connectionProblem));
                }
            }
        }, ConnectionTask.TIME_LIMIT_MS);
    }

    private class ContextMenuOption {
        static final int RUN_FLASHCARD = 0;
        static final int PREVIEW_FLASHCARD = 1;
        static final int LIKE_FLASHCARD = 2;
        static final int SHARE_FLASHCARD = 3;
        static final int REMOVE_FLASHCARD = 4;
        static final int DOWNLOAD_FLASHCARD = 5;
    }

    private class ResponseCode {
        static final int SUCCESS = 1;
        static final int TOKEN_INCORRECT = 2;
        static final int HASH_INCORRECT = 3;
        static final int FLASHCARD_ALREADY_LIKED = 4;
        static final int COULD_NOT_LIKE = 5;

        static final int NO_PERMISSION = 4;

    }
}
