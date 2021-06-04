package pnj.jejaringsosial.chandrasa.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
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

import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.models.ModelVideo;

public class AdapterVideo extends RecyclerView.Adapter<AdapterVideo.HolderVideo> {


    //context
    private Context context;
    //array list
    private ArrayList<ModelVideo> modelVideo;

    String myUid;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference likesRef; //likes db
    private DatabaseReference postsRef; //ref post

    boolean mProcessLike = false;

    //constructor
    public AdapterVideo(Context context, ArrayList<ModelVideo> videoArrayList) {
        this.context = context;
        this.modelVideo = videoArrayList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

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

        String uid = modelVideo.get(i).getUid();
        String pTitle = modelVideo.get(i).getpTitle();
        String pTimeStamps = modelVideo.get(i).getpTime();
        String videoUrl = modelVideo.get(i).getVideoUrl();
        String uEmail = modelVideo.get(i).getuEmail();
        String uName = modelVideo.get(i).getuName();
        String uDp = modelVideo.get(i).getuDp();
        String pId = modelVideo.get(i).getpId();
        String pLikes = modelVideo.get(i).getpLikes(); //total likes
        String pComments = modelVideo.get(i).getpComments(); //total comment

        //convert timestamp
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(pTimeStamps));
        String formattedDateTime = DateFormat.format("dd/MM/yyyy K:mm a", calendar).toString();

        //firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //set data
        holder.titleTv.setText(pTitle);
        holder.timeTv.setText(formattedDateTime);
        setVideoUrl(modelVideo.get(i), holder);

        //set data
        holder.nameTv.setText(uName);
        holder.titleTv.setText(pTitle);
        setVideoUrl(modelVideo.get(i), holder);

        //uploadedBy
        if (uName.equals("")){
            try {
                holder.uploadedBy.setText("This video is uploaded by "+ uEmail);

            }
            catch (Exception e) {
            }
        }
        else {
            holder.uploadedBy.setText("This video is uploaded by "+ uName);
        }

        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_img).into(holder.uPictureIv);
        }
        catch (Exception e) {
            Picasso.get().load(R.drawable.ic_default_img).into(holder.uPictureIv);
        }
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
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void setVideoUrl(ModelVideo modelVideo, HolderVideo holder) {
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
        return modelVideo.size(); // return size of list
    }

    //View holder class
    class HolderVideo extends RecyclerView.ViewHolder {

        //ui view row video xml
        VideoView videoView;
        TextView titleTv, timeTv, nameTv, uploadedBy;
        ImageView uPictureIv;;
        ProgressBar progressBar;

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

        }
    }
}
