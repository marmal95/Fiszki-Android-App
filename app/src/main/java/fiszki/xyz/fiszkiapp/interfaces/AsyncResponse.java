package fiszki.xyz.fiszkiapp.interfaces;

import java.util.HashMap;

import fiszki.xyz.fiszkiapp.async_tasks.ConnectionTask;

public interface AsyncResponse {
    void processRequestResponse(HashMap<ConnectionTask.Key, String> output);
}
