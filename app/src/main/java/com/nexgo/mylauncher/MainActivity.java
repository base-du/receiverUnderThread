package com.nexgo.mylauncher;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends ActionBarActivity {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String EXTERNAL_PATH = "/mnt/external_sdcard/";
    private UsbManager usbManager = null;// USB管理器
    private UsbDevice usbDevice = null;// 代表一个USB设备
    private UsbEndpoint inEndpoint = null; // 读数据节点
    private UsbEndpoint outEndpoint = null;// 写数据节点
    private UsbDeviceConnection connection = null;// USB连接
    private UsbInterface usbInterface = null;// USB接口
    /**
     * 广播接收者，接收来自USB授权的广播
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("action", action);

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            getconnection();// 如果用户授权，则打开连接，并开启读线程
//                            new MyThread3().start();
                        }
                    } else {
                        Log.d("denied", "permission denied for device "
                                + usbDevice);
                    }
                }
            }

        }
    };
    private PendingIntent pendingIntent;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
//        Intent install_hide_intent = new Intent("android.intent.action.VIEW_HIDE");
//        install_hide_intent.setDataAndType(Uri.parse("file:///sdcard/app.apk"), "application/vnd.android.package-archive");
//        startActivityForResult(install_hide_intent, 1001);
//        startActivity(install_hide_intent);

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(new InstallThread(this, "file:///sdcard/app.apk"));
    }

    public void OnUnInstall(View view) {
        Intent install_hide_intent = new Intent("android.intent.action.DELETE_HIDE");
        Uri uri = Uri.parse("package:com.qihoo.appstore");
        install_hide_intent.setData(uri);
        startActivityForResult(install_hide_intent, 1001);
    }

    public void OnClear(View view) {
        Intent install_hide_intent = new Intent("android.intent.action.CLEAR_HIDE");
        Uri uri = Uri.parse("package:com.qihoo.appstore");
        install_hide_intent.setData(uri);
        startActivity(install_hide_intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1000:
                if (RESULT_OK == resultCode) {
                    Toast.makeText(this, "Install successfully!", Toast.LENGTH_SHORT).show();
                }
                break;

            case 1001:
                if (RESULT_OK == resultCode) {
                    Toast.makeText(this, "Uninstall successfully!", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1002:
                if (RESULT_OK == resultCode) {
                    Toast.makeText(this, "Clear successfully!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    void log(String line) {
        Log.i("USB_HOST_DEBUG", line);
    }

    public void doEnum(View view) {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            log("Unable to access to USB manager");
            return;
        }

        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        HashMap<String, UsbDevice> connectedDevices = usbManager.getDeviceList();
        if (connectedDevices == null || connectedDevices.size() == 0) {
            log("No USB devices detected");
            return;
        }
        for (UsbDevice usbdevice : connectedDevices.values()) {
            int deviceId = usbdevice.getProductId();
            int vendorId = usbdevice.getVendorId();
            int interfaceCount = usbdevice.getInterfaceCount();
            if (0x05fe == usbdevice.getVendorId()
                    && 0x1010 == usbdevice.getProductId()) {
                usbDevice = usbdevice;
                log("====found Chic Optical Wireless=======");
            }
            int configurationCount = 0;
            String tmp_serial = "unknown";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tmp_serial = usbdevice.getSerialNumber();
                configurationCount = usbdevice.getConfigurationCount();
            }
            log("====================================");
            log(String.format("Detected %x:%x device (serial=%s)", vendorId, deviceId, tmp_serial));
            if (interfaceCount < 1) {
                log(String.format("drop device because it has no interfaces %x:%x:%s %d interfaces", vendorId, deviceId, tmp_serial, interfaceCount));
            }
            String line = String.format("%d configuration and %d interface reported by UsbDevice object", configurationCount, interfaceCount);
            if (interfaceCount < 1) {
                line = "!!!! " + line + " !!!!";
            }
            log(line);
        }
    }

    public void doPerm(View view) {
        // 程序是否有操作设备的权限
        if (usbManager.hasPermission(usbDevice)) {
            getconnection();// 如果用户授权，则打开连接，并开启读线程
//            new MyThread3().start();
        } else {
            // 没有权限询问用户是否授予权限
            usbManager.requestPermission(usbDevice, pendingIntent); // 该代码执行后，系统弹出一个对话框，
        }
    }

    private void getconnection() {
        if (usbDevice == null) {
            log("USB还未连接，请先开启USB连接再重试");
            return;
        }
        connection = null;
        usbInterface = null;
        inEndpoint = null;
        outEndpoint = null;
        int count = usbDevice.getInterfaceCount();// 接口数量
        log("interfacecount-->" + count);
        usbInterface = usbDevice.getInterface(1);
        int count1 = usbInterface.getEndpointCount();// 该接口上的接入点数量
        log("endpointcount-->" + usbInterface.getEndpointCount() + "class:" + usbInterface.getInterfaceClass());
        connection = usbManager.openDevice(usbDevice);// 打开连接
        boolean a = connection.claimInterface(usbInterface, true);
        for (int j = 0; j < count1; j++) {
            log("a-->" + a + ";" + "permission-->"
                    + usbManager.hasPermission(usbDevice) + ";"
                    + "serial-->" + connection.getSerial() + ";"
                    + "endpoint" + j + "-->"
                    + usbInterface.getEndpoint(j) + " dir:"
                    + usbInterface.getEndpoint(j).getDirection());
            if (usbInterface.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT) {
                outEndpoint = usbInterface.getEndpoint(j); // 写数据节点
            } else if (usbInterface.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN) {
                inEndpoint = usbInterface.getEndpoint(j); // 读数据节点
            }
        }
        log("getconnection()-> connection:" + connection
                + ";outEndpoint" + outEndpoint + ";inEndpoint" + inEndpoint);

    }

    public void doConn(View view) {
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void copyFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
//            File dir = new File(outputPath);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public void OnSDCard(View view) {
        log(Environment.getExternalStorageState());
        log(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
        log(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        log(Environment.getExternalStorageDirectory().getPath());
        log(Environment.getExternalStorageDirectory().getAbsolutePath());

        try {

            // Executes the command.

//            Process process = Runtime.getRuntime().exec("/system/bin/ls /mnt/external_sdcard/1.apk");
            Process process = Runtime.getRuntime().exec("/system/bin/cd /mnt/external_sdcard/; /system/bin/cp 1.apk 3.apk;");

            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();

            log(output.toString());
        } catch (IOException e) {

            throw new RuntimeException(e);

        } catch (InterruptedException e) {

            throw new RuntimeException(e);
        }
//        copyFile(EXTERNAL_PATH, "1.apk", EXTERNAL_PATH + "new/");

//        File old = new File(EXTERNAL_PATH, "1.apk");
//        File newer = new File(EXTERNAL_PATH, "new.apk");
//        try {
//            copy(old, newer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
