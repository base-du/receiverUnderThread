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
    private String file;
    private BroadcastReceiver installReceiver;
    private CyclicBarrier barrier;

    public InstallThread(Context context, String file) {
        this.context = context;
        this.file = file;
        barrier = new CyclicBarrier(PARTICIPANTS);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
//        filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
//        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        installReceiver = new InstallPackageReceiver();
        context.registerReceiver(installReceiver, filter);
    }

    @Override
    public Boolean call() throws Exception {
        Intent install_hide_intent = new Intent("android.intent.action.VIEW_HIDE");
        install_hide_intent.setDataAndType(Uri.parse(file),
                "application/vnd.android.package-archive");
        context.startActivity(install_hide_intent);

        Log.d(TAG, "start install_hide_intent");
        barrier.await();
        Log.d(TAG, "exit");
        return true;
    }


    private class InstallPackageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_PACKAGE_ADDED.equals(action) || Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                String packageName = intent.getData().getEncodedSchemeSpecificPart();
                Log.d(TAG, packageName);
                context.unregisterReceiver(installReceiver);
                try {
                    barrier.await();
                    Log.d(TAG, "InstallPackageReceiver exit");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
