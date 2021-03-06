package pnj.jejaringsosial.chandrasa.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import pnj.jejaringsosial.chandrasa.ChatActivity;
import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.UserProfileActivity;
import pnj.jejaringsosial.chandrasa.models.ModelUser;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUser> userList;

    //constructor
    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        //get data
        String hisUID = userList.get(i).getUid();
        String userImage = userList.get(i).getImage();
        String userName = userList.get(i).getName();
        String userEmail = userList.get(i).getEmail();

        //set data
        myHolder.mNameTv.setText(userName);
        myHolder.mEmailTv.setText(userEmail);
        try {
            Picasso.get().load(userImage).into(myHolder.mAvatarIv);

        }
        catch (Exception e){
            myHolder.mAvatarIv.setImageResource(R.drawable.ic_face_black);
        }

        //handle item click
        myHolder.itemView.setOnClickListener(v -> {
            //show dialog
            AlertDialog.Builder builder =  new AlertDialog.Builder(context);
            builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which==0) {
                        //profile clicked
                        //click to go profile with uid, uid of clicked user
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra("uid", hisUID);
                        context.startActivity(intent);

                    }
                    if (which==1) {
                        //chat clicked
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        context.startActivity(intent);
                    }

                }
            });
            builder.create().show();
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarIv;
        TextView mNameTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNameTv = itemView.findViewById(R.id.nameTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);
        }
    }
}
