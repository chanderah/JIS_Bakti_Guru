package pnj.jejaringsosial.chandrasa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    //array list
    private ArrayList<ModelVideo> videoArrayList;

    //adapter
    private AdapterVideo adapterVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        //actionbar title
        setTitle("Videos");

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
    }

    private void loadVideosFromFirebase() {
        //init arraylist
        videoArrayList = new ArrayList<>();

        //db ref
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Videos");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //clear list before add data
                for (DataSnapshot ds: snapshot.getChildren()){
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
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
}