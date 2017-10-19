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
import fiszki.xyz.fiszkiapp.source.Functions;
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

    private ListView mListView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private Flashcard flashcard;
    private FloatingActionButton fab;

    private WordsAdapter mAdapter;

    private ArrayList<Pair> mWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_flashcard);

        setTitle(getString(R.string.flashcardPreview));

        this.mListView = (ListView)findViewById(R.id.listView);
        this.progressBar = (ProgressBar)findViewById(R.id.progressBar);
        this.fab = (FloatingActionButton)findViewById(R.id.fab);

        this.mWords = new ArrayList<>();
        this.mAdapter = new WordsAdapter(this, this.mWords);
        this.mListView.setAdapter(this.mAdapter);

        if(getIntent().getIntExtra(Constants.MODE_KEY, Constants.NULL_MODE) == Constants.LOCAL_MODE){
            getFlashcardContent_callback(getIntent().getStringExtra(Constants.CONTENT));
            mAdapter.notifyDataSetChanged();
        }
        else if(getIntent().getIntExtra(Constants.MODE_KEY, Constants.NULL_MODE) == Constants.GLOBAL_MODE) {

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

            flashcard = getIntent().getParcelableExtra(Constants.LIST);

            if(flashcard.getHash() != null)
                getFlashcardContent(flashcard.getHash());
            setTitle(flashcard.getName());
        }
    }

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

        manageList(strArr_words, strArr_trans);
    }

    private void manageList(String words, String translations){
        if (!Functions.isOnline(getApplicationContext()))
            Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();

        String name = this.flashcard.getName();
        String hash = this.flashcard.getHash();
        String token = User.getInstance(this).getUserToken();
        try {
            words = URLEncoder.encode(words, "UTF-8");
            translations = URLEncoder.encode(translations, "UTF-8");
            name = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String mRequest = "token=" + token + "&words=" + words
                + "&translations=" + translations + "&name=" + name + "&action=add" + "&type=1"
                + "&lang=" + flashcard.getLangFrom() + "&lang2=" + flashcard.getLangTo();

        if(hash != null)
            mRequest += "&hash=" + hash;

        final ConnectionTask mConn = new ConnectionTask(this, Constants.MANAGE_LIST);
        mConn.execute(getString(R.string.manageListPhp), mRequest);

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

    private void manageListCallback(int responseCode){
        switch (responseCode){
            case 1:
                Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.flashcardAdded), Toast.LENGTH_LONG).show();
                break;
            case 2:
                Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.tokenIncorrect), Toast.LENGTH_LONG).show();
                break;
            case 3:
                // TODO: Fix message
                Toast.makeText(PreviewFlashcardActivity.this, "Cooldown", Toast.LENGTH_LONG).show();
                break;
            case 4:
                // TODO: Fix message
                Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.dataFormatIncorrect), Toast.LENGTH_LONG).show();
                break;
            case 6:
                Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.tokenIncorrect), Toast.LENGTH_LONG).show();
                break;
            case 7:
                // TODO: Fix message
                Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.incorrcectNameFormat), Toast.LENGTH_LONG).show();
                break;
            case 8:
                // TODO: Fix message
                Toast.makeText(PreviewFlashcardActivity.this, "Type incorrect", Toast.LENGTH_LONG).show();
                break;
            case 9:
                Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.incorrectFormat), Toast.LENGTH_LONG).show();
                break;
            case 10:
                Toast.makeText(PreviewFlashcardActivity.this, getString(R.string.languageIncorrect), Toast.LENGTH_LONG).show();
                break;
            case 11:
                // TODO: Fix message
                Toast.makeText(PreviewFlashcardActivity.this, "Action incorrect", Toast.LENGTH_LONG).show();
                break;
            case 12:
                // TODO: Fix message
                Toast.makeText(PreviewFlashcardActivity.this,  "Owner problem", Toast.LENGTH_LONG).show();
                break;
            case 13:
                Toast.makeText(PreviewFlashcardActivity.this,  getString(R.string.hashIncorrect), Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void getFlashcardContent(String hash){
        if(!Functions.isOnline(getApplicationContext()))
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

    @Override
    public void processFinish(HashMap<String, String> result) {

        switch(result.get(Constants.MODE)){
            case Constants.GET_FLASHCARD_CONTENT:
                this.getFlashcardContent_callback(result.get(Constants.RESULT));
                break;
            case Constants.MANAGE_LIST:
                try {
                    JSONObject jsonObject = new JSONObject(result.get(Constants.RESULT));
                    this.manageListCallback(jsonObject.getInt("status"));
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }

        this.mAdapter.notifyDataSetChanged();
        this.progressBar.setVisibility(View.GONE);
    }
}

