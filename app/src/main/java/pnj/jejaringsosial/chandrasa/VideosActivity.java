package pnj.jejaringsosial.chandrasa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class VideosActivity extends AppCompatActivity {

    //UI view
    FloatingActionButton addVideosBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //actionbar title
        setTitle("Videos");

        //init view
        addVideosBtn = findViewById(R.id.addVideosBtn);
        addVideosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start activity add video
                startActivity(new Intent(VideosActivity.this, AddVideoActivity.class));

            }
        });
    }
}