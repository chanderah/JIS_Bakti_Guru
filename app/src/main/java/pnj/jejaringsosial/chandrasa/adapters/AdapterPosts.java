package pnj.jejaringsosial.chandrasa.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IInterface;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import pnj.jejaringsosial.chandrasa.AddPostActivity;
import pnj.jejaringsosial.chandrasa.PostDetailActivity;
import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.UserProfileActivity;
import pnj.jejaringsosial.chandrasa.models.ModelPost;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    Context context;
    List<ModelPost> postList;

    boolean paused = false;

    String myUid;

    private DatabaseReference likesRef; //likes db
    private DatabaseReference postsRef; //ref post

    boolean mProcessLike = false;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row post
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        ModelPost modelPost = postList.get(i);

        //get data
        String uid = modelPost.getUid();
        String uEmail = modelPost.getuEmail();
        String uName = modelPost.getuName();
        String uDp = modelPost.getuDp();
        String pId = modelPost.getpId();
        String pTitle = modelPost.getpTitle();
        String pDesc = modelPost.getpDesc();
        String pImage = modelPost.getpImage();
        String pTimeStamp = modelPost.getpTime();
        String pLikes = modelPost.getpLikes(); //total likes
        String pComments = modelPost.getpComments(); //total comment
        String videoUrl = modelPost.getVideoUrl(); //total comment
        String type = modelPost.getType(); //total comment

        //convert timestamp
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yy hh:mm aa", calendar).toString();

        //set data
        myHolder.pTimeTv.setText(pTime);
        myHolder.pTitleTv.setText(pTitle);
        myHolder.pDescriptionTv.setText(pDesc);
        myHolder.pLikesTv.setText(pLikes + " Likes");
        myHolder.pCommentsTv.setText(pComments +" Comments");

        //set like of each post
        setLikes(myHolder, pId);

        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(myHolder.uPictureIv);
        }
        catch (Exception e) {
            myHolder.uPictureIv.setImageResource(R.drawable.ic_default_img);
        }

        if (type.equals("photo")) {
            //this is image
            Picasso.get().load(pImage).into(myHolder.pImageIv);
            myHolder.pImageIv.setVisibility(View.VISIBLE);
            myHolder.progressBar.setVisibility(View.GONE);
            myHolder.videoView.setVisibility(View.GONE);
            if (uName.equals("")){
                try {
                    myHolder.uEmailTv.setText(uEmail);
                    myHolder.uEmailTv.setVisibility(View.VISIBLE);
                    myHolder.uNameTv.setVisibility(View.GONE);
                    myHolder.uploadedBy.setText("This image is uploaded by "+ uEmail);

                }
                catch (Exception e) {
                }
            }
            else {
                myHolder.uNameTv.setText(uName);
                myHolder.uNameTv.setVisibility(View.VISIBLE);
                myHolder.uEmailTv.setVisibility(View.GONE);

                myHolder.uploadedBy.setText("This image is uploaded by "+ uName);
                myHolder.uploadedBy.setVisibility(View.VISIBLE);
            }
        }
        else {
            //set post video
            setVideoUrl(modelPost, myHolder, pId);
            myHolder.videoView.setVisibility(View.VISIBLE);
            myHolder.pImageIv.setVisibility(View.GONE);
            myHolder.pDescriptionTv.setVisibility(View.GONE);

            if (uName.equals("")){
                try {
                    myHolder.uEmailTv.setText(uEmail);
                    myHolder.uEmailTv.setVisibility(View.VISIBLE);
                    myHolder.uNameTv.setVisibility(View.GONE);
                    myHolder.uploadedBy.setText("This video is uploaded by "+ uEmail);

                }
                catch (Exception e) {
                }
            }
            else {
                myHolder.uNameTv.setText(uName);
                myHolder.uNameTv.setVisibility(View.VISIBLE);
                myHolder.uEmailTv.setVisibility(View.GONE);

                myHolder.uploadedBy.setText("This video is uploaded by "+ uName);
                myHolder.uploadedBy.setVisibility(View.VISIBLE);
            }
        }

        //handle btn click
        myHolder.moreBtn.setOnClickListener(v -> {
            if (pImage==null){
                PopupMenu popupMenu = new PopupMenu(context, myHolder.moreBtn, Gravity.CENTER);

                if (uid.equals(myUid)){
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
                            deleteVideo(modelPost);
                        }
                        if (id==0) {
                            //start postdetailactivity
                            Intent intent = new Intent(context, PostDetailActivity.class);
                            intent.putExtra("postId", pId); //get detail post with this id
                            context.startActivity(intent);                        }

                        return false;
                    }
                });
                //show menu
                popupMenu.show();
            }
            else {
                showMoreOptions(myHolder.moreBtn, uid, myUid, pId, pImage);
            }
        });
        myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                int pLikes = Integer.parseInt(postList.get(i).getpLikes());
                mProcessLike = true;
                //get id of the post clicked
                String postIde = postList.get(i).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        if (mProcessLike) {
                            if (dataSnapshot.child(postIde).hasChild(myUid)) {
                                //already liked, remove like
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes-1));
                                likesRef.child(postIde).child(myUid).removeValue();
                                mProcessLike = false;
                            }
                            else {
                                //not liked, like it
                                postsRef.child(postIde).child("pLikes").setValue(""+(pLikes+1));
                                likesRef.child(postIde).child(myUid).setValue("Liked"); //set
                                mProcessLike = false;

                                addToHisNotifications(""+uid, ""+pId, "Liked your post");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                    }
                });
            }
        });
        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start postdetailactivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); //get detail post with this id
                context.startActivity(intent);
            }
        });

        myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click to go profile with uid, uid of clicked user
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });

    }

    private void deleteVideo(ModelPost modelPost) {
        final String videoId = modelPost.getpId();
        String videoUrl = modelPost.getVideoUrl();

        //delete from firebase storage
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //deleted, now delete on firebase db
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                        databaseReference.child(videoId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Video deleted...", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void setVideoUrl(ModelPost modelPost, MyHolder myHolder, String pId) {
        //get video url
        String videoUrl = modelPost.getVideoUrl();

        //media controller
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(myHolder.videoView);

        Uri videoUri = Uri.parse(videoUrl);
        //holder.videoView.setMediaController(mediaController);
        myHolder.videoView.setVideoURI(videoUri);
        myHolder.videoView.seekTo(1);

        myHolder.videoView.requestFocus();

        myHolder.videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paused) {
                    myHolder.videoView.start();
                    paused = false;
                    myHolder.playIv.setVisibility(View.GONE);
                }
                else {
                    myHolder.videoView.pause();
                    paused = true;
                    myHolder.playIv.setVisibility(View.VISIBLE);
                }
            }
        });

        myHolder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                myHolder.progressBar.setVisibility(View.GONE);
                myHolder.playIv.setVisibility(View.VISIBLE);
            }
        });

        myHolder.videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
                    myHolder.progressBar.setVisibility(View.GONE);
                    myHolder.progressBar.setVisibility(View.GONE);
                }
                if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
                    myHolder.progressBar.setVisibility(View.VISIBLE);
                }
                if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
                    myHolder.progressBar.setVisibility(View.GONE);
                    myHolder.progressBar.setVisibility(View.GONE);
                }
                return false;
            }
        });

        myHolder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                myHolder.playIv.setVisibility(View.VISIBLE);
                myHolder.videoView.seekTo(1);
            }
        });

    }

    private void showEditVideoDialog(String pTitle, String pId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Post");

        LinearLayout linearLayout = new LinearLayout(context);

        EditText pTitleEt = new EditText(context);
        pTitleEt.setText(pTitle);

        pTitleEt.setMinEms(20);

        linearLayout.addView(pTitleEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        final ProgressDialog pd = new ProgressDialog(context);
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
                                Toast.makeText(context, "Updated...", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                //fail
                                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void setLikes(MyHolder holder, String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myUid)){
                    //user has liked this post
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    holder.likeBtn.setText("Liked");
                }
                else {
                    //user has not liked this post
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_white,0,0,0);
                    holder.likeBtn.setText("Like");

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        if (uid.equals(myUid)){
            //add items menu
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
            popupMenu.getMenu().add(Menu.NONE,2,0,"Delete");
        }
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "View Detail");

        //add items
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==0) {
                    //start postdetailactivity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId); //get detail post with this id
                    context.startActivity(intent);
                }
                else if (id==1) {
                    //edit clicked
                    //start AddpostActivity key "editPost" id post clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);

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

        final ProgressDialog pd = new ProgressDialog(context);
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
                                Toast.makeText(context, "Deleted successfully...", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //viewholder class
    class MyHolder extends RecyclerView.ViewHolder {

        //views from row post xml
        ImageView uPictureIv, pImageIv, playIv;
        VideoView videoView;
        TextView uNameTv, pTimeTv, pTitleTv, pDescriptionTv, pLikesTv, pCommentsTv, uploadedBy, uEmailTv;
        ImageButton moreBtn;
        Button likeBtn, commentBtn;
        LinearLayout profileLayout;
        ProgressBar progressBar;

        public MyHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            //init views
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.pImageIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pTitleTv = itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            profileLayout = itemView.findViewById(R.id.profileLayout);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            uploadedBy = itemView.findViewById(R.id.uploadedBy);
            uEmailTv = itemView.findViewById(R.id.uEmailTv);
            progressBar = itemView.findViewById(R.id.progressBarr);
            videoView = itemView.findViewById(R.id.pVideoView);
            playIv = itemView.findViewById(R.id.playIv);

        }
    }
}
