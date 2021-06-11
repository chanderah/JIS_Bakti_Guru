package pnj.jejaringsosial.chandrasa.adapters;

import android.content.Context;
import android.text.Layout;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
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

import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.models.ModelGroupChat;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelGroupChat> modelGroupChat;

    private FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChat) {
        this.context = context;
        this.modelGroupChat = modelGroupChat;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //inflate layout
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new HolderGroupChat(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HolderGroupChat holder, int position) {
        //get data
        ModelGroupChat model = modelGroupChat.get(position);
        String timestamp = model.getTimestamp();
        String message = model.getMessage();
        String senderUid = model.getSender();
        String messageType = model.getType();

        //convert timestamp to dd/mm/yy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("HH:mm, MMM d", cal).toString();


        holder.timeTv.setText(dateTime);
        if (messageType.equals("text")){
            //set data
            holder.messageTv.setText(message);

            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
        }
        else {
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);

            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
            }
            catch (Exception e) {
                holder.messageIv.setImageResource(R.drawable.ic_image_black);
            }
        }


        setUserName(model, holder);
    }

    private void setUserName(ModelGroupChat model, HolderGroupChat holder) {
        //get sender info from uid, model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            String name = ""+ds.child("name").getValue();

                            holder.nameTv.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroupChat.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChat.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

        private TextView nameTv, messageTv, timeTv;
        private ImageView messageIv;

        public HolderGroupChat(@NonNull @NotNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.messageIv);

        }
    }
}
