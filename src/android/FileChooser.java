package com.megster.cordova;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.lang.Exception;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONException;
import org.json.JSONObject;


public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final int PICK_FILE_REQUEST = 1;
    CallbackContext callback;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            chooseFile(callbackContext);
            return true;
        }

        return false;
    }

    public void chooseFile(CallbackContext callbackContext) {

        // type and title should be configurable

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        Intent chooser = Intent.createChooser(intent, "Select File");
        cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_FILE_REQUEST && callback != null) {

            if (resultCode == Activity.RESULT_OK) {

                Uri uri = data.getData();

                if (uri != null) {

                    String mimeType = cordova.getActivity().getContentResolver().getType(uri);
                    String fileName = "";
                    String fileExt  = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                    String base64   = "";

                    // TODO(Moris): check if it's possible to use a separate thread for reading and/or base64-encoding the file to avoid the warning:
                    //
                    //              "THREAD WARNING: exec() call to FileChooser.open blocked the main thread for 20ms. Plugin should use CordovaInterface.getThreadPool()."
                    //
                    //              See: http://www.donmarges.io/thread-warning-exec-call-blocked-the-main-thread-plugin-should-use-cordovainterface-getthreadpool-cordova-plugin-warning/

                    ParcelFileDescriptor mInputPFD;
                    try {
                        mInputPFD = cordova.getActivity().getContentResolver().openFileDescriptor(uri, "r");

                    } catch (FileNotFoundException e) {

                        e.printStackTrace();
                        Log.e("FileChooser", "File not found.");
                        callback.error("File not found");
                        return;
                    }

                    try {
                        FileDescriptor fd = mInputPFD.getFileDescriptor();
                        FileInputStream fis = new FileInputStream(fd);

                        byte[] bytes;
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        ByteArrayOutputStream output = new ByteArrayOutputStream();

                        while ((bytesRead = fis.read(buffer)) > -1) {
                            output.write(buffer, 0, bytesRead);
                        }

                        bytes = output.toByteArray();
                        base64 = Base64.encodeToString(bytes, Base64.DEFAULT);

                    } catch (Exception e) {

                        e.printStackTrace();
                        Log.e("FileChooser", "File read error.");
                        callback.error("File read error");
                    }

                    JSONObject result = new JSONObject();
                    try {
                        result.put("uri", uri.toString());
                        result.put("mime", mimeType);
                        result.put("ext", fileExt);
                        result.put("base64", base64);

                    } catch(JSONException e) {

                        e.printStackTrace();
                        Log.e("FileChooser", "JSON object build error.");
                        callback.error("JSON object build error");
                        return;
                    }

                    callback.success(result);

                } else {

                    callback.error("File uri was null");
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {

                // TODO NO_RESULT or error callback?
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                callback.sendPluginResult(pluginResult);

            } else {

                callback.error(resultCode);
            }
        }
    }
}
