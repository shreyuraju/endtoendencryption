package com.end2endmessage.e2em;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManagement {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String SHARED_PREF_NAME = "session";
    String SESSIOM_KEY = "session_user";

    public SessionManagement(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSession(User user) {
        //save session
        int id = user.getUID();
        editor.putInt(SESSIOM_KEY, id).commit();
    }
    public int getSession() {
        //return user of saved session
        return  sharedPreferences.getInt(SESSIOM_KEY,-1);
    }

    public void removeSession() {
        editor.putInt(SESSIOM_KEY,-1).commit();
    }

}
