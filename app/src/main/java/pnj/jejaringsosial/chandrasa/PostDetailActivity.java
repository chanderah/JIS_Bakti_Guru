package pnj.jejaringsosial.chandrasa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import pnj.jejaringsosial.chandrasa.adapters.AdapterComments;
import pnj.jejaringsosial.chandrasa.models.ModelComment;
import pnj.jejaringsosial.chandrasa.models.ModelPost;

public class PostDetailActivity extends AppCompatActivity {

    //get detail of user post
    String hisUid, myUid, myEmail, myName, myDp, postId, pLikes, hisDp, hisName, pImage, hisEmail, videoUrl, pTitle, pId;

    boolean mProcessComment = false;
    boolean mProcessLike = false;
    boolean paused = false;

    //progress
    ProgressDialog pd;

    //views
    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTiv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv, uploadedBy, emailTv;
    ImageButton moreBtn;
    Button likeBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;

    List<ModelComment> commentList;
    List<ModelPost> postList;
    AdapterComments adapterComments;

    //add comments view
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv, playIv;
    VideoView videoView;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        //actionbar prop
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //get post id using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //init view
        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTiv = findViewById(R.id.pTimeTiv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerViewComment);
        uploadedBy = findViewById(R.id.uploadedBy);
        emailTv = findViewById(R.id.emailTv);
        videoView = findViewById(R.id.pVideoView);
        progressBar = findViewById(R.id.progressBarr);
        playIv = findViewById(R.id.playIv);

        commentEt = findViewById(R.id.commentEt);
        cAvatarIv = findViewById(R.id.cAvatarIv);
        sendBtn = findViewById(R.id.sendBtn);

        loadPostInfo();

        checkUserStatus();
        
        loadUserInfo();

        setLikes();

        loadComments();

        //set subtitle actionbar
        actionBar.setSubtitle(myEmail);
        
        //send comment btn click
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        //like btn click
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        //handle btn click
        moreBtn.setOnClickListener(v -> {
            if (pImage==null){
                PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.CENTER);

                if (hisUid.equals(myUid)){
                    //add items menu
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
                    popupMenu.getMenu().add(Menu.NONE,2,0,"Delete");
                }

                else {
                    popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Detail");
                }

                //add items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (id==1) {
                            showEditVideoDialog(pTitle, pId);
                        }
                        if (id==2) {
                            deleteVideo(pId, videoUrl);
                        }
                        return false;
                    }
                });
                //show menu
                popupMenu.show();
            }
            else {
                showMoreOptions(moreBtn, hisUid, myUid, pId, pImage);
            }
        });
    }

    private void deleteVideo(String pId, String videoUrl) {

        //delete from firebase storage
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //deleted, now delete on firebase db
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        databaseReference.child(pId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(PostDetailActivity.this, "Video deleted...", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
                });
    }

    private void showEditVideoDialog(String pTitle, String pId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Post");

        LinearLayout linearLayout = new LinearLayout(this);

        EditText pTitleEt = new EditText(this);
        pTitleEt.setText(pTitle);

        pTitleEt.setMinEms(20);

        linearLayout.addView(pTitleEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Updating...");

        //buttons recover
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd.show();
                String newPostTitle = pTitleEt.getText().toString().trim();
                String pTimestamps = ""+ System.currentTimeMillis();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("pTitle", newPostTitle);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                reference.child(pId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                pd.dismiss();
                                Toast.makeText(PostDetailActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                //fail
                                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
            }
        });
        //buttons recover
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout to rv
        recyclerView.setLayoutManager(layoutManager);

        //init comments list
        commentList = new ArrayList<>();

        //path post get comment
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);



                    //setup adapter
                    adapterComments = new AdapterComments(getApplicationContext(), commentList, myUid, postId);
                    //set adapter
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        if (uid.equals(myUid)){
            //add items menu
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
            popupMenu.getMenu().add(Menu.NONE,2,0,"Delete");
        }

        //add items
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==1) {
                    //edit clicked
                    //start AddpostActivity key "editPost" id post clicked
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    startActivity(intent);

                }

                else if (id==2){
                    //delete clicked
                    beginDelete(pId, pImage);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(final String pId, String pImage) {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //image deleted, now delete database
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                    ds.getRef().removeValue(); //remove values matched pid

                                }
                                //deleted
                                Toast.makeText(PostDetailActivity.this, "Deleted successfully...", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //failed, cant go further
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void setLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).hasChild(myUid)){
                    //user has liked this post
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    likeBtn.setText("Liked");
                }
                else {
                    //user has not liked this post
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_white,0,0,0);
                    likeBtn.setText("Like");

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
        mProcessLike = true;
        //get id of the post clicked
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (mProcessLike) {
                    if (dataSnapshot.child(postId).hasChild(myUid)) {
                        //already liked, remove like
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;

                    }
                    else {
                        //not liked, like it
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked"); //set
                        mProcessLike = false;

                        addToHisNotifications(""+hisUid, ""+postId, "Liked your post");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding comment...");
        
        //get data from et
        String comment = commentEt.getText().toString().trim();;
        //validate
        if (TextUtils.isEmpty(comment)) {
            //no type
            Toast.makeText(this, "Please enter a comment.", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());

        //each post have child "comment"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        //put info in hashmap
        hashMap.put("cId", timeStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uDp", myDp);
        hashMap.put("uName", myName);

        //put data in db
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Comment Added...", Toast.LENGTH_SHORT).show();
                        commentEt.setText("");
                        updateCommentCount();

                        addToHisNotifications(""+hisUid, ""+postId, "Commented your post");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCommentCount() {
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (mProcessComment) {
                    String comments = ""+ dataSnapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
        //get user info
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    //set dp
                    try {
                        //set img
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img).into(cAvatarIv);
                    }
                    catch (Exception e){

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    //get data
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDesc = ""+ds.child("pDesc").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    String pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    hisEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();
                    videoUrl = ""+ds.child("videoUrl").getValue();
                    String type = "" + ds.child("type").getValue();

                    //convert timestamp
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yy hh:mm aa", calendar).toString();

                    //set data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDesc);
                    pLikesTv.setText(pLikes + " Likes");
                    pTimeTiv.setText(pTime);
                    pCommentsTv.setText(commentCount +" Comments");

                    if (type.equals("photo")) {
                        //this is image
                        Picasso.get().load(pImage).into(pImageIv);
                        pImageIv.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);

                        setPhotoNameOrEmailTv();
                    }
                    else {
                        //set post video
                        pImageIv.setVisibility(View.GONE);
                        videoView.setVisibility(View.VISIBLE);
                        pDescriptionTv.setVisibility(View.GONE);

                        setVideoNameOrEmailTv();
                        setVideoToView();
                    }


                    //set user img in comment part
                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img).into(uPictureIv);
                    }
                    catch (Exception e) {
                        uPictureIv.setImageResource(R.drawable.ic_default_img);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });

    }

    private void setVideoToView() {
        //media controller
        MediaController mediaController = new MediaController(PostDetailActivity.this);
        mediaController.setAnchorView(videoView);

        Uri videoUri = Uri.parse(videoUrl);
        //holder.videoView.setMediaController(mediaController);
        videoView.setVideoURI(videoUri);
        videoView.seekTo(1);

        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressBar.setVisibility(View.GONE);
                playIv.setVisibility(View.VISIBLE);
            }
        });

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paused) {
                    videoView.start();
                    paused = false;
                    playIv.setVisibility(View.GONE);
                }
                else {
                    videoView.pause();
                    paused = true;
                    playIv.setVisibility(View.VISIBLE);
                }
            }
        });


        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
                    progressBar.setVisibility(View.GONE);
                    playIv.setVisibility(View.GONE);

                }
                if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
                    progressBar.setVisibility(View.GONE);
                    playIv.setVisibility(View.GONE);

                }
                return false;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playIv.setVisibility(View.VISIBLE);
                videoView.seekTo(1);
            }
        });
    }

    private void setVideoNameOrEmailTv() {
        if (hisName.equals("")){
            //no name, set email
            emailTv.setText(hisEmail);
            emailTv.setVisibility(View.VISIBLE);
            uNameTv.setVisibility(View.GONE);
            uploadedBy.setText("This video is uploaded by "+ hisEmail);
        }
        else {
            //have name, set name
            uNameTv.setText(hisName);
            uNameTv.setVisibility(View.VISIBLE);
            emailTv.setVisibility(View.GONE);
            uploadedBy.setText("This video is uploaded by "+ hisName);
        }
    }

    private void setPhotoNameOrEmailTv() {
        if (hisName.equals("")){
            emailTv.setText(hisEmail);
            emailTv.setVisibility(View.VISIBLE);
            uNameTv.setVisibility(View.GONE);
            uploadedBy.setText("This image is uploaded by "+ hisEmail);
        }
        else {
            uNameTv.setText(hisName);
            uNameTv.setVisibility(View.VISIBLE);
            emailTv.setVisibility(View.GONE);

            uploadedBy.setText("This image is uploaded by "+ hisName);
            uploadedBy.setVisibility(View.VISIBLE);
        }
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            //signed in
            myEmail = user.getEmail();
            myUid = user.getUid();
        }
        else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void addToHisNotifications(String hisUid, String pId, String notification){
        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
                });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_video).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}