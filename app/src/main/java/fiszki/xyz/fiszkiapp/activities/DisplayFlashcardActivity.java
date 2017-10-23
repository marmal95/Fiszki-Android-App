package fiszki.xyz.fiszkiapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;
import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;
import fiszki.xyz.fiszkiapp.source.Flashcard;
import fiszki.xyz.fiszkiapp.source.Settings;
import fiszki.xyz.fiszkiapp.source.User;
import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.utils.Functions;
import fiszki.xyz.fiszkiapp.utils.IntentKey;
import fiszki.xyz.fiszkiapp.utils.Pair;

public class DisplayFlashcardActivity extends AppCompatActivity implements AsyncResponse {

    // GUI components
    private ProgressBar progressBar;
    private TextView progressLabel;
    private SeekBar flashcardProgress;
    private TextView errorsLabel;
    private TextView wordLabel;
    private TextView translationLabel;
    private EditText translationInput;
    private Button knowButton;
    private Button dontKnowButton;
    private Button showTranslation;

    // Flashcard
    private Flashcard flashcard;
    private ArrayList<Pair> wordsList;
    private ArrayList<Pair> errorsList;

    // TextToSpeech
    private TextToSpeech word_speak;
    private TextToSpeech trans_speak;
    boolean isTtsSupported;

    // Settings
    private Settings settings;

    // Indexes and Words count
    private int index;
    private int wordsLength;
    private int errorsLength;

    private InterstitialAd mInterstitialAd;

    private boolean goBack;
    private boolean learningNotKnown;

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_flashcard);

        // Loads user settings to know how to run learn algorithm
        loadSettings();

        // Initialize GUI components
        initializeGUI();

        // Initialize variables
        goBack = false;

        // If there is saved instance - restore it
        restoreActivityState(savedInstanceState);

        // Check Data of TTS, when it is ok - initialize it
        initializeTts();

        // Set Listeners for tts to speak words
        if(settings.isTtsEnabled() && isTtsSupported)
            setTtsListeners();

        if(savedInstanceState == null){
            // Download flashcard
            // Check if there already is content -> then no need to download it
            if(getIntent().getStringExtra(Constants.CONTENT) != null)
                getFlashcardContent_callback(getIntent().getStringExtra(Constants.CONTENT));
            else
                getFlashcardContent(flashcard.getHash());
        }

        // Initialize Ad
        initializeAd();
    }

    @SuppressWarnings("unchecked")
    private void restoreActivityState(Bundle savedInstanceState){
        // Initialize objects
        if(savedInstanceState == null)
        {
            flashcard = new Flashcard();
            flashcard.setHash(getIntent().getStringExtra(IntentKey.HASH.name()));
            wordsList = new ArrayList<>();
            errorsList = new ArrayList<>();
            index = 0;
            wordsLength = 0;
            errorsLength = 0;
            isTtsSupported = true;
            learningNotKnown = false;
        } else{
            wordsList = (ArrayList<Pair>) savedInstanceState.getSerializable(Constants.WORDS_LIST);
            errorsList = (ArrayList<Pair>) savedInstanceState.getSerializable(Constants.ERROR_LIST);
            index = savedInstanceState.getInt(Constants.INDEX);
            wordsLength = savedInstanceState.getInt(Constants.WORDS_LENGTH);
            errorsLength = savedInstanceState.getInt(Constants.ERRORS_LENGTH);
            flashcard = savedInstanceState.getParcelable(Constants.FLASHCARD);
            isTtsSupported = savedInstanceState.getBoolean(Constants.TTS_ENABLED);
            learningNotKnown = savedInstanceState.getBoolean(Constants.LEARNING_NOT_KNOWN);
        }
    }

    /**
     * {@inheritDoc}
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(Constants.WORDS_LIST, wordsList);
        outState.putSerializable(Constants.ERROR_LIST, errorsList);
        outState.putInt(Constants.INDEX, index);
        outState.putParcelable(Constants.FLASHCARD, flashcard);
        outState.putBoolean(Constants.TTS_ENABLED, isTtsSupported);
        outState.putBoolean(Constants.LEARNING_NOT_KNOWN, learningNotKnown);
        outState.putInt(Constants.WORDS_LENGTH, wordsLength);
        outState.putInt(Constants.ERRORS_LENGTH, errorsLength);
    }

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(learningNotKnown)
            learnNotKnown();
        else
            learn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.cancelLearningQuestion));

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                goBack = true;
                if(mInterstitialAd.isLoaded())
                    mInterstitialAd.show();
                else
                    DisplayFlashcardActivity.super.onBackPressed();
            }
        });

        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (word_speak != null) {
            word_speak.stop();
            word_speak.shutdown();
        }

        if (trans_speak != null) {
            trans_speak.stop();
            trans_speak.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Displays Ad if is loaded.
     * Otherwise shows result
     */
    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }else
            showResult();
    }

    /**
     * Initializes all ui components
     */
    private void initializeGUI() {
        progressBar = findViewById(R.id.progressBar);
        progressLabel = findViewById(R.id.progressLabel);
        flashcardProgress = findViewById(R.id.seekBarProgress);
        errorsLabel = findViewById(R.id.errorLabel);
        wordLabel = findViewById(R.id.wordToTranslate);
        translationLabel = findViewById(R.id.wordTranslation);
        translationInput = findViewById(R.id.inputTranslation);

        knowButton = findViewById(R.id.knowButton);
        dontKnowButton = findViewById(R.id.dontKnowButton);
        showTranslation = findViewById(R.id.showTranslation);

        wordLabel.setVisibility(View.INVISIBLE);
        translationLabel.setVisibility(View.INVISIBLE);

        progressLabel.setText("0");
        flashcardProgress.setProgress(0);
        errorsLabel.setText("0");

        wordLabel.setVisibility(View.VISIBLE);
        translationLabel.setVisibility(View.VISIBLE);

        flashcardProgress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        if(settings.isWriteMode()){
            translationInput.setVisibility(View.VISIBLE);
            translationLabel.setVisibility(View.INVISIBLE);
        }
        if(settings.isDecisionMode()){
            translationInput.setVisibility(View.INVISIBLE);
            translationLabel.setVisibility(View.VISIBLE);
        }

        wordLabel.setMovementMethod(new ScrollingMovementMethod());
        translationLabel.setMovementMethod(new ScrollingMovementMethod());

        // Settings bar has range [0;20]. We allow user to set font in range [10; 30] sp
        // Default font size is 14 sp, so we need to subtract 4
        // final size = standard_size + user_preference_size - 4
        float size_sp = wordLabel.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
        wordLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, size_sp + settings.getFontSize()-4);
        translationLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, size_sp + settings.getFontSize()-4);
    }

    /**
     * Loads user settings and sets needed data
     */
    private void loadSettings() {
        settings = new Settings();

        SharedPreferences sharedPreferences = getSharedPreferences("settings", 0);
        settings.setDisplayInSequence(sharedPreferences.getBoolean(Constants.DISP_SEQUENCE, true));
        settings.setDisplayRevSequence(sharedPreferences.getBoolean(Constants.DISP_REV_SEQUENCE, false));
        settings.setDisplayRandomly(sharedPreferences.getBoolean(Constants.DISP_RANDOM, false));

        settings.setRepeatNotKnown(sharedPreferences.getBoolean(Constants.REP_NOT_KNOWN, false));
        settings.setReverseLanguages(sharedPreferences.getBoolean(Constants.REV_LANGUAGES, false));
        settings.setCreateRevisionList(sharedPreferences.getBoolean(Constants.CREATE_REV_LIST, false));

        settings.setDecisionMode(sharedPreferences.getBoolean(Constants.DECISION_MODE, true));
        settings.setWriteMode(sharedPreferences.getBoolean(Constants.WRITE_MODE, false));

        settings.setTtsEnabled(sharedPreferences.getBoolean(Constants.TTS_ENABLED, false));
        settings.setTtsSpeed(sharedPreferences.getInt(Constants.TTS_SPEED, 10));
        settings.setSkipBrackets(sharedPreferences.getBoolean(Constants.SKIP_BRACKETS, false));
        settings.setAutoReadWords(sharedPreferences.getBoolean(Constants.AUTO_READ_WORDS, false));
        settings.setAutoReadTrans(sharedPreferences.getBoolean(Constants.AUTO_READ_TRANS, false));

        settings.setFontSize(sharedPreferences.getInt(Constants.FONT_SIZE, 10));
    }

    /**
     * Initializes TTS engine, and set all TTS data.
     */
    private void initializeTts(){
        // Initialize TextToSpeech
        if (settings.isTtsEnabled()) {
            word_speak = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int res1 = word_speak.setLanguage(getLocale(flashcard.getLangFrom()));
                        word_speak.setSpeechRate((float) (0.1 * settings.getTtsSpeed()));
                        if (res1 == TextToSpeech.LANG_MISSING_DATA || res1 == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Toast.makeText(DisplayFlashcardActivity.this,
                                    getString(R.string.languageNotSupported, flashcard.getLangFrom()), Toast.LENGTH_SHORT).show();
                            isTtsSupported = false;
                        }

                        // If user wants to speak words automatically
                        // Read first word after tts initialized
                        if(settings.isTtsEnabled() && isTtsSupported && settings.isAutoReadWords())
                            speak(word_speak, wordLabel.getText().toString());

                    } else {
                        isTtsSupported = false;
                        Toast.makeText(DisplayFlashcardActivity.this, getString(R.string.ttsNotSupported), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Initialize TextToSpeech
        if (settings.isTtsEnabled() && isTtsSupported) {
            trans_speak = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int res1 = trans_speak.setLanguage(getLocale(flashcard.getLangTo()));
                        trans_speak.setSpeechRate((float) (0.1 * settings.getTtsSpeed()));
                        if (res1 == TextToSpeech.LANG_MISSING_DATA || res1 == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Toast.makeText(DisplayFlashcardActivity.this,
                                    getString(R.string.languageNotSupported, flashcard.getLangTo()), Toast.LENGTH_SHORT).show();
                            isTtsSupported = false;
                        }
                    } else {
                        isTtsSupported = false;
                        Toast.makeText(DisplayFlashcardActivity.this, getString(R.string.ttsNotSupported), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * Sets TTS listeners to display appropriate word
     */
    private void setTtsListeners(){
        // Set speak listeners
        if (settings.isTtsEnabled() && isTtsSupported) {
            wordLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String word_to_speech = wordLabel.getText().toString();
                    if (settings.isSkipBrackets())
                        word_to_speech = word_to_speech.replaceAll("\\(.*?\\) ?", " ");
                    speak(word_speak, word_to_speech);
                }
            });

            translationLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String word_to_speech = translationLabel.getText().toString();
                    if (settings.isSkipBrackets())
                        word_to_speech = word_to_speech.replaceAll("\\(.*?\\) ?", " ");
                    speak(trans_speak, word_to_speech);
                }
            });
        }
    }

    /**
     * Speaks current word with passed tts object
     * @param tts TTS object used to speak
     * @param phrToSpeak phrase to speak
     */
    @SuppressWarnings("deprecation")
    private void speak(TextToSpeech tts, String phrToSpeak){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(phrToSpeak,TextToSpeech.QUEUE_FLUSH,null, null);
        } else {
            tts.speak(phrToSpeak, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    /**
     * Initializes Ad
     */
    private void initializeAd(){
        // Ads
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                if(!goBack)
                    showResult();
                else{
                    Intent intent = new Intent(DisplayFlashcardActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    /**
     * Runs learning algorithm
     */
    private void learn() {
        // Check display settings and reverse or shuffle array if necessary
        if (settings.isDisplayRevSequence())
            Collections.reverse(wordsList);
        else if (settings.isDisplayRandomly())
            Collections.shuffle(wordsList);

        if (settings.isReverseLanguages()) {
            String t = flashcard.getLangFrom();
            flashcard.setLangFrom(flashcard.getLangTo());
            flashcard.setLangTo(t);

            for (Pair p : wordsList)
                p.swapValues();
        }

        flashcardProgress.setMax(wordsList.size() - 1);
        wordsLength = wordsList.size();
        // Update ui
        updateUi();

        knowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index++;
                if (index < wordsList.size())
                    updateUi();
                else {
                    if (settings.isCreateRevisionList() && errorsList.size() > 0)
                        addList();
                    if (settings.isRepeatNotKnown() && errorsList.size() > 0)
                        learnNotKnown();
                    else {
                        showInterstitial();
                    }
                }
            }
        });

        dontKnowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(index < wordsList.size()) {
                    errorsList.add(wordsList.get(index));
                    errorsLength++;
                }
                index++;
                if (index < wordsList.size())
                    updateUi();
                else {
                    if (settings.isCreateRevisionList() && errorsList.size() > 0)
                        addList();
                    else if (settings.isRepeatNotKnown() && errorsList.size() > 0)
                        learnNotKnown();
                    else
                        showInterstitial();
                }
            }
        });

        showTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Decision Mode
                if(settings.isDecisionMode())
                    translationLabel.setVisibility(View.VISIBLE);

                // Write Mode
                if(settings.isWriteMode()){
                    String userTrans = translationInput.getText().toString().trim();
                    if(userTrans.equalsIgnoreCase(wordsList.get(index).getRightValue()))
                        translationInput.setTextColor(ContextCompat.getColor(DisplayFlashcardActivity.this, R.color.veryGoodScore));
                    else {
                        translationInput.setTextColor(ContextCompat.getColor(DisplayFlashcardActivity.this, R.color.veryWeakScore));
                        translationLabel.setVisibility(View.VISIBLE);
                    }
                }

                // Read Automatically
                if (settings.isTtsEnabled() && isTtsSupported && settings.isAutoReadTrans()) {
                    String word_to_speech = translationLabel.getText().toString();
                    if (settings.isSkipBrackets())
                        word_to_speech = word_to_speech.replaceAll("\\(.*?\\) ?", " ");
                    speak(trans_speak, word_to_speech);
                }
            }
        });
    }

    /**
     * Runs algorithm for learning not-known words
     */
    private void learnNotKnown() {
        learningNotKnown = true;
        // Set new progress max
        flashcardProgress.setMax(errorsLength);

        // Check display settings and reverse or shuffle array if necessary
        if (settings.isDisplayRevSequence())
            Collections.reverse(errorsList);
        else if (settings.isDisplayRandomly())
            Collections.shuffle(errorsList);

        final Random rand = new Random();
        index = rand.nextInt(errorsList.size());
        updateRepeatUi();

        flashcardProgress.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);

        knowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorsList.remove(index);
                if (errorsList.size() > 0) {
                    index = rand.nextInt(errorsList.size());
                    updateRepeatUi();
                } else {
                    showInterstitial();
                }
            }
        });

        dontKnowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (errorsList.size() > 0) {
                    index = rand.nextInt(errorsList.size());
                    updateRepeatUi();
                } else
                    showInterstitial();
            }
        });

        showTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Decision Mode
                if(settings.isDecisionMode())
                    translationLabel.setVisibility(View.VISIBLE);

                // Write Mode
                if(settings.isWriteMode()){
                    String userTrans = translationInput.getText().toString().trim();
                    if(userTrans.equalsIgnoreCase(errorsList.get(index).getRightValue()))
                        translationInput.setTextColor(ContextCompat.getColor(DisplayFlashcardActivity.this, R.color.veryGoodScore));
                    else {
                        translationInput.setTextColor(ContextCompat.getColor(DisplayFlashcardActivity.this, R.color.veryWeakScore));
                        translationLabel.setVisibility(View.VISIBLE);
                    }
                }

                // Read automatically
                if (settings.isTtsEnabled() && isTtsSupported && settings.isAutoReadTrans()) {
                    String word_to_speech = translationLabel.getText().toString();
                    if (settings.isSkipBrackets())
                        word_to_speech = word_to_speech.replaceAll("\\(.*?\\) ?", " ");
                    speak(trans_speak, word_to_speech);
                }

            }
        });
    }

    /**
     * Download flashcard content from the server.
     * Builds POST request and runs ConnectionTask.
     *
     * @param hash flashcard hash
     */
    private void getFlashcardContent(String hash) {
        if (!Functions.isOnline(getApplicationContext()))
            Toast.makeText(DisplayFlashcardActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else {
            // Encode POST arguments with UTF-8 Encoder
            try {
                hash = URLEncoder.encode(hash, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "hash=" + hash;

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, ConnectionTask.Mode.GET_FLASHCARD_CONTENT);
            mConn.execute(getString(R.string.getListContentByHashPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(DisplayFlashcardActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, ConnectionTask.TIME_LIMIT_MS);
        }
    }

    /**
     * Verifies server response to the getFlashcardContent request
     *
     * @param output server response
     */
    private void getFlashcardContent_callback(String output) {
        JSONObject c;
        JSONArray wordsToTranslate;
        JSONArray wordsTranslated;

        try {
            c = new JSONObject(output);
            flashcard.setName(c.getString("name"));
            flashcard.setLangFrom(c.getString("lang"));
            flashcard.setLangTo(c.getString("lang2"));
            wordsToTranslate = c.getJSONArray("words");
            wordsTranslated = c.getJSONArray("translations");

            for (int i = 0; i < wordsToTranslate.length(); i++) {
                String word = wordsToTranslate.getString(i);
                String trans = wordsTranslated.getString(i);

                try {
                    word = URLDecoder.decode(word, "UTF-8");
                    trans = URLDecoder.decode(trans, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                Pair pair = new Pair(word, trans);
                this.wordsList.add(pair);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setTitle(flashcard.getName());

        if(wordsList.isEmpty()){
            Toast.makeText(this, getString(R.string.flashcardEmpty), Toast.LENGTH_LONG).show();
            showInterstitial();
        } else
        learn();
    }

    /**
     * Updates ui according to the progress of flashcard
     */
    private void updateUi() {
        flashcardProgress.setProgress(index);

        progressLabel.setText(getString(R.string.flashcardProgress, String.valueOf(index + 1), String.valueOf(wordsLength)));
        errorsLabel.setText(String.valueOf(errorsLength));

        wordLabel.setText(wordsList.get(index).getLeftValue());
        wordLabel.scrollTo(0, 0);

        translationLabel.setText(wordsList.get(index).getRightValue());
        translationLabel.scrollTo(0, 0);
        translationLabel.setVisibility(View.INVISIBLE);

        translationInput.setText("");
        translationInput.setTextColor(ContextCompat.getColor(DisplayFlashcardActivity.this, R.color.Th1_darkColor_text));

        // Read Automatically
        if (settings.isTtsEnabled() && isTtsSupported && settings.isAutoReadWords()) {
            String word_to_speech = wordLabel.getText().toString();
            if (settings.isSkipBrackets())
                word_to_speech = word_to_speech.replaceAll("\\(.*?\\) ?", " ");
            speak(word_speak, word_to_speech);
        }
    }

    /**
     * Updates ui according to the flashcard progress during learning not-known words
     */
    private void updateRepeatUi() {
        flashcardProgress.setProgress(errorsList.size());

        progressLabel.setText(getString(R.string.flashcardProgress, String.valueOf(wordsLength), String.valueOf(wordsLength)));
        errorsLabel.setText(String.valueOf(errorsList.size()));

        wordLabel.setText(errorsList.get(index).getLeftValue());
        translationLabel.setText(errorsList.get(index).getRightValue());
        translationLabel.setVisibility(View.INVISIBLE);
        translationInput.setText("");

        // Read Automatically
        if (settings.isTtsEnabled() && isTtsSupported && settings.isAutoReadWords()) {
            String word_to_speech = wordLabel.getText().toString();
            if (settings.isSkipBrackets())
                word_to_speech = word_to_speech.replaceAll("\\(.*?\\) ?", " ");
            speak(word_speak, word_to_speech);
        }
    }

    /**
     * Sends new flashcard to the server.
     */
    private void addList() {
        if (!Functions.isOnline(getApplicationContext()))
            Toast.makeText(DisplayFlashcardActivity.this, getString(R.string.noConnectionWarning), Toast.LENGTH_LONG).show();
        else {
            String[] words = new String[errorsList.size()];
            String[] trans = new String[errorsList.size()];

            for (int i = 0; i < errorsList.size(); i++) {
                Pair hm = errorsList.get(i);
                words[i] = hm.getLeftValue();
                trans[i] = hm.getRightValue();
            }

            JSONArray json_words = new JSONArray(Arrays.asList(words));
            JSONArray json_trans = new JSONArray(Arrays.asList(trans));

            String strArr_words = json_words.toString();
            String strArr_trans = json_trans.toString();

            // Encode POST arguments with UTF-8 Encoder
            String name = getString(R.string.revList, flashcard.getName());
            String token = User.getInstance(this).getUserToken();
            try {
                strArr_words = URLEncoder.encode(strArr_words, "UTF-8");
                strArr_trans = URLEncoder.encode(strArr_trans, "UTF-8");
                token = URLEncoder.encode(token, "UTF-8");
                name = URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // Build POST Request
            String mRequest = "token=" + token + "&words=" + strArr_words
                    + "&translations=" + strArr_trans + "&name=" + name
                    + "&lang=" + flashcard.getLangFrom() + "&lang2=" + flashcard.getLangTo();

            // Create and run ConnectionTask
            final ConnectionTask mConn = new ConnectionTask(this, ConnectionTask.Mode.ADD_FLASHCARD);
            mConn.execute(getString(R.string.addListPhp), mRequest);

            progressBar.setVisibility(View.VISIBLE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mConn.getStatus() == AsyncTask.Status.RUNNING) {
                        mConn.cancel(true);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(DisplayFlashcardActivity.this, getString(R.string.connectionProblem), Toast.LENGTH_LONG).show();
                    }
                }
            }, ConnectionTask.TIME_LIMIT_MS);
        }
    }

    /**
     * Verifies server response to the addList request
     *
     * @param output server response
     */
    private void addList_callback(String output) {
        int listStatus = -1;
        try {
            JSONObject c = new JSONObject(output);
            listStatus = c.getInt("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        switch (listStatus) {
            case 1:
                Toast.makeText(this, getResources().getString(R.string.flashcardAdded), Toast.LENGTH_LONG).show();
                break;
            case 2:
                Toast.makeText(this, getResources().getString(R.string.cantAddFlashcard), Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(this, getResources().getString(R.string.sthHasGoneWrong), Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Gets locale basing on language abbreviation
     * @param abbr abbreviation of language
     * @return locale equivalent passed abbr. by argument
     */
    private Locale getLocale(String abbr) {
        switch (abbr) {
            case "pl":
                return new Locale("pl");
            case "de":
                return Locale.GERMAN;
            case "en":
                return Locale.UK;
            case "es":
                return new Locale("es");
            case "fr":
                return Locale.FRENCH;
            case "it":
                return Locale.ITALIAN;
            case "lt":
                return new Locale("lt");
            default:
                return null;
        }
    }

    /**
     * Displays user's result of current flashcard
     */
    private void showResult() {
        // Show result
        Intent intent = new Intent(DisplayFlashcardActivity.this, ResultActivity.class);

        // Not divide by zero
        int score;
        if(wordsList.size() == 0)
            score = 0;
        else
            score = ((wordsList.size() - errorsList.size()) * 100) / wordsList.size();

        intent.putExtra(Constants.USER_RESULT, score);
        intent.putExtra(Constants.CONTENT, getIntent().getStringExtra(Constants.CONTENT));
        startActivity(intent);
        finish();
    }

    /**
     * {@inheritDoc}
     * @param output <MODE/RESULT> response from the server
     */
    @Override
    public void processRequestResponse(HashMap<ConnectionTask.Key, String> output) {
        ConnectionTask.Mode requestMode = ConnectionTask.Mode.valueOf(output.get(ConnectionTask.Key.REQUEST_MODE));
        String requestResponse = output.get(ConnectionTask.Key.REQUEST_RESPONSE);

        if(requestMode == ConnectionTask.Mode.GET_FLASHCARD_CONTENT)
            getFlashcardContent_callback(requestResponse);
        else if (requestMode == ConnectionTask.Mode.ADD_FLASHCARD)
            addList_callback(requestResponse);

        progressBar.setVisibility(View.GONE);
    }
}
