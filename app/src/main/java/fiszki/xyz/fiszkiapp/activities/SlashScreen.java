package fiszki.xyz.fiszkiapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import fiszki.xyz.fiszkiapp.activities.LoginActivity;
import fiszki.xyz.fiszkiapp.activities.MenuActivity;


public class SlashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSharedPreferences("user_data", 0).getString("user_token", "0").equals("0")) {
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
