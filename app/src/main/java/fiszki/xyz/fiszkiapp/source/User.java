package fiszki.xyz.fiszkiapp.source;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Keeps all necessary information about logged user.
 */
public class User {

    private static User mInstance = null;

    private String name = null;
    private String usr_id = null;
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

    public String getUsr_id() {
        return usr_id;
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

    public void get_data(Context context){
        SharedPreferences sharedPreferences = context.getApplicationContext().
                getSharedPreferences("user_data", 0);

        name = sharedPreferences.getString("user_name", "0");
        usr_id = sharedPreferences.getString("user_id", "0");
        email = sharedPreferences.getString("user_email", "0");
        full_name = sharedPreferences.getString("user_full_name", "0");
        permission = sharedPreferences.getString("user_permission", "0");
        time_created = sharedPreferences.getString("user_time_created", "0");
        last_activity = sharedPreferences.getString("user_last_activity", "0");
        user_token = sharedPreferences.getString("user_token", "0");
    }

    public void clear_data(Context context){
        name = null;
        usr_id = null;
        email = null;
        full_name = null;
        permission= null;
        time_created = null;
        last_activity = null;
        user_token = null;

        mInstance = null;

        SharedPreferences sharedPreferences = context.getApplicationContext().
                getSharedPreferences("user_data", 0);

        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove("user_name");
        edit.remove("user_id");
        edit.remove("user_email");
        edit.remove("user_full_name");
        edit.remove("user_permission");
        edit.remove("user_time_created");
        edit.remove("user_last_activity");
        edit.remove("user_token");
        edit.apply();
    }
}
