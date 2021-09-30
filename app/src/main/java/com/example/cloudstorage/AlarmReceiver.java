package com.example.cloudstorage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "Alarm Receiver";
    FirebaseStorage cloudStorage;
    StorageReference mainRef;
    StorageReference file1Ref;

    @Override
    public void onReceive(Context context, Intent intent) {
        cloudStorage = FirebaseStorage.getInstance();
        mainRef = cloudStorage.getReference();
        file1Ref = mainRef.child("file1");
        String filePath = intent.getExtras().getString("File path");
        Log.d("Alarm Receiver", "onReceive: " + filePath);
        if (filePath != null) {
            UploadTask uploadFile1 = file1Ref.putFile(Uri.fromFile(new File(filePath)));
            uploadFile1.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Alarm Receiver", "upload to server failed: ", e);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.R)
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d("Alarm Receiver", "Upload File Successfully");
                    File fileDelete = new File(filePath);
                    Log.d("Alarm Receiver", "File to delete exist: " + fileDelete.exists());
                    Log.d("Alarm Receiver", "start Deleting file: " + fileDelete.getPath());
                    boolean deleted = fileDelete.getAbsoluteFile().delete();
                    if (!deleted) {
                        Log.d(TAG, "Can not delete, file still exist!");
                    } else {
                        Log.d(TAG, "Delete successfully");
                    }
                }
            });

        }
    }
}
