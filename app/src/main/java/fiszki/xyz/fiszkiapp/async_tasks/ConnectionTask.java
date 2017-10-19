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
        String urlAddress = params[0];
        String postRequest = params[1];
        String serverResponse = "";

        try{
            URLConnection connection = initConnection(urlAddress);
            sendData(connection, postRequest);
            receiveData(connection);
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

    private URLConnection initConnection(String urlAddress) {
        URLConnection connection = null;
        URL url;

        try {
            url = new URL(urlAddress);
            connection = url.openConnection();
            connection.setDoOutput(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return connection;
    }

    private void sendData(URLConnection connection, String postRequest) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        writer.write(postRequest);
        writer.close();
    }

    private String receiveData(URLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = "";
        String responseLine;

        while((responseLine = reader.readLine()) != null)
            response += responseLine;

        return response;
    }
}
