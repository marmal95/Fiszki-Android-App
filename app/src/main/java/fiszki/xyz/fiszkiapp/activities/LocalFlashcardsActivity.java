package fiszki.xyz.fiszkiapp.activities;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.R;
import fiszki.xyz.fiszkiapp.utils.IntentKey;

public class LocalFlashcardsActivity extends AppCompatActivity {

    // GUI Components
    private ListView mListView;
    private ProgressBar progressBar;

    // Data list and adapter
    private ArrayList<String> flashcards;
    private ArrayAdapter<String> mAdapter;

    // Fiszki folder
    private File dir;

    // SwipeRefresher
    private SwipeRefreshLayout swiperefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_flashcards);

        // Initialize GUI Components
        this.mListView = findViewById(R.id.listView);
        this.progressBar = findViewById(R.id.progressBar);
        this.swiperefresh = findViewById(R.id.swiperefresh);

        // Initialize Objects
        this.flashcards = new ArrayList<>();
        this.mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, flashcards);

        // Set data
        this.mListView.setAdapter(mAdapter);
        registerForContextMenu(this.mListView);


        // Get Fiszki folder
        dir = new File(android.os.Environment.getExternalStorageDirectory(), "Fiszki");

        // Create Fiszki folder if does not exist
        if(!dir.exists()){
            if(!dir.mkdir())
                Toast.makeText(LocalFlashcardsActivity.this, getString(R.string.couldNotCreateFolder),
                        Toast.LENGTH_LONG).show();
        } else
            getLocalFlashcards();

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
                getLocalFlashcards();
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
    @SuppressWarnings("unchecked")
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if(v.getId() == R.id.listView){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

            menu.setHeaderTitle(flashcards.get(info.position));
            String[] menuOptions = getResources().getStringArray(R.array.localFlashcardsMenuOptions);

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
                runFlashcard(info.position);
                break;
            case 1:
                Intent intent = new Intent(LocalFlashcardsActivity.this, PreviewFlashcardActivity.class);
                intent.putExtra(IntentKey.ACTIVITY_MODE.name(), Constants.LOCAL_MODE);
                intent.putExtra(IntentKey.PARENT_ACTIVITY.name(), Constants.LOCAL_FLASHCARD_ACT);
                intent.putExtra(Constants.CONTENT, this.readFlashcardFromFile(flashcards.get(info.position)));
                startActivity(intent);
                break;
            case 2:
                File[] files = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.toString().toLowerCase().endsWith(".xyz");
                    }
                });

                if(!files[info.position].delete())
                    Toast.makeText(this, "deleteError", Toast.LENGTH_LONG).show();
                else
                    flashcards.remove(info.position);
                mAdapter.notifyDataSetChanged();
                break;
        }
        return true;
    }

    private void getLocalFlashcards(){
        // Read local flashcards
        // Flashcard have name: *.xyz so filter it
        File[] flashcardFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.toString().toLowerCase().endsWith(".xyz");
            }
        });

        // Add flashcards name to ui
        if(flashcardFiles != null)
            for(File f : flashcardFiles)
                flashcards.add(f.getName().replace(".xyz", ""));
        else
            Toast.makeText(this, getString(R.string.noLocalFlashcards), Toast.LENGTH_LONG).show();
        mAdapter.notifyDataSetChanged();
        swiperefresh.setRefreshing(false);
    }

    private String readFlashcardFromFile(String fileName){

        String fileContent = "";
        try {
            File file = new File(dir.getAbsolutePath() + File.separator + fileName + ".xyz");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder builder = new StringBuilder();
            String line = "";

            while((line = reader.readLine()) != null)
                builder.append(line);

            reader.close();
            fileContent = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent;
    }

    private void runFlashcard(int position){
        Intent intent = new Intent(LocalFlashcardsActivity.this, DisplayFlashcardActivity.class);
        intent.putExtra(Constants.CONTENT, readFlashcardFromFile(flashcards.get(position)));
        startActivity(intent);
    }
}
