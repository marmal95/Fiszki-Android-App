package fiszki.xyz.fiszkiapp.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.source.Flashcard;
import fiszki.xyz.fiszkiapp.adapters.FlashcardsAdapter;
import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.source.User;

/**
 * Class displays to user their flashcards.
 * Implementing AsyncResponse to get callback from ConnectionTask.
 */
public class MyFlashcardsActivity extends AppCompatActivity implements AsyncResponse {

    // GUI Components
    private ListView mListView;
    private ProgressBar progressBar;

    private FlashcardsAdapter mAdapter;
    private ArrayList<Flashcard> mFlashcards;

    // SwipeRefresher
    private SwipeRefreshLayout swiperefresh;

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_flashcards);

        setTitle(getString(R.string.myFlashcards));

        // Initialize Variables
        this.mFlashcards = new ArrayList<>();

        // Initialize GUI Components
        this.mListView = (ListView)findViewById(R.id.listView);
        this.progressBar = (ProgressBar)findViewById(R.id.progressBar);
        this.swiperefresh = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);

        this.mAdapter = new FlashcardsAdapter(this, R.layout.list_view_item_my_flashcard, this.mFlashcards);
        this.mListView.setAdapter(this.mAdapter);

        // To "long click" on list item
        registerForContextMenu(this.mListView);

        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch(NullPointerException e){
            e.printStackTrace();
        }

        // Get user's Flashcards from server
        this.getUserFlashcards();

        // Set ListView item click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            runFlashcard(position);
            }
        });

        // Set SwipeRefresh listener
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiperefresh.setRefreshing(true);
                mAdapter.clear();
                getUserFlashcards();
            }
        });
    }

    /**
     * {@inheritDoc}
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == R.id.listView){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(mFlashcards.get(info.position).getName());

            String[] menuOptions = getResources().getStringArray(R.array.myFlashcardsMenuOptions);
            for(int i = 0; i < menuOptions.length; ++i)
                menu.add(Menu.NONE, i, i, menuOptions[i]);
        }
    }

    /**
     * {@inheritDoc}
     * @param item
     * @return true when event handled, false otherwise
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        switch(menuItemIndex){
            case 0: {
                runFlashcard(info.position);
                break;
            }
            case 1:
                share(info.position);
                break;
            case 2:
                this.likeFlashcard(this.mFlashcards.get(info.position).getHash());
                break;
            case 3:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = getString(R.string.shareFlashcardUrl, this.mFlashcards.get(info.position).getHash());
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        this.mFlashcards.get(info.position).getName());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
            case 4:
                this.removeFlashcard(this.mFlashcards.get(info.position).getHash());
                this.mFlashcards.remove(info.position);
                break;
            case 5:
                this.downloadFlashcard(this.mFlashcards.get(info.position).getHash());
                break;

            default:
                return false;
        }

        return true;
    }

    /**
     * Likes the flashcard.
     * Builds POST request, makes connection via ConnectionTask.
     * @param hash flashcard hash
     */
    private void likeFlashcard(String hash){
        if(!isOnline())
            Toast.makeText(MyFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else{
            String userToken = "";

            // Encode POST arguments with UTF-8 Encoder
            try{
                userToken = URLEncoder.encode(User.getInstance(this).getUser_token(), "UTF-8");
                hash = URLEncoder.encode(hash, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "token=" + userToken + "&hash=" + hash;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.LIKE_TASK);
            mConn.execute(getString(R.string.likeListPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Download user's flashcards from server.
     * Builds POST request, runs ConnectionTask.
     */
    private void getUserFlashcards(){
        if(!isOnline())
            Toast.makeText(MyFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else{

            String userToken = "";

            // Encode POST arguments with UTF-8 Encoder
            try{
                userToken = URLEncoder.encode(User.getInstance(this).getUser_token(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "token=" + userToken;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.GET_MINE_TASK);
            mConn.execute(getString(R.string.listsByTokenPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Share user's flashcard
     * @param position item position to share
     */
    private void share(int position){
        Intent intent = new Intent(MyFlashcardsActivity.this, PreviewFlashcardActivity.class);
        intent.putExtra(Constants.LIST, this.mFlashcards.get(position));
        intent.putExtra(Constants.PARENT, Constants.MY_FLASHCARDS_ACT);
        intent.putExtra(Constants.MODE_KEY, Constants.GLOBAL_MODE);
        startActivity(intent);
    }

    /**
     * Removes user's flashcard.
     * Builds POST request, runs ConnectionTask.
     * @param hash flashcard hash
     */
    private void removeFlashcard(String hash){
        if(!isOnline())
            Toast.makeText(MyFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else{
            String userToken = "";

            // Encode POST arguments with UTF-8 Encoder
            try{
                userToken = URLEncoder.encode(User.getInstance(this).getUser_token(), "UTF-8");
                hash = URLEncoder.encode(hash, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "token=" + userToken + "&hash=" + hash;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.REMOVE_FLASHCARD);
            mConn.execute(getString(R.string.deleteListPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Verifies if device is connected to the internet.
     * @return true - if online, false - otherwise
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Verifies the server response on getFlashcards request.
     * @param output server response
     */
    private void getFlashcards_callback(String output){
        JSONObject c;
        try{
            c = new JSONObject(output);
            int listCount = c.getInt("listCount");

            for(int i = 0; i < listCount; ++i){
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

    /**
     * Verifies the server response on likeFlashcard request.
     * @param result server response
     */
    private void likeFlashcard_callback(String result){

        switch(result){
            case "1":
                Toast.makeText(MyFlashcardsActivity.this, getString(R.string.likedFlashcard), Toast.LENGTH_LONG).show();
                break;
            case "2":
                Toast.makeText(MyFlashcardsActivity.this, getString(R.string.tokenIncorrect), Toast.LENGTH_LONG).show();
                logout();
                break;
            case "3":
                Toast.makeText(MyFlashcardsActivity.this, getString(R.string.hashIncorrect), Toast.LENGTH_LONG).show();
                break;
            case "4":
                Toast.makeText(MyFlashcardsActivity.this, getString(R.string.flashcardAlreadyLiked), Toast.LENGTH_LONG).show();
                break;
            case "5":
                Toast.makeText(MyFlashcardsActivity.this, getString(R.string.couldNotLike), Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Verifies the server response on removeFlashcard request.
     * @param output flashcard hash
     */
    private void removeFlashcard_callback(String output){
        switch(output){
            case "1":
                Toast.makeText(MyFlashcardsActivity.this, getResources().getString(R.string.flashcardDeleted), Toast.LENGTH_LONG).show();
                break;

            case "2":
                Toast.makeText(MyFlashcardsActivity.this, getResources().getString(R.string.tokenIncorrect), Toast.LENGTH_LONG).show();
                logout();
                break;

            case "3":
                Toast.makeText(MyFlashcardsActivity.this, getResources().getString(R.string.flashcardNotFound), Toast.LENGTH_LONG).show();
                break;

            case "4":
                Toast.makeText(MyFlashcardsActivity.this, getResources().getString(R.string.nonAuthor), Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * {@inheritDoc}
     * @param result
     */
    @Override
    public void processFinish(HashMap<String, String> result) {

        switch(result.get(Constants.MODE)){
            case Constants.GET_MINE_TASK:
                this.getFlashcards_callback(result.get(Constants.RESULT));
                break;
            case Constants.LIKE_TASK:
                this.likeFlashcard_callback(result.get(Constants.RESULT));
                break;
            case Constants.REMOVE_FLASHCARD:
                this.removeFlashcard_callback(result.get(Constants.RESULT));
                break;
            case Constants.DOWNLOAD_LIST:
                this.downloadFlashcard_callback(result.get(Constants.RESULT));
        }

        this.mAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        swiperefresh.setRefreshing(false);
    }

    /**
     * Handles FloatingActionButton click event.
     * Displays dialog to create new flashcard.
     * @param view clicked view
     */
    public void fab_onClick(View view) {
        // Check if first visit
        SharedPreferences sharedPreferences = getSharedPreferences("settings", 0);
        boolean first_visit = sharedPreferences.getBoolean("first_visit", true);
        if(first_visit){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("first_visit", false);
            editor.apply();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.fiszkiWebInformationAddList));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    addList();
                }
            });

            // Create the AlertDialog object and return it
            builder.create().show();
        }
        else
            addList();
    }

    private void addList(){
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_list);

        // Create Adapter for Spinner
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, getResources().getStringArray(R.array.languages));

        // dialog components
        final EditText name = (EditText)dialog.findViewById(R.id.cardName);
        final Spinner spinner1 = (Spinner)dialog.findViewById(R.id.spinner1);
        final Spinner spinner2 = (Spinner)dialog.findViewById(R.id.spinner2);
        TextView dialogHeader = (TextView)dialog.findViewById(R.id.dialogHeader);
        Button ok = (Button)dialog.findViewById(R.id.okButton);
        Button cancel = (Button)dialog.findViewById(R.id.cancelButton);
        ImageView visit_www_banner = (ImageView)dialog.findViewById(R.id.visit_www_banner);

        if(getString(R.string.locale).equals("pl"))
            visit_www_banner.setImageResource(R.drawable.create_www_pl);
        else
            visit_www_banner.setImageResource(R.drawable.create_www_en);

        dialogHeader.setText(getString(R.string.flashcardName));

        spinner1.setAdapter(langAdapter);
        spinner2.setAdapter(langAdapter);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flashcard flashcard = new Flashcard();
                flashcard.setName(name.getText().toString());

                switch(spinner1.getSelectedItemPosition()){
                    case 0:
                        flashcard.setLangFrom("pl");
                        break;
                    case 1:
                        flashcard.setLangFrom("en");
                        break;
                    case 2:
                        flashcard.setLangFrom("de");
                        break;
                    case 3:
                        flashcard.setLangFrom("es");
                        break;
                    case 4:
                        flashcard.setLangFrom("fr");
                        break;
                    case 5:
                        flashcard.setLangFrom("it");
                        break;
                    case 6:
                        flashcard.setLangFrom("lt");
                        break;
                }

                switch(spinner2.getSelectedItemPosition()){
                    case 0:
                        flashcard.setLangTo("pl");
                        break;
                    case 1:
                        flashcard.setLangTo("en");
                        break;
                    case 2:
                        flashcard.setLangTo("de");
                        break;
                    case 3:
                        flashcard.setLangTo("es");
                        break;
                    case 4:
                        flashcard.setLangTo("fr");
                        break;
                    case 5:
                        flashcard.setLangTo("it");
                        break;
                    case 6:
                        flashcard.setLangTo("lt");
                        break;
                }

                dialog.dismiss();

                // Run new Activity to add words
                Intent intent = new Intent(MyFlashcardsActivity.this, PreviewFlashcardActivity.class);
                intent.putExtra(Constants.PARENT, Constants.MY_FLASHCARDS_ACT);
                intent.putExtra(Constants.MODE_KEY, Constants.GLOBAL_MODE);
                intent.putExtra(Constants.LIST, flashcard);
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

    /**
     * Downloads flashcard to save it on local device.
     * Build POST request and runs ConnectionTask.
     * @param hash flashcard hash
     */
    private void downloadFlashcard(String hash){
        if(!isOnline())
            Toast.makeText(MyFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else{
            // Encode POST arguments with UTF-8 Encoder
            try{

                hash = URLEncoder.encode(hash, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "hash=" + hash;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.DOWNLOAD_LIST);
            mConn.execute(getString(R.string.getListContentByHashPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MyFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Callback from downloadFlashcard task.
     * Saves the flashcard content on the device.
     * @param content server response
     */
    private void downloadFlashcard_callback(String content){
        File sdCard = new File(android.os.Environment.getExternalStorageDirectory(), "Fiszki");
        // Create Fiszki folder if does not exist
        if(!sdCard.exists()) {
            if (!sdCard.mkdir()) {
                Toast.makeText(this, getString(R.string.couldNotCreateFolder),
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        String fileName;
        try {
            JSONObject c = new JSONObject(content);
            fileName = c.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(sdCard.getAbsolutePath()
                + File.separator + fileName + ".xyz"));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Toast.makeText(this, getString(R.string.downloaded), Toast.LENGTH_LONG).show();
    }

    private void logout() {
        User.getInstance(this).clear_data(this);

        Toast.makeText(this, getString(R.string.userLogout), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void runFlashcard(int position){
        Intent intent = new Intent(MyFlashcardsActivity.this, DisplayFlashcardActivity.class);
        intent.putExtra(Constants.HASH, mFlashcards.get(position).getHash());
        startActivity(intent);
    }
}
