package com.nthu.internetofthing.priend;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nthu.internetofthing.priend.data.AccountHelper;

/**
 * Created by Ywuan on 31/05/2016.
 */
public class SplashActivity extends AppCompatActivity{
    private final String LOG_TAG = SplashActivity.class.getSimpleName();
    private AccountHelper accountHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        accountHelper = new AccountHelper(this);

        if(accountHelper.isEmpty()){
            Intent intent = new Intent(this, LoginActivity.class);
            Log.v(LOG_TAG, "Start login activity");
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(this, PetListActivity.class);
            Log.v(LOG_TAG, "Start petlist activity");
            startActivity(intent);
            finish();
        }

    }
}
