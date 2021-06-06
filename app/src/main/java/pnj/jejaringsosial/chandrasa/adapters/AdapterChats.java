package pnj.jejaringsosial.chandrasa.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import pnj.jejaringsosial.chandrasa.ChatActivity;
import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.models.ModelUser;

public class AdapterChats extends RecyclerView.Adapter<AdapterChats.MyHolder> {

    Context context;
    List<ModelUser> userList; //get user info
    private HashMap<String, String> lastMessageMap;

    //constructor

    public AdapterChats(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @NotNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int i) {
        //inflate layout  row chatlist xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_chats, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyHolder myHolder, int i) {
        //get data
        String hisUid = userList.get(i).getUid();
        String hisDp = userList.get(i).getImage();
        String hisName = userList.get(i).getName();
        String hisEmail = userList.get(i).getEmail();
        String lastMessage = lastMessageMap.get(hisUid);

        //set data
        if (hisName.equals("")){
            try {
                myHolder.emailTv.setText(hisEmail);
                myHolder.emailTv.setVisibility(View.VISIBLE);
                myHolder.nameTv.setVisibility(View.GONE);
            }
            catch (Exception e) {
            }
        }
        else {
            myHolder.nameTv.setText(hisName);
            myHolder.nameTv.setVisibility(View.VISIBLE);
            myHolder.emailTv.setVisibility(View.GONE);
        }

        if (lastMessage==null || lastMessage.equals("default")){
            myHolder.lastMessageTv.setVisibility(View.GONE);
        }
        else{
            myHolder.lastMessageTv.setVisibility(View.VISIBLE);
            myHolder.lastMessageTv.setText(lastMessage);
        }
        try{
            Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img).into(myHolder.profileIv);

        }
        catch (Exception e){

        }
        //online status
        if (userList.get(i).getOnlineStatus().equals("online")){
            //online
            myHolder.onlineStatusIv.setImageResource(R.drawable.circle_online);
            myHolder.onlineStatusIv.setVisibility(View.VISIBLE);
        }
        else {
            //offline
            myHolder.onlineStatusIv.setVisibility(View.GONE);
        }

        //handle click user in chatlist
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start chat activity
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid", hisUid);
                context.startActivity(intent);
            }
        });
    }

    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return userList.size(); //size of list user
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //views row chatlist xml
        ImageView profileIv, onlineStatusIv;
        TextView nameTv, lastMessageTv, emailTv;

        public MyHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
            emailTv = itemView.findViewById(R.id.emailTv);
        }
    }
}
