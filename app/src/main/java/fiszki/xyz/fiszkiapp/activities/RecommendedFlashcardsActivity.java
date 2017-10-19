package fiszki.xyz.fiszkiapp.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.source.Flashcard;
import fiszki.xyz.fiszkiapp.adapters.FlashcardsAdapter;
import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.source.User;

/*
TODO: THIS CODE CAN BE SHORTER
TODO: BY MOVING ALL REDUNDANT
TODO: CALLS IN FUNCTION TASKS
TODO: TO ONE SEPARATE FUNCTION
 */

public class RecommendedFlashcardsActivity extends AppCompatActivity implements AsyncResponse {

    // GUI Components
    private ListView mListView;
    private ProgressBar progressBar;
    private Spinner spinner;

    private FlashcardsAdapter mAdapter;
    private ArrayList<Flashcard> mFlashcards;

    // Ad
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended_flashcards);

        setTitle(getString(R.string.recommendedFiszki));

        // Load Ad
        adView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);


        // Initialize GUI Components
        this.mListView = (ListView)findViewById(R.id.listView);
        this.progressBar = (ProgressBar)findViewById(R.id.progressBar);
        this.spinner = (Spinner)findViewById(R.id.spinner);

        // Initialize Objects & Variables
        this.mFlashcards = new ArrayList<>();

        this.mAdapter = new FlashcardsAdapter(this, R.layout.list_view_item_my_flashcard, this.mFlashcards);
        this.mListView.setAdapter(this.mAdapter);

        // To "long click" on list item
        registerForContextMenu(this.mListView);

        // Creating adapter for spinner
        ArrayAdapter<CharSequence> spinnerAdapter =
                ArrayAdapter.createFromResource(this, R.array.languages, R.layout.spinner_item);

        // Set adapter
        this.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        getRecommendedFlashcards("pl");
                        break;
                    case 1:
                        getRecommendedFlashcards("en");
                        break;
                    case 2:
                        getRecommendedFlashcards("de");
                        break;
                    case 3:
                        getRecommendedFlashcards("es");
                        break;
                    case 4:
                        getRecommendedFlashcards("fr");
                        break;
                    case 5:
                        getRecommendedFlashcards("it");
                        break;
                    case 6:
                        getRecommendedFlashcards("lt");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        this.spinner.setAdapter(spinnerAdapter);
        this.spinner.setSelection(1);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                runFlashcard(position);
            }
        });
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == R.id.listView){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(mFlashcards.get(info.position).getName());

            String[] menuOptions;
            if(User.getInstance(this).getPermission().contains("l"))
                menuOptions = getResources().getStringArray(R.array.recommendedFlashcardsMenuOptions_admin);
            else
                menuOptions = getResources().getStringArray(R.array.recommendedFlashcardsMenuOptions);

            for(int i = 0; i < menuOptions.length; ++i)
                menu.add(Menu.NONE, i, i, menuOptions[i]);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        switch(menuItemIndex){
            case 0:
                runFlashcard(info.position);
                break;
            case 1:
                Intent intent = new Intent(RecommendedFlashcardsActivity.this, PreviewFlashcardActivity.class);
                intent.putExtra(Constants.LIST, this.mFlashcards.get(info.position));
                intent.putExtra(Constants.PARENT, Constants.REC_FLASHCARDS_ACT);
                intent.putExtra(Constants.MODE_KEY, Constants.GLOBAL_MODE);
                startActivity(intent);
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
                this.downloadFlashcard(this.mFlashcards.get(info.position).getHash());
                break;
            case 5:
                this.removeFlashcard(mFlashcards.get(info.position).getHash());
                break;
        }

        return true;
    }

    private void getRecommendedFlashcards(String lang){

        if(!isOnline())
            Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else{

            String userToken = "";

            // Encode POST arguments with UTF-8 Encoder
            try{
                userToken = URLEncoder.encode(User.getInstance(this).getUserToken(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "token=" + userToken + "&name=" + "best of " + lang + "&lang=" + lang;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.GET_RECOM_TASK);
            mConn.execute(getString(R.string.searchByNamePhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    private void likeFlashcard(String hash){
        if(!isOnline())
            Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /*
    Function checks if device is connected to the internet.
    Returns: true - if connected, false - otherwise.
    */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void getRecommendedFlashcards_callback(String output){
        // Clear current data
        this.mFlashcards.clear();
        this.mAdapter.notifyDataSetChanged();

        JSONObject c;
        JSONArray c_arr;
        try {
            c = new JSONObject(output);
            c_arr = c.getJSONArray("arrays");

            for (int i = 0; i < c_arr.length(); i++) {
                JSONArray mList = c_arr.getJSONArray(i);
                Flashcard sList = new Flashcard();

                sList.setName(mList.getString(0));
                sList.setHash(mList.getString(1));
                sList.setLangFrom(mList.getString(2));
                sList.setLangTo(mList.getString(3));
                sList.setStatus(mList.getString(4));
                sList.setStatus(mList.getString(5));

                this.mFlashcards.add(sList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void likeFlashcard_callback(String result){

        switch(result){
            case "1":
                Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.likedFlashcard), Toast.LENGTH_LONG).show();
                break;
            case "2":
                Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.tokenIncorrect), Toast.LENGTH_LONG).show();
                logout();
                break;
            case "3":
                Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.hashIncorrect), Toast.LENGTH_LONG).show();
                break;
            case "4":
                Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.flashcardAlreadyLiked), Toast.LENGTH_LONG).show();
                break;
            case "5":
                Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.couldNotLike), Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void processFinish(HashMap<String, String> result) {

        switch(result.get(Constants.MODE)){
            case Constants.GET_RECOM_TASK:
                this.getRecommendedFlashcards_callback(result.get(Constants.RESULT));
                break;
            case Constants.LIKE_TASK:
                this.likeFlashcard_callback(result.get(Constants.RESULT));
                break;
            case Constants.DOWNLOAD_LIST:
                this.downloadFlashcard_callback(result.get(Constants.RESULT));
                break;
            case Constants.REMOVE_FLASHCARD:
                this.removeFlashcard_callback(result.get(Constants.RESULT));
                break;
        }

        this.progressBar.setVisibility(View.GONE);
        this.mAdapter.notifyDataSetChanged();
    }

    /**
     * Downloads flashcard to save it on local device.
     * Build POST request and runs ConnectionTask.
     * @param hash flashcard hash
     */
    private void downloadFlashcard(String hash){
        if(!isOnline())
            Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
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
        if(!isOnline())
            Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(RecommendedFlashcardsActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
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
                Toast.makeText(RecommendedFlashcardsActivity.this, getResources().getString(R.string.flashcardDeleted), Toast.LENGTH_LONG).show();
                break;

            case "2":
                Toast.makeText(RecommendedFlashcardsActivity.this, getResources().getString(R.string.tokenIncorrect), Toast.LENGTH_LONG).show();
                logout();
                break;

            case "3":
                Toast.makeText(RecommendedFlashcardsActivity.this, getResources().getString(R.string.flashcardNotFound), Toast.LENGTH_LONG).show();
                break;

            case "4":
                Toast.makeText(RecommendedFlashcardsActivity.this, getResources().getString(R.string.nonAuthor), Toast.LENGTH_LONG).show();
                break;
        }
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
        Intent intent = new Intent(RecommendedFlashcardsActivity.this, DisplayFlashcardActivity.class);
        intent.putExtra(Constants.HASH, mFlashcards.get(position).getHash());
        startActivity(intent);
    }
}
