package pnj.jejaringsosial.chandrasa.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import pnj.jejaringsosial.chandrasa.GroupChatActivity;
import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.models.ModelGroupChatList;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList> {

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @NotNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchatlist, parent, false);
        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HolderGroupChatList holder, int position) {
        //get data
        ModelGroupChatList model = groupChatLists.get(position);
        final String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        //load last message
        loadLastMessage(model, holder);

        //set data
        holder.groupTitleTv.setText(groupTitle);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary).into(holder.groupIconIv);

        }
        catch (Exception e){
            holder.groupIconIv.setImageResource(R.drawable.ic_group_primary);
        }
        //handle group click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open groupchat
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);

            }
        });

    }

    private void loadLastMessage(ModelGroupChatList model, HolderGroupChatList holder) {
        //get last msg from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1) //get last item(msg) from that child
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            //get data
                            String message = ""+ds.child("message").getValue();
                            String timestamp = ""+ds.child("timestamp").getValue();
                            String sender = ""+ds.child("sender").getValue();
                            String messageType = ""+ds.child("type").getValue();

                            //convert timestamp
                            //convert timestamp to dd/mm/yy hh:mm am/pm
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yy hh:mm aa", cal).toString();

                            holder.timeTv.setText(dateTime);
                            if (messageType.equals("image")){
                                holder.messageTv.setText("sent a photo");
                            }
                            else {
                                holder.messageTv.setText(message);
                            }

                            //get info of sender last msg
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds: dataSnapshot.getChildren()){
                                                String name = ""+ds.child("name").getValue();
                                                String hisEmail = ""+ds.child("email").getValue();

                                                if (name.equals("")){
                                                    try {
                                                        holder.emailTv.setText(hisEmail);
                                                        holder.emailTv.setVisibility(View.VISIBLE);
                                                        holder.nameTv.setVisibility(View.GONE);
                                                        holder.messageTv.setVisibility(View.GONE);

                                                        holder.emailMessageTv.setText(message);
                                                        holder.emailMessageTv.setVisibility(View.VISIBLE);
                                                    }
                                                    catch (Exception e) {
                                                    }
                                                }
                                                else {
                                                    holder.emailTv.setVisibility(View.GONE);
                                                    holder.nameTv.setText(name+":");
                                                    holder.emailMessageTv.setVisibility(View.GONE);
                                                    holder.nameTv.setVisibility(View.VISIBLE);
                                                }

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                                        }
                                    });


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    //viewholder class
    class HolderGroupChatList extends RecyclerView.ViewHolder{

        //ui views
        private ImageView groupIconIv;
        private TextView groupTitleTv, nameTv, messageTv, timeTv, emailTv, emailMessageTv;

        public HolderGroupChatList(@NonNull @NotNull View itemView) {
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            emailMessageTv = itemView.findViewById(R.id.emailMessageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
