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

public class ConnectionTask extends AsyncTask<String, Void, String> {

    private AsyncResponse delegate;
    private String mode;

    public ConnectionTask(AsyncResponse delegate, String mode){
        this.delegate = delegate;
        this.mode = mode;
    }

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

        try{
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

        return serverResponse;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        HashMap<String, String> temp = new HashMap<>();
        temp.put(Constants.MODE, this.mode);
        temp.put(Constants.RESULT, result);
        this.delegate.processFinish(temp);
    }
}
