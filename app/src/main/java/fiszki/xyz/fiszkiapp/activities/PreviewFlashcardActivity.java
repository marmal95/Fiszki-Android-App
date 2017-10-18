package fiszki.xyz.fiszkiapp.activities;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.source.Flashcard;
import fiszki.xyz.fiszkiapp.utils.Pair;
import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.source.User;
import fiszki.xyz.fiszkiapp.adapters.WordsAdapter;

/**
 * Displays preview of the flashcard.
 * Implementing AsyncResponse to get callback from ConnectionTask.
 */
public class PreviewFlashcardActivity extends AppCompatActivity implements AsyncResponse {

    // GUI Components
    private ListView mListView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private Flashcard flashcard;
    private FloatingActionButton fab;

    private WordsAdapter mAdapter;

    private ArrayList<Pair> mWords;

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_flashcard);

        setTitle(getString(R.string.flashcardPreview));

        // Initialize GUI Components
        this.mListView = (ListView)findViewById(R.id.listView);
        this.progressBar = (ProgressBar)findViewById(R.id.progressBar);
        this.fab = (FloatingActionButton)findViewById(R.id.fab);

        // Initialize Objects & Variables
        this.mWords = new ArrayList<>();

        this.mAdapter = new WordsAdapter(this, this.mWords);
        this.mListView.setAdapter(this.mAdapter);


        // We run local flashcard (device may be offline)
        if(getIntent().getIntExtra(Constants.MODE_KEY, Constants.NULL_MODE) == Constants.LOCAL_MODE){
            getFlashcardContent_callback(getIntent().getStringExtra(Constants.CONTENT));
            mAdapter.notifyDataSetChanged();
        }
        // Here we run global mode (device must me online)
        else if(getIntent().getIntExtra(Constants.MODE_KEY, Constants.NULL_MODE) == Constants.GLOBAL_MODE) {
            // Check who was the parent

            // If user is owner of list or has enough permission
            // to edit list or delete - launch additional context menu
            // and show fab button to add new words
            if(getIntent().getStringExtra(Constants.PARENT).equals(Constants.MY_FLASHCARDS_ACT)
                || User.getInstance(this).getPermission().contains("l")) {
                registerForContextMenu(mListView);
                fab.setVisibility(View.VISIBLE);

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addNewWord();
                    }
                });
            }

            // In global mode there always exist flashcard passed in intent
            flashcard = getIntent().getParcelableExtra(Constants.LIST);

            // If hash == null it means we create new flashcard
            // so there is no content yet
            if(flashcard.getHash() != null)
                getFlashcardContent(flashcard.getHash());
            setTitle(flashcard.getName());
        }
    }

    /**
     * {@inheritDoc}
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if(v.getId() == R.id.listView){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

            Pair temp = (Pair)(mAdapter.getItem(info.position));
            menu.setHeaderTitle(temp.getLeftValue());
            String[] menuOptions = getResources().getStringArray(R.array.previewFlashcardsMenuOptions);

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
    @SuppressWarnings("unchecked")
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();

        switch(menuItemIndex){
            case 0:
                editWord(info.position);
                mAdapter.getFilter().filter("");
                break;
            case 1:
                Pair t = (Pair) mAdapter.getItem(info.position);
                mWords.remove(t);
                mAdapter.getFilter().filter(searchView.getQuery().toString());
                mAdapter.notifyDataSetChanged();
                break;
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
        if(getIntent().getStringExtra(Constants.PARENT).equals(Constants.MY_FLASHCARDS_ACT)
                || User.getInstance(this).getPermission().contains("l"))
            inflater.inflate(R.menu.flashcard_edit_activity_menu, menu);
        else
            inflater.inflate(R.menu.flashcard_preview_activity_menu, menu);

        searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });

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
            case R.id.action_editName:
                this.editFlashcardName();
                return true;
            case R.id.action_search:
                return true;
            case R.id.action_addWord:
                this.addNewWord();
                return true;
            case R.id.action_upload:
                this.sendToServer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Prepare data to build POST request.
     * Runs editList or addList function.
     */
    private void sendToServer(){
        String[] words = new String[mWords.size()];
        String[] trans = new String[mWords.size()];

        for(int i = 0; i < mWords.size(); i++){
            Pair p = mWords.get(i);
            words[i] = p.getLeftValue();
            trans[i] = p.getRightValue();
        }

        JSONArray json_words = new JSONArray(Arrays.asList(words));
        JSONArray json_trans = new JSONArray(Arrays.asList(trans));

        String strArr_words = json_words.toString();
        String strArr_trans = json_trans.toString();

        if(this.flashcard.getHash() != null && !this.flashcard.getHash().equals(""))
            this.editList(strArr_words, strArr_trans);
        else
        {
            this.addList(strArr_words,strArr_trans);
        }
    }

    /**
     * Download flashcard content from the server.
     * Builds POST request and runs ConnectionTask.
     * @param hash flashcard hash
     */
    private void getFlashcardContent(String hash){
        if(!isOnline())
            Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
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
            final ConnectionTask mConn = new ConnectionTask(this, Constants.GET_FLASHCARD_CONTENT);
            mConn.execute(getString(R.string.getListContentByHashPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Sends edited flashcard to the server.
     * Builds POST request and runs ConnectionTask.
     * @param words JSONArray with words to translate
     * @param trans JSONArray with words translated
     */
    private void editList(String words, String trans) {
        if (!isOnline())
            Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else {
            // Encode POST arguments with UTF-8 Encoder
            String hash = this.flashcard.getHash();
            String name = this.flashcard.getName();
            String token = User.getInstance(this).getUser_token();
            try {
                hash = URLEncoder.encode(hash, "UTF-8");
                words = URLEncoder.encode(words, "UTF-8");
                trans = URLEncoder.encode(trans, "UTF-8");
                token = URLEncoder.encode(token, "UTF-8");
                name = URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "hash=" + hash + "&token=" + token + "&words=" + words
                    + "&translations=" + trans + "&name=" + name;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.EDIT_LIST);
            mConn.execute(getString(R.string.editListPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Sends new flashcard to the server.
     * @param words JSONArray with words to translate
     * @param trans JSONArray with words translated
     */
    private void addList(String words, String trans){
        if (!isOnline())
            Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else {
            // Encode POST arguments with UTF-8 Encoder
            String name = this.flashcard.getName();
            String token = User.getInstance(this).getUser_token();
            try {
                words = URLEncoder.encode(words, "UTF-8");
                trans = URLEncoder.encode(trans, "UTF-8");
                token = URLEncoder.encode(token, "UTF-8");
                name = URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "token=" + token + "&words=" + words
                    + "&translations=" + trans + "&name=" + name
                    + "&lang=" + flashcard.getLangFrom() + "&lang2=" + flashcard.getLangTo();

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, Constants.ADD_LIST);
            mConn.execute(getString(R.string.addListPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, 10000);
        }
    }

    /**
     * Opens dialog to edit flashcard name.
     */
    private void editFlashcardName(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_name);

        final EditText cardName = (EditText)dialog.findViewById(R.id.name);
        cardName.setText(flashcard.getName());

        TextView dialogHeader = (TextView)dialog.findViewById(R.id.dialogHeader);
        Button okButton = (Button)dialog.findViewById(R.id.okButton);
        Button canButton = (Button)dialog.findViewById(R.id.cancelButton);

        dialogHeader.setText(getString(R.string.flashcardName));

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTitle(cardName.getText().toString());
                flashcard.setName(cardName.getText().toString());
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

    /**
     * Verifies if device is conntected to the internet.
     * @return true - if online, false - otherwise
     */
    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Verifies server response to the getFlashcardContent request
     * @param output server response
     */
    private void getFlashcardContent_callback(String output){
        JSONObject c;
        JSONArray wordsToTranslate;
        JSONArray wordsTranslated;

        try {
            c = new JSONObject(output);
            wordsToTranslate = c.getJSONArray("words");
            wordsTranslated = c.getJSONArray("translations");

            for(int i = 0; i < wordsToTranslate.length(); i++) {
                String word = wordsToTranslate.getString(i);
                String trans = wordsTranslated.getString(i);

                try {
                    word = URLDecoder.decode(word, "UTF-8");
                    trans = URLDecoder.decode(trans, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                Pair p = new Pair();
                p.setLeftValue(word);
                p.setRightValue(trans);
                mWords.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifies server response to the editList request
     * @param output server response
     */
    private void editList_callback(String output){
        switch(output){
            case "0":
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
                break;
            case "1":
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.flashcardSaved), Toast.LENGTH_LONG).show();
                break;
            case "2":
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.noDataToSave), Toast.LENGTH_LONG).show();
                break;
            case "3":
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.tokenIncorrect), Toast.LENGTH_LONG).show();
                break;
            case "4":
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.hashIncorrect), Toast.LENGTH_LONG).show();
                break;
            case "5":
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.nonAuthor), Toast.LENGTH_LONG).show();
                break;
            case "6":
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.decodeWordsProblem), Toast.LENGTH_LONG).show();
                break;
            case "7":
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.decodeWordsProblem), Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Verifies server response to the addList request
     * @param output server response
     */
    private void addList_callback(String output){
        int listStatus = -1;
        String hash = null;
        JSONObject c;
        try {
            c = new JSONObject(output);
            listStatus = c.getInt("status");
            hash = c.getString("hash");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch(listStatus){
            case 1:
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.flashcardAdded), Toast.LENGTH_LONG).show();
                flashcard.setHash(hash);
                break;
            case 2:
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.cantAddFlashcard), Toast.LENGTH_LONG).show();
                break;
            default:
                Log.d("ADD_LIST_RESPONSE_CODE", String.valueOf(listStatus));
                Toast.makeText(PreviewFlashcardActivity.this, getResources().getString(R.string.sthHasGoneWrong), Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Shows dialog to enter new word to add it to flashcard
     */
    private void addNewWord(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_word);

        TextView dialogHeader = (TextView) dialog.findViewById(R.id.dialogHeader);
        final EditText word = (EditText) dialog.findViewById(R.id.wordToTranslate);
        final EditText trans = (EditText) dialog.findViewById(R.id.wordTranslation);
        Button okButton = (Button)dialog.findViewById(R.id.okButton);
        Button canButton = (Button)dialog.findViewById(R.id.cancelButton);

        dialogHeader.setText(getString(R.string.addWord));

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair p = new Pair();
                p.setLeftValue(word.getText().toString());
                p.setRightValue(trans.getText().toString());

                mWords.add(p);
                mAdapter.notifyDataSetChanged();
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

    /**
     * Edit word on the given position.
     * Shows dialog to edit words.
     * @param position position to edit
     */
    @SuppressWarnings("unchecked")
    private void editWord(int position){

        // Filtered index
         Pair temp = (Pair)mAdapter.getItem(position);

        // Index in main array
        final int index = mWords.indexOf(temp);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_word);

        TextView dialogHeader = (TextView) dialog.findViewById(R.id.dialogHeader);
        final EditText word = (EditText) dialog.findViewById(R.id.wordToTranslate);
        final EditText trans = (EditText) dialog.findViewById(R.id.wordTranslation);

        word.setText(temp.getLeftValue());
        trans.setText(temp.getRightValue());

        Button okButton = (Button)dialog.findViewById(R.id.okButton);
        Button canButton = (Button)dialog.findViewById(R.id.cancelButton);

        dialogHeader.setText(getString(R.string.addWord));

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Pair p = new Pair();
                p.setLeftValue(word.getText().toString());
                p.setRightValue(trans.getText().toString());

                mWords.set(index, p);
                mAdapter.notifyDataSetChanged();
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

    /**
     * {@inheritDoc}
     * @param result
     */
    @Override
    public void processFinish(HashMap<String, String> result) {

        switch(result.get(Constants.MODE)){
            case Constants.GET_FLASHCARD_CONTENT:
                this.getFlashcardContent_callback(result.get(Constants.RESULT));
                break;
            case Constants.EDIT_LIST:
                this.editList_callback(result.get(Constants.RESULT));
                break;
            case Constants.ADD_LIST:
                this.addList_callback(result.get(Constants.RESULT));
                break;
        }

        this.mAdapter.notifyDataSetChanged();
        this.progressBar.setVisibility(View.GONE);
    }
}

