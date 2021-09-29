package com.example.cloudstorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.cloudstorage.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding mainBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainBinding.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                ) {
                    uploadToServer();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
                    }
                }
            }
        });

    }

    private void uploadToServer() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent = Intent.createChooser(intent, "Choose a file");
        startActivityForResult(intent, 1111);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
                uploadToServer();
            } else {
                Toast.makeText(getApplicationContext(), "Can not upload file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111 && resultCode == RESULT_OK && data != null) {
            Uri fileURI = data.getData();

            String filePath = null;
            try {
                filePath = Utilities.getPath(getApplicationContext(), fileURI);
            } catch (Exception e) {
                Log.e("MainActivity", "onActivityResult: error", e);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, mainBinding.timePicker.getHour());
            calendar.set(Calendar.MINUTE, mainBinding.timePicker.getMinute());
            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            intent.putExtra("File path", filePath);
            Log.d("Main Activity", "send to AlarmReceiver: " + filePath);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }
}