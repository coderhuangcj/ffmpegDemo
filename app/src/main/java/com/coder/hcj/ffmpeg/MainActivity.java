package com.coder.hcj.ffmpeg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestNeedPermission();
    }


    private void requestNeedPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<String>();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                permissions.add(Manifest.permission.CAMERA);
            }


            if (permissions.size() > 0) {
                String[] permissionStr = new String[permissions.size()];
                for (int i = 0; i < permissions.size(); i++) {
                    permissionStr[i] = permissions.get(i);
                }
                requestPermissions(permissionStr, 100);
            } else {
                start();
            }
        } else {
            start();
        }
    }

    private void start() {
        startActivity(new Intent(this, FFmpegActivity.class));
    }

    public static boolean checkAllPermissionIsGrant(int[] grantResults) {
        if (grantResults == null) {
            return false;
        }
        for (int item : grantResults) {
            if (item == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && checkAllPermissionIsGrant(grantResults)) {
                start();
            } else {
                Toast.makeText(this, "请授权", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
