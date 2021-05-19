package pnj.jejaringsosial.chandrasa.adapters;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;

import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.models.ModelVideo;

public class AdapterVideo extends RecyclerView.Adapter<AdapterVideo.HolderVideo> {


    //context
    private Context context;
    //array list
    private ArrayList<ModelVideo> videoArrayList;

    //constructor
    public AdapterVideo(Context context, ArrayList<ModelVideo> videoArrayList) {
        this.context = context;
        this.videoArrayList = videoArrayList;
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
    public void onBindViewHolder(@NonNull @NotNull HolderVideo holder, int position) {
        //Get, format, handle click

        //get data
        ModelVideo modelVideo = videoArrayList.get(position);

        String id = modelVideo.getId();
        String title = modelVideo.getTitle();
        String timestamp = modelVideo.getTimestamp();
        String videoUrl = modelVideo.getVideoUrl();

        //format timestamp
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String formattedDateTime = DateFormat.format("dd/MM/yyyy K:mm a", calendar).toString();

        //set data
        holder.titleTv.setText(title);
        holder.timeTv.setText(formattedDateTime);
        setVideoUrl(modelVideo, holder);
    }

    private void setVideoUrl(ModelVideo modelVideo, HolderVideo holder) {
        //show progress
        holder.progressBar.setVisibility(View.VISIBLE);

        //get video url
        String videoUrl = modelVideo.getVideoUrl();

        //media controller
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(holder.videoView);

        Uri videoUri = Uri.parse(videoUrl);
        holder.videoView.setMediaController(mediaController);
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
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                        //render started
                        holder.progressBar.setVisibility(View.VISIBLE);
                        return true;
                    }
                    //buffer started
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:{
                        holder.progressBar.setVisibility(View.GONE);
                        return true;
                    }
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
        return videoArrayList.size(); // retturn size of list
    }

    //View holder class
    class HolderVideo extends RecyclerView.ViewHolder {

        //ui view row video xml
        VideoView videoView;
        TextView titleTv, timeTv;
        ProgressBar progressBar;

        public HolderVideo(@NonNull @NotNull View itemView) {
            super(itemView);

            //init ui view row video xml
            videoView = itemView.findViewById(R.id.videoView);
            titleTv = itemView.findViewById(R.id.titleTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            progressBar = itemView.findViewById(R.id.progressBar);

        }
    }
}
