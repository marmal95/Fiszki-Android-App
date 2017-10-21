package fiszki.xyz.fiszkiapp.source;

import android.content.Context;

// TODO: Test User
public class User {
    private static User mInstance = null;
    private AppPreferences appPreferences;

    private String name = null;
    private String userId = null;
    private String email = null;
    private String fullName = null;
    private String permission = null;
    private String timeCreated = null;
    private String lastActivity = null;
    private String userToken = null;

    public static User getInstance(Context context){
        if(mInstance == null)
            mInstance = new User(context);
        return mInstance;
    }

    public void setName(String name) {
        this.name = name;
        appPreferences.put(AppPreferences.Key.USER_NAME, name);
    }

    public void setUserId(String userId) {
        this.userId = userId;
        appPreferences.put(AppPreferences.Key.USER_ID, userId);
    }

    public void setEmail(String email) {
        this.email = email;
        appPreferences.put(AppPreferences.Key.USER_EMAIL, email);
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        appPreferences.put(AppPreferences.Key.USER_FULLNAME, fullName);
    }

    public void setPermission(String permission) {
        this.permission = permission;
        appPreferences.put(AppPreferences.Key.USER_PERMISSION, permission);
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
        appPreferences.put(AppPreferences.Key.USER_TIME_CREATED, timeCreated);
    }

    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
        appPreferences.put(AppPreferences.Key.USER_LAST_ACTIVITY, lastActivity);
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
        appPreferences.put(AppPreferences.Key.USER_TOKEN, userToken);
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

    private User(Context context){
        appPreferences = AppPreferences.getInstance(context);
        initUserData();
    }

    private void initUserData() {
        name = appPreferences.getString(AppPreferences.Key.USER_NAME);
        userId = appPreferences.getString(AppPreferences.Key.USER_ID);
        email = appPreferences.getString(AppPreferences.Key.USER_EMAIL);
        fullName = appPreferences.getString(AppPreferences.Key.USER_FULLNAME);
        permission = appPreferences.getString(AppPreferences.Key.USER_PERMISSION);
        timeCreated = appPreferences.getString(AppPreferences.Key.USER_TIME_CREATED);
        lastActivity = appPreferences.getString(AppPreferences.Key.USER_LAST_ACTIVITY);
        userToken = appPreferences.getString(AppPreferences.Key.USER_TOKEN);
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
