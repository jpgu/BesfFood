package com.mobitant.besffood;

/**
 * Created by jp on 2019-01-21.
 */

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PermissionActivity extends AppCompatActivity {
    private static final int PERMISSION_MULTI_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(android.R.layout.activity_permission);

        if(Build.VERSION.SDK_INT < 23) {
            goIndexActivity();
        }
        else {
            if(checkAndRequestPermission()){
                goIndexActivity();
            }
        }
    }


    /* 권한을 확인하고 부여되어 있지 않다면 권한을 요청 */
    private boolean checkAndRequestPermission(){
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        List<String> listPermissionNeeded = new ArrayList<>();

        for(String permission:permissions){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                listPermissionNeeded.add(permission);
            }
        }

        if(!listPermissionNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]),PERMISSION_MULTI_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
        if(grantResults.length == 0) return;

        switch(requestCode){
            case PERMISSION_MULTI_CODE:
                checkPermissionResult(permissions, grantResults);

                break;
        }
    }

    private void checkPermissionResult(String[] permissions, int[] grantResults){
        boolean isAllGranted = true;

        for(int i=0; i<permissions.length; i++){
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                isAllGranted = false;
            }
        }

        if(isAllGranted){
            goIndexActivity();
        }
        else{
            showPermissionDialog();
        }
    }

    private void goIndexActivity(){
        Intent intent = new Intent(this, IndexActivity.class);
        startActivity(intent);

        finish();
    }

    private void showPermissionDialog(){
        AlertDialog.Builder dialog = new AlertDialog().Builder(this);
        dialog.setTitle(R.string.permission_setting_title);
        dialog.setMessage(R.string.permission_setting_message);
        dialog.setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                MyToast.s(PermissionActivity.this, R.string.permission_setting_restart);
                PermissionActivity.this.finish();
                goAppSettingActivity();
            }
        });

        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
                PermissionActivity.this.finish();
            }
        });
        dialog.show();
    }

    private void goAppSettingActivity(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
