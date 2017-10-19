package fiszki.xyz.fiszkiapp.source;

import android.content.Context;
import android.content.SharedPreferences;


public class AppPreferences {

    private static final String SETTINGS_NAME = "default_settings";
    private static AppPreferences appPreferences;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    public enum Key {
        USER_TOKEN,
        USER_NAME,
        USER_ID,
        USER_EMAIL,
        USER_FULLNAME,
        USER_PERMISSION,
        USER_TIME_CREATED,
        USER_LAST_ACTIVITY
    }

    private AppPreferences(Context context) {
        mPreferences = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }


    public static AppPreferences getInstance(Context context) {
        if (appPreferences == null) {
            appPreferences = new AppPreferences(context.getApplicationContext());
        }
        return appPreferences;
    }

    public void put(Key key, String val) {
        doEdit();
        mEditor.putString(key.name(), val);
        doCommit();
    }

    public void put(Key key, int val) {
        doEdit();
        mEditor.putInt(key.name(), val);
        doCommit();
    }

    public void put(Key key, boolean val) {
        doEdit();
        mEditor.putBoolean(key.name(), val);
        doCommit();
    }

    public void put(Key key, float val) {
        doEdit();
        mEditor.putFloat(key.name(), val);
        doCommit();
    }

    public void put(Key key, double val) {
        doEdit();
        mEditor.putString(key.name(), String.valueOf(val));
        doCommit();
    }

    public void put(Key key, long val) {
        doEdit();
        mEditor.putLong(key.name(), val);
        doCommit();
    }

    public String getString(Key key, String defaultValue) {
        return mPreferences.getString(key.name(), defaultValue);
    }

    public String getString(Key key) {
        return mPreferences.getString(key.name(), null);
    }

    public int getInt(Key key) {
        return mPreferences.getInt(key.name(), 0);
    }

    public int getInt(Key key, int defaultValue) {
        return mPreferences.getInt(key.name(), defaultValue);
    }

    public long getLong(Key key) {
        return mPreferences.getLong(key.name(), 0);
    }

    public long getLong(Key key, long defaultValue) {
        return mPreferences.getLong(key.name(), defaultValue);
    }

    public float getFloat(Key key) {
        return mPreferences.getFloat(key.name(), 0);
    }

    public float getFloat(Key key, float defaultValue) {
        return mPreferences.getFloat(key.name(), defaultValue);
    }

    public double getDouble(Key key) {
        return getDouble(key, 0);
    }

    public double getDouble(Key key, double defaultValue) {
        try {
            return Double.valueOf(mPreferences.getString(key.name(), String.valueOf(defaultValue)));
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public boolean getBoolean(Key key, boolean defaultValue) {
        return mPreferences.getBoolean(key.name(), defaultValue);
    }

    public boolean getBoolean(Key key) {
        return mPreferences.getBoolean(key.name(), false);
    }

    public void remove(Key... keys) {
        doEdit();
        for (Key key : keys) {
            mEditor.remove(key.name());
        }
        doCommit();
    }

    public void clear() {
        doEdit();
        mEditor.clear();
        doCommit();
    }

    private void doEdit() {
        if (mEditor == null) {
            mEditor = mPreferences.edit();
        }
    }

    private void doCommit() {
        if (mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }
}