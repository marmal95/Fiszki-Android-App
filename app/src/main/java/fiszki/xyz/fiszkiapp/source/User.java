package fiszki.xyz.fiszkiapp.source;

import android.content.Context;

public class User {

    private static User mInstance = null;

    private String name = null;
    private String user_id = null;
    private String email = null;
    private String full_name = null;
    private String permission = null;
    private String time_created = null;
    private String last_activity = null;
    private String user_token = null;

    private User(Context context){
        get_data(context);
    }

    public static User getInstance(Context context){
        if(mInstance == null)
            mInstance = new User(context);
        return mInstance;
    }

    public String getName() {
        return name;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getEmail() {
        return email;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getPermission() {
        return permission;
    }

    public String getTime_created() {
        return time_created;
    }

    public String getLast_activity() {
        return last_activity;
    }

    public String getUser_token() {
        return user_token;
    }

    public void get_data(Context context) {
        AppPreferences appPreferences = AppPreferences.getInstance(context);
        name = appPreferences.getString(AppPreferences.Key.USER_NAME);
        user_id = appPreferences.getString(AppPreferences.Key.USER_ID);
        email = appPreferences.getString(AppPreferences.Key.USER_EMAIL);
        full_name = appPreferences.getString(AppPreferences.Key.USER_FULLNAME);
        permission = appPreferences.getString(AppPreferences.Key.USER_PERMISSION);
        time_created = appPreferences.getString(AppPreferences.Key.USER_TIME_CREATED);
        last_activity = appPreferences.getString(AppPreferences.Key.USER_LAST_ACTIVITY);
        user_token = appPreferences.getString(AppPreferences.Key.USER_TOKEN);
    }

    public void clear_data(Context context) {
        clear();

        AppPreferences appPreferences = AppPreferences.getInstance(context);
        appPreferences.remove(AppPreferences.Key.USER_NAME);
        appPreferences.remove(AppPreferences.Key.USER_ID);
        appPreferences.remove(AppPreferences.Key.USER_EMAIL);
        appPreferences.remove(AppPreferences.Key.USER_FULLNAME);
        appPreferences.remove(AppPreferences.Key.USER_PERMISSION);
        appPreferences.remove(AppPreferences.Key.USER_TIME_CREATED);
        appPreferences.remove(AppPreferences.Key.USER_LAST_ACTIVITY);
        appPreferences.remove(AppPreferences.Key.USER_TOKEN);
    }

    private void clear() {
        name = null;
        user_id = null;
        email = null;
        full_name = null;
        permission= null;
        time_created = null;
        last_activity = null;
        user_token = null;

        mInstance = null;
    }
}
