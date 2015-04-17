package com.nexgo.mylauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

/**
 * @author 新国都技术股份有限公司 duxd
 * @since 1.0.0
 */
public class InstallThread implements Callable<Boolean> {
    private static final String TAG = "InstallThread";
    private static final int PARTICIPANTS = 2;
    private Context context;
    private String file_pkg;
    private int type;
    private BroadcastReceiver receiver;
    private CyclicBarrier barrierInstall;
    private CyclicBarrier barrierUnInstall;
    private CyclicBarrier barrierClear;
    private Boolean result;

    public InstallThread(Context context, String file_pkg, int type) {
        this.context = context;
        this.file_pkg = file_pkg;
        this.type = type;

        IntentFilter filter = new IntentFilter();
        //TODO
//        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
//        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
//        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
//        filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        switch (type) {
            case AppOpHideMgr.INSTALL:
                filter.addAction(Intent.ACTION_PACKAGE_ADDED);
                filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
                barrierInstall = new CyclicBarrier(PARTICIPANTS);
                break;
            case AppOpHideMgr.UNINSTALL:
                filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                barrierUnInstall = new CyclicBarrier(PARTICIPANTS);
                break;
            case AppOpHideMgr.CLEAR:
                filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
                barrierClear = new CyclicBarrier(PARTICIPANTS);
                break;
        }

        filter.addDataScheme("package");
        receiver = new InstallPackageReceiver();
        context.registerReceiver(receiver, filter);
    }

    @Override
    public Boolean call() throws Exception {
        switch (type) {
            case AppOpHideMgr.INSTALL:
                Intent install_hide_intent = new Intent("android.intent.action.VIEW_HIDE");
                install_hide_intent.setDataAndType(Uri.parse("file://" + file_pkg), "application/vnd.android.package-archive");
                context.startActivity(install_hide_intent);
                Log.d(TAG, "start install_hide_intent");
                barrierInstall.await();
                break;
            case AppOpHideMgr.UNINSTALL:
                Intent uninstall_hide_intent = new Intent("android.intent.action.DELETE_HIDE");
                uninstall_hide_intent.setData(Uri.parse("package:" + file_pkg));
                context.startActivity(uninstall_hide_intent);
                Log.d(TAG, "start uninstall_hide_intent");
                barrierUnInstall.await();
                break;
            case AppOpHideMgr.CLEAR:
                Intent clear_hide_intent = new Intent("android.intent.action.CLEAR_HIDE");
                clear_hide_intent.setData(Uri.parse("package:" + file_pkg));
                context.startActivity(clear_hide_intent);
                Log.d(TAG, "start clear_hide_intent");
                barrierClear.await();
                break;
        }

        Log.d(TAG, "call exit");
        context.unregisterReceiver(receiver);
        return result;
    }

    private class InstallPackageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive " + action);
            if (Intent.ACTION_PACKAGE_ADDED.equals(action) || Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                String packageName = intent.getData().getEncodedSchemeSpecificPart();
                Log.d(TAG, packageName);
                result = true;
                try {
                    barrierInstall.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "InstallPackageReceiver exit");
            }

            if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                String packageName = intent.getData().getEncodedSchemeSpecificPart();
                Log.d(TAG, packageName);
                result = file_pkg.equals(packageName);
                try {
                    barrierUnInstall.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "UnInstallPackageReceiver exit");
            }

            if (Intent.ACTION_PACKAGE_DATA_CLEARED.equals(action)) {
                String packageName = intent.getData().getEncodedSchemeSpecificPart();
                Log.d(TAG, packageName);
                result = file_pkg.equals(packageName);
                try {
                    barrierClear.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "ClearPackageReceiver exit");
            }
        }
    }
}
