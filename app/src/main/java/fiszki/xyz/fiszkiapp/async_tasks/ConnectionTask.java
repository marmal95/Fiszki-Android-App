package fiszki.xyz.fiszkiapp.async_tasks;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;

/**
 * Extends AsyncTask, makes connection to the server,
 * sends POST request to the give address.
 */
public class ConnectionTask extends AsyncTask<String, Void, String> {

    // Calls function of proper activity at the end.
    private AsyncResponse delegate;

    // Mode(task) that is run by ConnectionTask
    private String mode;

    /**
     * Initialize AsyncResponse to call the proper function at the end.
     * Initialize String mode to detect what action is being run
     * @param delegate activity that function will be called
     * @param mode mode of running task
     */
    public ConnectionTask(AsyncResponse delegate, String mode){
        this.delegate = delegate;
        this.mode = mode;
    }

    /**
     * {@inheritDoc}
     * Send a request to the server
     * @param params [0] - address of *.php file, [1] - POST request
     * @return server response on sent request
     */
    @Override
    protected String doInBackground(String... params) {

        // params[0] - address
        // params[1] - request

        URL mUrl;
        URLConnection mConnect = null;

        String serverResponse = "";
        String mUrlAddress = params[0];
        String postRequest = params[1];

        try {
            mUrl = new URL(mUrlAddress);
            mConnect = mUrl.openConnection();
            mConnect.setDoOutput(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Send a request to server
        try{
            assert mConnect != null; // Should be ;o
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(mConnect.getOutputStream()));
            writer.write(postRequest);
            writer.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(mConnect.getInputStream()));
            String respLine;

            while((respLine = reader.readLine()) != null)
                serverResponse += respLine;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return serverResponse; // = Html.fromHtml(servAnswer).toString();
    }

    /**
     * {@inheritDoc}
     * Pass the server response to the proper activity that called task
     * @param result server response on POST request
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        HashMap<String, String> temp = new HashMap<>();
        temp.put(Constants.MODE, this.mode);
        temp.put(Constants.RESULT, result);
        this.delegate.processFinish(temp);
    }
}
