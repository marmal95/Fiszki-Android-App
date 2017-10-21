package fiszki.xyz.fiszkiapp.activities;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import fiszki.xyz.fiszkiapp.utils.Constants;
import fiszki.xyz.fiszkiapp.R;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        setTitle(getString(R.string.result));

        displayResult();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ResultActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void displayResult(){

        TextView scoreInfo = (TextView)findViewById(R.id.scoreInfo);
        int percScore = getIntent().getIntExtra(Constants.USER_RESULT, 0);

        String strScore = String.valueOf(percScore) + "%";
        scoreInfo.setText(strScore);

        if(percScore >= 90.0 && percScore <= 100.0)
            scoreInfo.setTextColor(ContextCompat.getColor(ResultActivity.this, R.color.veryGoodScore));
        else if(percScore < 90.0 && percScore >= 70.0)
            scoreInfo.setTextColor(ContextCompat.getColor(ResultActivity.this, R.color.goodScore));
        else if(percScore < 70.0 && percScore >= 30.0)
            scoreInfo.setTextColor(ContextCompat.getColor(ResultActivity.this, R.color.middleScore));
        else if(percScore < 30.0 && percScore >= 10.0)
            scoreInfo.setTextColor(ContextCompat.getColor(ResultActivity.this, R.color.weakScore));
        else if(percScore < 10.0)
            scoreInfo.setTextColor(ContextCompat.getColor(ResultActivity.this, R.color.veryWeakScore));

        ProgressBar pr = findViewById(R.id.progressBar);
        pr.setMax(200);
        pr.setProgress(150);
        pr.setMax(100);
        pr.setProgress(percScore);
        pr.setProgress(percScore);
    }
}
