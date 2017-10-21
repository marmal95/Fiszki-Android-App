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

import fiszki.xyz.fiszkiapp.interfaces.AsyncResponse;

public class ConnectionTask extends AsyncTask<String, Void, String> {

    private AsyncResponse delegate;
    private ConnectionTask.Mode mode;

    public ConnectionTask(AsyncResponse delegate, ConnectionTask.Mode mode){
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
            serverResponse = receiveData(connection);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return serverResponse;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        HashMap<ConnectionTask.Key, String> temp = new HashMap<>();
        temp.put(Key.REQUEST_MODE, mode.name());
        temp.put(Key.REQUEST_RESPONSE, result);
        this.delegate.processRequestResponse(temp);
    }

    private URLConnection initConnection(String urlAddress) throws IOException {
        URLConnection connection;
        URL url;

        url = new URL(urlAddress);
        connection = url.openConnection();
        connection.setDoOutput(true);

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

    public enum Key {
        REQUEST_MODE,
        REQUEST_RESPONSE
    }

    public enum Mode {
        LOGIN,
        REGISTER_ACCOUNT,
        ACTIVATE_ACCOUNT,
        RESTORE_PASSWORD,
        GET_FAVOURITE_FLASHCARDS,
        GET_MY_FLASHCARDS,
        GET_RECOMMENDED_FLASHCARDS,
        LIKE_FLASHCARD,
        UNLIKE_FLASHCARD,
        GET_FLASHCARD_CONTENT,
        REMOVE_FLASHCARD,
        GET_USER_INFO,
        ADD_FLASHCARD,
        MANAGE_FLASHCARD,
        DOWNLOAD_FLASHCARD,
        SEARCH_BY_NAME,
        SEARCH_BY_HASH,
        DELETE_ACCOUNT,
        CHANGE_USER_NAME,
        CHANGE_USER_PASSWORD
    }
}
