package com.nexgo.mylauncher;

import android.content.Context;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author 新国都技术股份有限公司 duxd
 * @since 1.0.0
 */
public class AppOpHideMgr {
    public static final int INSTALL = 0;
    public static final int UNINSTALL = 1;
    public static final int CLEAR = 2;

    public static boolean InstallHideApp(Context context, String file) {
        Boolean result = false;

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Boolean> future = executor.submit(new InstallThread(context, file, INSTALL));
        //TODO
        try {
            result = future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean UnInstallHideApp(Context context, String pkg) {
        Boolean result = false;

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Boolean> future = executor.submit(new InstallThread(context, pkg, UNINSTALL));
        try {
            result = future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean ClearHideApp(Context context, String pkg) {
        Boolean result = false;

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Boolean> future = executor.submit(new InstallThread(context, pkg, CLEAR));
        try {
            result = future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        return result;
    }
}
