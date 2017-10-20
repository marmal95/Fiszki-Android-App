package fiszki.xyz.fiszkiapp.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
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
import fiszki.xyz.fiszkiapp.utils.Functions;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.source.Flashcard;
import fiszki.xyz.fiszkiapp.adapters.FlashcardsAdapter;
import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.source.User;

/**
 * Keep and displays all user's favourite flashcards
 */
public class FavouriteFlashcardsActivity extends AppCompatActivity implements AsyncResponse {

    // GUI Components
    private ListView mListView;
    private ProgressBar progressBar;

    private ArrayList<Flashcard> mFlashcards;
    private FlashcardsAdapter mAdapter;

    // SwipeRefresher
    private SwipeRefreshLayout swiperefresh;

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_flashcards);

        setTitle(getString(R.string.myFavFiszki));

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
        this.getUserFavouriteFlashcards();

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
                getUserFavouriteFlashcards();
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

            String[] menuOptions;
            if(User.getInstance(this).getPermission().contains("l"))
                menuOptions = getResources().getStringArray(R.array.favouriteFlashcardsMenuOptions_admin);
            else
                menuOptions = getResources().getStringArray(R.array.favouriteFlashcardsMenuOptions);
            for(int i = 0; i < menuOptions.length; ++i)
                menu.add(Menu.NONE, i, i, menuOptions[i]);
        }
    }

    /**
     * {@inheritDoc}
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        switch(menuItemIndex){
            case 0:
                runFlashcard(info.position);
                break;
            case 1:
                Intent intent = new Intent(FavouriteFlashcardsActivity.this, PreviewFlashcardActivity.class);
                intent.putExtra(Constants.LIST, this.mFlashcards.get(info.position));
                intent.putExtra(Constants.PARENT, Constants.FAV_FLASHCARDS_ACT);
                intent.putExtra(Constants.MODE_KEY, Constants.GLOBAL_MODE);
                startActivity(intent);
                break;
            case 2:
                this.unlikeFlashcard(this.mFlashcards.get(info.position).getHash());
                this.mFlashcards.remove(info.position);
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
                this.downloadFlashcard(this.mFlashcards.get(info.position).getHash());
                break;
            case 5:
                this.removeFlashcard(mFlashcards.get(info.position).getHash());
                break;

        }

        return true;
    }

    /**
     * Gets user's favourite flashcards.
     * Builds POST request and runs ConnectionTask to connect to the server.
     */
    private void getUserFavouriteFlashcards(){
        if(!Functions.isOnline(getApplicationContext()))
            Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else {
            String userToken = "";

            // Encode POST arguments with UTF-8 Encoder
            try{
                userToken = URLEncoder.encode(User.getInstance(this).getUserToken(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "token=" + userToken;
            Log.d("REQUEST_SEND", mRequest);

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.GET_FAVOUR_TASK);
            mConn.execute(getString(R.string.listsByTokenPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Unlike's flashcard given by hash
     * Builds POST request and runs ConnectionTask to connect to the server.
     * @param hash flashcard hash
     */
    private void unlikeFlashcard(String hash){
        if(!Functions.isOnline(getApplicationContext()))
            Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else{
            String userToken = "";

            // Encode POST arguments with UTF-8 Encoder
            try{
                userToken = URLEncoder.encode(User.getInstance(this).getUserToken(), "UTF-8");
                hash = URLEncoder.encode(hash, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "token=" + userToken + "&hash=" + hash;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.UNLIKE_TASK);
            mConn.execute(getString(R.string.unlikeListPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Gets callback after 'Unlike' ConnectionTask.
     * Displays info to user.
     * @param output server response
     */
    private void unlikeFlashcard_callback(String output){
        switch(output){
            case "1":
                Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.unlikedFlashcard), Toast.LENGTH_LONG).show();
                break;
            case "2":
                Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.tokenIncorrect), Toast.LENGTH_LONG).show();
                logout();
                break;
            case "3":
                Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.hashIncorrect), Toast.LENGTH_LONG).show();
                break;
            case "4":
                Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.flashcardAlreadyunliked), Toast.LENGTH_LONG).show();
                break;
            case "5":
                Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.couldNotUnlike), Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Gets callback after 'Get Flashcards' ConnectionTask.
     * Displays info to user.
     * @param output server response
     */
    private void getFavouriteFlashcards_callback(String output){
        JSONObject c;
        JSONArray c_arr;
        try {
            c = new JSONObject(output);
            c_arr = c.getJSONArray("likedLists");

            for (int i = 0; i < c_arr.length(); i++) {
                JSONObject mList = c_arr.getJSONObject(i);
                Flashcard sList = new Flashcard();

                sList.setLangFrom(mList.getString("lang"));
                sList.setLangTo(mList.getString("lang2"));
                sList.setName(mList.getString("name"));
                sList.setHash(mList.getString("hash"));

                this.mFlashcards.add(sList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     * @param result
     */
    @Override
    public void processFinish(HashMap<String, String> result) {

        switch(result.get(Constants.MODE)){
            case Constants.GET_FAVOUR_TASK:
                this.getFavouriteFlashcards_callback(result.get(Constants.RESULT));
                break;
            case Constants.UNLIKE_TASK:
                this.unlikeFlashcard_callback(result.get(Constants.RESULT));
                break;
            case Constants.DOWNLOAD_LIST:
                this.downloadFlashcard_callback(result.get(Constants.RESULT));
                break;
            case Constants.REMOVE_FLASHCARD:
                this.removeFlashcard_callback(result.get(Constants.RESULT));
                break;
        }

        this.mAdapter.notifyDataSetChanged();
        this.progressBar.setVisibility(View.GONE);
        this.swiperefresh.setRefreshing(false);
    }

    /**
     * Downloads flashcard to save it on local device.
     * Build POST request and runs ConnectionTask.
     * @param hash flashcard hash
     */
    private void downloadFlashcard(String hash){
        if(!Functions.isOnline(getApplicationContext()))
            Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
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

    /**
     * Removes user's flashcard.
     * Builds POST request, runs ConnectionTask.
     * @param hash flashcard hash
     */
    private void removeFlashcard(String hash){
        if(!Functions.isOnline(getApplicationContext()))
            Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else{
            String userToken = "";

            // Encode POST arguments with UTF-8 Encoder
            try{
                userToken = URLEncoder.encode(User.getInstance(this).getUserToken(), "UTF-8");
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
                        Toast.makeText(FavouriteFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Verifies the server response on removeFlashcard request.
     * @param output flashcard hash
     */
    private void removeFlashcard_callback(String output){
        switch(output){
            case "1":
                Toast.makeText(FavouriteFlashcardsActivity.this, getResources().getString(R.string.flashcardDeleted), Toast.LENGTH_LONG).show();
                break;

            case "2":
                Toast.makeText(FavouriteFlashcardsActivity.this, getResources().getString(R.string.tokenIncorrect), Toast.LENGTH_LONG).show();
                logout();
                break;

            case "3":
                Toast.makeText(FavouriteFlashcardsActivity.this, getResources().getString(R.string.flashcardNotFound), Toast.LENGTH_LONG).show();
                break;

            case "4":
                Toast.makeText(FavouriteFlashcardsActivity.this, getResources().getString(R.string.nonAuthor), Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void logout() {
        User.getInstance(this).clearUserData(this);

        Toast.makeText(this, getString(R.string.userLogout), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void runFlashcard(int position){
        Intent intent = new Intent(FavouriteFlashcardsActivity.this, DisplayFlashcardActivity.class);
        intent.putExtra(Constants.HASH, mFlashcards.get(position).getHash());
        startActivity(intent);
    }
}
