package com.koiti.caja;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigStorage {
    private String key;

    public ConfigStorage(String key) {
        this.key = key;
    }

    public ConfigStorage(){}

    public void save(int value, String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void save(String value, String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void save(Boolean value, String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public int getValueInt(String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        return settings.getInt(key, 0);
    }

    public String getValueString(String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        return settings.getString(key, "");
    }

    public Boolean getValueBoolean(String key, Context context) {
        SharedPreferences settings = context.getSharedPreferences("KEY_DATA", 0);
        return settings.getBoolean(key, false);
    }
}
