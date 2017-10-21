package fiszki.xyz.fiszkiapp.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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

import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.adapters.FlashcardsAdapter;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.source.Flashcard;
import fiszki.xyz.fiszkiapp.source.User;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.utils.Functions;

public class SearchActivity extends AppCompatActivity implements AsyncResponse {

    // GUI Components
    private ListView mListView;
    private ProgressBar progressBar;

    // Data
    private ArrayList<Flashcard> flashcards;
    private FlashcardsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setTitle(getString(R.string.searchFlashcard));

        // Initialize GUI components
        this.mListView = findViewById(R.id.listView);
        this.progressBar = findViewById(R.id.progressBar);

        // Create Data Objects
        this.flashcards = new ArrayList<>();
        this.mAdapter = new FlashcardsAdapter(this, R.layout.list_view_item_search_flashcard, flashcards);

        // Set Data
        this.mListView.setAdapter(mAdapter);
        this.registerForContextMenu(mListView);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                runFlashcard(position);
            }
        });

        this.showSearchDialog();
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
            menu.setHeaderTitle(flashcards.get(info.position).getName());
            String[] menuOptions = getResources().getStringArray(R.array.searchFlashcardsMenuOptions);
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
            case 0:
                runFlashcard(info.position);
                break;
            case 1:
                Intent intent = new Intent(SearchActivity.this, PreviewFlashcardActivity.class);
                intent.putExtra(Constants.LIST, this.flashcards.get(info.position));
                intent.putExtra(Constants.PARENT, Constants.SEARCH_FLASHCARDS_ACT);
                intent.putExtra(Constants.MODE_KEY, Constants.GLOBAL_MODE);
                startActivity(intent);
                break;
            case 2:
                this.likeFlashcard(this.flashcards.get(info.position).getHash());
                break;
            case 3:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = getString(R.string.shareFlashcardUrl, this.flashcards.get(info.position).getHash());
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        this.flashcards.get(info.position).getName());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
            case 4:
                this.downloadFlashcard(this.flashcards.get(info.position).getHash());
                break;

            default:
                return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_activity_menu, menu);
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
            case R.id.action_search:
                this.showSearchDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSearchDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_search_lists);

        // Get dialog ui
        TextView header = dialog.findViewById(R.id.dialogHeader);
        header.setText(getString(R.string.searchFlashcard));

        final EditText query = dialog.findViewById(R.id.query);

        // Query method (name / hash)
        final Spinner spinner1 = dialog.findViewById(R.id.spinner1);

        // Language
        final Spinner spinner2 = dialog.findViewById(R.id.spinner2);

        // Buttons
        Button okButton = dialog.findViewById(R.id.okButton);
        Button canButton = dialog.findViewById(R.id.cancelButton);

        // Set adapters
        ArrayAdapter<String> spinner1Adapter = new ArrayAdapter<>(this, R.layout.spinner_item,
                getResources().getStringArray(R.array.searchOptions));
        ArrayAdapter<String> spinner2Adapter = new ArrayAdapter<>(this, R.layout.spinner_item,
                getResources().getStringArray(R.array.languages));

        spinner1.setAdapter(spinner1Adapter);
        spinner2.setAdapter(spinner2Adapter);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Search by name
                String lang = "";
                if(spinner1.getSelectedItemPosition() == 0){
                    switch(spinner2.getSelectedItemPosition()){
                        case 0:
                            lang = "pl";
                            break;
                        case 1:
                            lang = "en";
                            break;
                        case 2:
                            lang = "de";
                            break;
                        case 3:
                            lang = "es";
                            break;
                        case 4:
                            lang = "fr";
                            break;
                        case 5:
                            lang = "it";
                            break;
                        case 6:
                            lang = "lt";
                            break;
                    }

                    searchByName(query.getText().toString(), lang);
                }
                // Search by hash
                else if(spinner1.getSelectedItemPosition() == 1){
                    searchByHash(query.getText().toString());
                }

                setTitle(query.getText().toString());
                dialog.dismiss();
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

    private void searchByName(String name, String lang){
        if(!Functions.isOnline(getApplicationContext()))
            Toast.makeText(SearchActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else{
            // Encode POST arguments with UTF-8 Encoder
            try{

                name = URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "name=" + name + "&lang=" + lang;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, ConnectionTask.Mode.SEARCH_BY_NAME);
            mConn.execute(getString(R.string.searchListByNamePhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }



    private void searchByName_callback(String output){
        JSONObject c;
        try{
            c = new JSONObject(output);
            Log.d("EXCEPTION_OUT", output);
            int status = c.getInt("status");
            switch(status){

                case -1:
                    Toast.makeText(this, getString(R.string.noFlashcardsFound), Toast.LENGTH_LONG).show();
                    break;

                case 1:
                    JSONArray arr = c.getJSONArray("arrays");
                    for(int i = 0; i < arr.length(); i++){
                        JSONArray fi = arr.getJSONArray(i);
                        Flashcard flashcard = new Flashcard();
                        flashcard.setName(fi.getString(0));
                        flashcard.setHash(fi.getString(1));
                        flashcard.setLangFrom(fi.getString(2));
                        flashcard.setLangTo(fi.getString(3));
                        flashcard.setRate(fi.getString(4));
                        flashcard.setStatus(fi.getString(5));
                        flashcards.add(flashcard);
                    }
                    break;

                case 2:
                    Toast.makeText(this, getString(R.string.noVariables), Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(this, getString(R.string.incorrcectNameFormat), Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    Toast.makeText(this, getString(R.string.languageIncorrect), Toast.LENGTH_LONG).show();
                    break;
                case 5:
                case 6:
                    Toast.makeText(this, getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void searchByHash(String hash){
        if(!Functions.isOnline(getApplicationContext()))
            Toast.makeText(SearchActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
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
            final ConnectionTask mConn = new ConnectionTask(this, ConnectionTask.Mode.SEARCH_BY_HASH);
            mConn.execute(getString(R.string.getListContentByHashPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    private void searchByHash_callback(String output){
        JSONObject c;
        try{
            c = new JSONObject(output);
            Flashcard flashcard = new Flashcard();
            flashcard.setName(c.getString("name"));
            flashcard.setHash(c.getString("hash"));
            flashcard.setLangFrom(c.getString("lang"));
            flashcard.setLangTo(c.getString("lang2"));
            flashcards.add(flashcard);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Likes the flashcard.
     * Builds POST request, makes connection via ConnectionTask.
     * @param hash flashcard hash
     */
    private void likeFlashcard(String hash){
        if(!Functions.isOnline(getApplicationContext()))
            Toast.makeText(SearchActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
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
            final ConnectionTask mConn = new ConnectionTask(this, ConnectionTask.Mode.LIKE_FLASHCARD);
            mConn.execute(getString(R.string.likeListPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Verifies the server response on likeFlashcard request.
     * @param result server response
     */
    private void likeFlashcard_callback(String result){

        switch(result){
            case "1":
                Toast.makeText(SearchActivity.this, getString(R.string.likedFlashcard), Toast.LENGTH_LONG).show();
                break;
            case "2":
                Toast.makeText(SearchActivity.this, getString(R.string.tokenIncorrect), Toast.LENGTH_LONG).show();
                logout();
                break;
            case "3":
                Toast.makeText(SearchActivity.this, getString(R.string.hashIncorrect), Toast.LENGTH_LONG).show();
                break;
            case "4":
                Toast.makeText(SearchActivity.this, getString(R.string.flashcardAlreadyLiked), Toast.LENGTH_LONG).show();
                break;
            case "5":
                Toast.makeText(SearchActivity.this, getString(R.string.couldNotLike), Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Downloads flashcard to save it on local device.
     * Build POST request and runs ConnectionTask.
     * @param hash flashcard hash
     */
    private void downloadFlashcard(String hash){
        if(!Functions.isOnline(getApplicationContext()))
            Toast.makeText(SearchActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
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
            final ConnectionTask mConn = new ConnectionTask(this, ConnectionTask.Mode.DOWNLOAD_FLASHCARD);
            mConn.execute(getString(R.string.getListContentByHashPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
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

    @Override
    public void processRequestResponse(HashMap<ConnectionTask.Key, String> output) {
        ConnectionTask.Mode requestMode = ConnectionTask.Mode.valueOf(output.get(ConnectionTask.Key.REQUEST_MODE));
        String requestResponse = output.get(ConnectionTask.Key.REQUEST_RESPONSE);

        if(requestMode == ConnectionTask.Mode.SEARCH_BY_NAME)
            searchByName_callback(requestResponse);
        else if(requestMode == ConnectionTask.Mode.SEARCH_BY_HASH)
            searchByHash_callback(requestResponse);
        else if(requestMode == ConnectionTask.Mode.LIKE_FLASHCARD)
            likeFlashcard_callback(requestResponse);
        else if(requestMode == ConnectionTask.Mode.DOWNLOAD_FLASHCARD)
            downloadFlashcard_callback(requestResponse);

        progressBar.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
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
        Intent intent = new Intent(SearchActivity.this, DisplayFlashcardActivity.class);
        intent.putExtra(Constants.HASH, flashcards.get(position).getHash());
        startActivity(intent);
        finish();
    }
}
