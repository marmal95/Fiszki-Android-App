package fiszki.xyz.fiszkiapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import fiszki.xyz.fiszkiapp.activities.LoginActivity;
import fiszki.xyz.fiszkiapp.activities.MenuActivity;
import fiszki.xyz.fiszkiapp.source.User;


public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user = User.getInstance(getApplicationContext());
        if(user.getUserToken() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
