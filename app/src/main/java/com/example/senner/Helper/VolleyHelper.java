package com.example.senner.Helper;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class VolleyHelper extends Application {

    public static final String TAG = VolleyHelper.class.getSimpleName();
    private RequestQueue mRequestQueue;

    private static VolleyHelper mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized VolleyHelper getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
                getRequestQueue().add(req);
            }
        });
        thread.start();

    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}