package pnj.jejaringsosial.chandrasa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import pnj.jejaringsosial.chandrasa.adapters.AdapterVideo;
import pnj.jejaringsosial.chandrasa.models.ModelVideo;

public class VideosActivity extends AppCompatActivity {

    //UI view
    FloatingActionButton addVideosBtn;
    private RecyclerView videosRv;

    FirebaseAuth firebaseAuth;

    TextView photosFab;

    ActionBar actionBar;

    //array list
    private ArrayList<ModelVideo> videoArrayList;

    //adapter
    private AdapterVideo adapterVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Videos");

        //enable back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        //init view
        addVideosBtn = findViewById(R.id.addVideosBtn);
        videosRv = findViewById(R.id.videosRv);

        //function call, load vid
        loadVideosFromFirebase();

        //handle click
        addVideosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start activity add video
                startActivity(new Intent(VideosActivity.this, AddVideoActivity.class));

            }
        });

        checkUserStatus();

        photosFab = findViewById(R.id.videosFab);
        photosFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VideosActivity.this, DashboardActivity.class));
                finish();
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //signed user stay here

        }
        else {
            startActivity(new Intent(VideosActivity.this, MainActivity.class));
            finish();
        }
    }

    private void loadVideosFromFirebase() {
        //init arraylist
        videoArrayList = new ArrayList<>();

        //db ref
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Videos");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                //clear list before add data
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //getdata
                    ModelVideo modelVideo = ds.getValue(ModelVideo.class);
                    //add model/data to list
                    videoArrayList.add(modelVideo);
                }
                //setup adapter
                adapterVideo = new AdapterVideo(VideosActivity.this, videoArrayList);
                //set adapter to rv
                videosRv.setAdapter(adapterVideo);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous activity
        return super.onSupportNavigateUp();
    }
}