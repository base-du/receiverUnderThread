package com.nexgo.mylauncher;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void OnInstall(View view) {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = AppOpHideMgr.InstallHideApp(context, "/sdcard/app.apk");
                Log.d(TAG, "InstallHideApp: " + result);
            }
        });
    }

    public void OnUnInstall(View view) {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = AppOpHideMgr.UnInstallHideApp(context, "com.qihoo.appstore");
                Log.d(TAG, "UnInstallHideApp: " + result);
            }
        });
    }

    public void OnClear(View view) {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = AppOpHideMgr.ClearHideApp(context, "com.qihoo.appstore");
                Log.d(TAG, "ClearHideApp: " + result);
            }
        });
    }

    public void OnForLoop(View view) {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        executor.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = AppOpHideMgr.InstallHideApp(context, "/sdcard/app.apk");
                Log.d(TAG, "InstallHideApp: " + result);
            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = AppOpHideMgr.ClearHideApp(context, "com.qihoo.appstore");
                Log.d(TAG, "ClearHideApp: " + result);
            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = AppOpHideMgr.UnInstallHideApp(context, "com.qihoo.appstore");
                Log.d(TAG, "UnInstallHideApp: " + result);
            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = AppOpHideMgr.InstallHideApp(context, "/sdcard/app.apk");
                Log.d(TAG, "InstallHideApp: " + result);
            }
        });
    }
}
