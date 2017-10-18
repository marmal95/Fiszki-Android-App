package fiszki.xyz.fiszkiapp.interfaces;

import java.util.HashMap;

/**
 * Interface AsyncResponse allows class that implement it
 * to get callback from ConnectionTask and run
 * implemented on their own function "processFinish"
 */
public interface AsyncResponse {
    /**
     * ConnectionTask callback to update data and ui
     * Called at the end of running AsyncTask
     * @param output <MODE/RESULT> response from the server
     */
    void processFinish(HashMap<String, String> output);
}
