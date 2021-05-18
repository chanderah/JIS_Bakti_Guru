package pnj.jejaringsosial.chandrasa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class AddVideoActivity extends AppCompatActivity {

    //actionbar
    private ActionBar actionBar;
    //ui view
    EditText titleEt;
    VideoView videoView;
    Button uploadVideoBtn;
    FloatingActionButton pickVideoFab;

    private static final int VIDEO_PICK_GALLERY_CODE = 100;
    private static final int VIDEO_PICK_CAMERA_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;

    private String[] cameraPermissions;

    private Uri videoUri = null; //uri picked video
    private String title;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        //init actionbar
        actionBar = getSupportActionBar();
        //title
        actionBar.setTitle("Add New Video");
        //add back btn
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init view
        titleEt = findViewById(R.id.titleEt);
        videoView = findViewById(R.id.videoView);
        uploadVideoBtn = findViewById(R.id.uploadVideoBtn);
        pickVideoFab = findViewById(R.id.pickVideoFab);

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Uploading Video...");
        progressDialog.setCanceledOnTouchOutside(false);

        //camera perms
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //handle click upload video
        uploadVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = titleEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddVideoActivity.this, "Please enter a caption...", Toast.LENGTH_SHORT).show();
                }
                else if (videoUri==null){
                    //video is not picked
                    Toast.makeText(AddVideoActivity.this, "Pick a video first...", Toast.LENGTH_SHORT).show();
                }
                else {
                    //upload video to firebase
                    uploadVideoFirebase();
                }
            }
        });

        //handle click, pick video from camera / gallery
        pickVideoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoPickDialog();
            }
        });
    }

    private void uploadVideoFirebase() {
        //show pd
        progressDialog.show();

        //timestamp
        String timestamp = ""+ System.currentTimeMillis();

        //file path and name in firebase
        String filePathAndName = "Videos/" + "video_" + timestamp;

        //storage ref
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        //upload video
        storageReference.putFile(videoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //vid uploaded, get url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()){
                            //url uploaded video received

                            //video details to db
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", "" + timestamp);
                            hashMap.put("title", title);
                            hashMap.put("timestamp", "" + timestamp);
                            hashMap.put("videoUrl", "" + downloadUri);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Videos");
                            reference.child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //video detail uploaded to db
                                            progressDialog.dismiss();
                                            Toast.makeText(AddVideoActivity.this, "Video uploaded...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            //fail
                                            progressDialog.dismiss();
                                            Toast.makeText(AddVideoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //failed upload to storage
                        progressDialog.dismiss();
                        Toast.makeText(AddVideoActivity.this, ""+e.getMessage() , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void videoPickDialog() {
        //options dialog display
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Video From")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0) {
                            //camera click
                            if (!checkCameraPermission()){
                                //camera perms not allowed, ask
                                requestCameraPermission();
                            }else {
                                //perms allowed, take pict
                                videoPickCamera();
                            }
                        }
                        else if (i==1) {
                            //gallery click
                            videoPickGallery();
                        }

                    }
                })
                .show();
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED;

        return result1 && result2;
    }

    private void videoPickGallery(){
        //pick video from gallery

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Videos"), VIDEO_PICK_GALLERY_CODE);
    }

    private void videoPickCamera(){
        //pick video from camera

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_PICK_CAMERA_CODE);
    }

    private void setVideoToVideoView(){
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        //set media controller to vv
        videoView.setMediaController(mediaController);
        //set video uri
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
                mediaPlayer.setLooping(true);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0){
                    //check permission allowed or not
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        //allowed
                        videoPickCamera();
                    }
                    else {
                        //1 or both denied
                        Toast.makeText(this, "Please allow camera & storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        //called after pick video from cam/gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == VIDEO_PICK_GALLERY_CODE) {
                videoUri = data.getData();
                //show picked video in VideoView
                setVideoToVideoView();
            }
            else if (requestCode == VIDEO_PICK_CAMERA_CODE) {
                videoUri = data.getData();
                //show picked video in VideoView
                setVideoToVideoView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}