package fiszki.xyz.fiszkiapp.source;

import android.content.Context;

// TODO: Test User
public class User {

    private static User mInstance = null;

    private String name = null;
    private String userId = null;
    private String email = null;
    private String fullName = null;
    private String permission = null;
    private String timeCreated = null;
    private String lastActivity = null;
    private String userToken = null;

    private User(Context context){
        initUserData(context);
    }

    public static User getInstance(Context context){
        if(mInstance == null)
            mInstance = new User(context);
        return mInstance;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPermission() {
        return permission;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public String getLastActivity() {
        return lastActivity;
    }

    public String getUserToken() {
        return userToken;
    }

    public void initUserData(Context context) {
        AppPreferences appPreferences = AppPreferences.getInstance(context);
        name = appPreferences.getString(AppPreferences.Key.USER_NAME);
        userId = appPreferences.getString(AppPreferences.Key.USER_ID);
        email = appPreferences.getString(AppPreferences.Key.USER_EMAIL);
        fullName = appPreferences.getString(AppPreferences.Key.USER_FULLNAME);
        permission = appPreferences.getString(AppPreferences.Key.USER_PERMISSION);
        timeCreated = appPreferences.getString(AppPreferences.Key.USER_TIME_CREATED);
        lastActivity = appPreferences.getString(AppPreferences.Key.USER_LAST_ACTIVITY);
        userToken = appPreferences.getString(AppPreferences.Key.USER_TOKEN);
    }

    public void clearUserData(Context context) {
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
        userId = null;
        email = null;
        fullName = null;
        permission= null;
        timeCreated = null;
        lastActivity = null;
        userToken = null;

        mInstance = null;
    }
}
