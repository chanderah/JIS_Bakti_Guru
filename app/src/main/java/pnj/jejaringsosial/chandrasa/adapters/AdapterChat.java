package pnj.jejaringsosial.chandrasa.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.models.ModelChat;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    boolean seen = false;

    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout left receiver right sender
        if (i==MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MyHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, final int i) {
        //get data
        String message = chatList.get(i).getMessage();
        String timeStamp = chatList.get(i).getTimestamp();
        String type = chatList.get(i).getType();
        boolean isSeen = chatList.get(i).isSeen();


        //convert timestamp to dd/mm/yy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("HH:mm, MMM d", cal).toString();

        if (type.equals("text")){
            myHolder.messageTv.setVisibility(View.VISIBLE);
            myHolder.messageIv.setVisibility(View.GONE);
            myHolder.messageTv.setText(message);

        }
        else {
            myHolder.messageTv.setVisibility(View.GONE);
            myHolder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(myHolder.messageIv);
        }
        

        //set data
        myHolder.messageTv.setText(message);
        myHolder.timeTv.setText(dateTime);
        //myHolder.isSeenTv.setText();
        try {
            Picasso.get().load(imageUrl).into(myHolder.profileIv);
        }
        catch (Exception e) {

        }

        //long click to show delete dialog
        myHolder.messageLAyout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context) ;
                builder.setTitle("Delete");
                builder.setMessage("Delete this message?");

                //delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteMessage(i);

                    }
                });
                //cancel delete btn
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });
                builder.create().show();

                return false;
            }
        });

        ///set seen/delivered status
        if (i==chatList.size()-1) {
            if (isSeen) {
                myHolder.isSeenTv.setText("Seen");
            }
            else {
                myHolder.isSeenTv.setText("Sent");
            }
        }
        else {
            myHolder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int position) {

        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // get timestamp clicked msg, compare with other, value must match
        // allow sender to delete his and receiver message
        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    if (ds.child("sender").getValue().equals(myUID)){
                        //remove msg /1
                        ds.getRef().removeValue();

                        //this message was deleted /2
                        /*HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was deleted...");
                        ds.getRef().updateChildren(hashMap);*/

                        Toast.makeText(context, "Message deleted...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, "You can't delete this message...", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get current user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //views
        ImageView profileIv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLAyout; //click listener delete

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            messageIv = itemView.findViewById(R.id.messageIv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLAyout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
