package pnj.jejaringsosial.chandrasa.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import pnj.jejaringsosial.chandrasa.AddPostActivity;
import pnj.jejaringsosial.chandrasa.AddVideoActivity;
import pnj.jejaringsosial.chandrasa.PostDetailActivity;
import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.VideosActivity;
import pnj.jejaringsosial.chandrasa.models.ModelVideo;

public class AdapterVideo extends RecyclerView.Adapter<AdapterVideo.HolderVideo> {


    //context
    private Context context;
    //array list
    private ArrayList<ModelVideo> videoArrayList;

    String myUid;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference likesRef; //likes db
    private DatabaseReference postsRef; //ref post

    ProgressDialog pd;

    boolean mProcessLike = false;

    //constructor
    public AdapterVideo(Context context, ArrayList<ModelVideo> videoArrayList) {
        this.context = context;
        this.videoArrayList = videoArrayList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @NonNull
    @NotNull
    @Override
    public HolderVideo onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //inflate layout row video xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_video, parent, false);

        return new HolderVideo(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HolderVideo holder, int i) {
        //Get, format, handle click

        //get data
        ModelVideo modelVideo = videoArrayList.get(i);

        String uid = modelVideo.getUid();
        String pTitle = modelVideo.getpTitle();
        String pTimeStamps = modelVideo.getpTime();
        String videoUrl = modelVideo.getVideoUrl();
        String uEmail = modelVideo.getuEmail();
        String uName = modelVideo.getuName();
        String uDp = modelVideo.getuDp();
        String pId = modelVideo.getpId();
        String pLikes = modelVideo.getpLikes(); //total likes
        String pComments = modelVideo.getpComments(); //total comment

        //convert timestamp
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(pTimeStamps));
        String formattedDateTime = DateFormat.format("dd/MM/yyyy K:mm a", calendar).toString();

        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //set data
        holder.titleTv.setText(pTitle);
        holder.timeTv.setText(formattedDateTime);
        setVideoUrl(modelVideo, holder, pId);

        //set data
        holder.titleTv.setText(pTitle);

        //uploadedBy
        if (uName.equals("")){
            try {
                holder.uploadedBy.setText("This video is uploaded by "+ uEmail);
                holder.nameTv.setText(uEmail);
            }
            catch (Exception e) {
            }
        }
        else {
            holder.uploadedBy.setText("This video is uploaded by "+ uName);
            holder.nameTv.setText(uName);
        }

        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.uPictureIv);
        }
        catch (Exception e) {
            holder.uPictureIv.setImageResource(R.drawable.ic_default_img);

        }

        //more btn click

        //handle more btn click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.moreBtn, Gravity.CENTER);

                String uid = modelVideo.getUid();
                if (uid.equals(myUid)){
                    //add items menu
                    popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
                    popupMenu.getMenu().add(Menu.NONE,2,0,"Delete");
                }

                else {
                    holder.moreBtn.setVisibility(View.GONE);
                }

                //add items
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if (id==1) {
                            showEditVideoDialog(pTitle, pId);
                        }
                        else if (id==2) {
                            deleteVideo(modelVideo);
                        }

                        return false;
                    }
                });
                //show menu
                popupMenu.show();
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
                String filePathAndName = "Videos/" + "video_" + pTimestamps;

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("pTitle", newPostTitle);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Videos");
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

    private void deleteVideo(ModelVideo modelVideo) {
        final String videoId = modelVideo.getpId();
        String videoUrl = modelVideo.getVideoUrl();

        //delete from firebase storage
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //deleted, now delete on firebase db
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Videos");
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

    private void setVideoUrl(ModelVideo modelVideo, HolderVideo holder, String pId) {
        //get video url
        String videoUrl = modelVideo.getVideoUrl();

        //media controller
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(holder.videoView);

        Uri videoUri = Uri.parse(videoUrl);
        //holder.videoView.setMediaController(mediaController);
        holder.videoView.setVideoURI(videoUri);

        holder.videoView.requestFocus();
        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
             mediaPlayer.start();
             mediaPlayer.setLooping(true);
            }
        });

        holder.videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
                    holder.progressBar.setVisibility(View.GONE);
                }
                if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
                    holder.progressBar.setVisibility(View.VISIBLE);
                }
                if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
                    holder.progressBar.setVisibility(View.GONE);
                }
                return false;
            }
        });

        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start(); //restart video on complete

            }
        });

    }

    @Override
    public int getItemCount() {
        return videoArrayList.size(); // return size of list
    }

    //View holder class
    class HolderVideo extends RecyclerView.ViewHolder {

        //ui view row video xml
        VideoView videoView;
        TextView titleTv, timeTv, nameTv, uploadedBy;
        ImageView uPictureIv;;
        ProgressBar progressBar;
        ImageButton moreBtn;

        public HolderVideo(@NonNull @NotNull View itemView) {
            super(itemView);

            //init ui view row video xml
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            videoView = itemView.findViewById(R.id.videoView);
            nameTv = itemView.findViewById(R.id.nameTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            progressBar = itemView.findViewById(R.id.progressBar);
            uploadedBy = itemView.findViewById(R.id.uploadedBy);
            moreBtn = itemView.findViewById(R.id.moreBtn);

        }
    }
}
