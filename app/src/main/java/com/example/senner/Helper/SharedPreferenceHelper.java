package com.example.senner.Helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Response;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;


public class SharedPreferenceHelper {
    private static final String spFileName = "app";

    @Nullable
        public final String getString(@NotNull Context context, @Nullable String strKey, @Nullable String strDefault) {
            
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            
            return sp.getString(strKey, strDefault);
        }
        

        public final void putString(@NotNull Context context, @Nullable String strKey, @Nullable String strData) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(strKey, strData);
            editor.apply();
        }

        public final boolean getBoolean(@NotNull Context context, @Nullable String strKey, @Nullable Boolean strDefault) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            return sp.getBoolean(strKey, strDefault);
        }


        public final void putBoolean(@NotNull Context context, @Nullable String strKey, @Nullable Boolean strData) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(strKey, strData);
            editor.apply();
        }

        public final int getInt(@NotNull Context context, @Nullable String strKey, int strDefault) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            return sp.getInt(strKey, strDefault);
        }
        

        public final void putInt(@NotNull Context context, @Nullable String strKey, int strData) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(strKey, strData);
            editor.apply();
        }

        public final long getLong(@NotNull Context context, @Nullable String strKey, long strDefault) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            SharedPreferences setPreferences = sp;
            return setPreferences.getLong(strKey, strDefault);
        }
        

        public final void putLong(@NotNull Context context, @Nullable String strKey, long strData) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(strKey, strData);
            editor.apply();
        }

        public final float getFloat(@NotNull Context context, @Nullable String strKey, float strDefault) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            return sp.getFloat(strKey, strDefault);
        }
        

        public final void putFloat(@NotNull Context context, @Nullable String strKey, float strData) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat(strKey, strData);
            editor.apply();
        }

        @Nullable
        public final Set<String> getStringSet(@NotNull Context context, @Nullable String strKey) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            return sp.getStringSet(strKey, (new LinkedHashSet<String>()));
        }

        public final void putStringSet(@NotNull Context context, @Nullable String strKey, @Nullable LinkedHashSet<String> strData) {
            SharedPreferences sp = context.getSharedPreferences(SharedPreferenceHelper.spFileName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putStringSet(strKey, strData);
            editor.apply();
        }
        
}
