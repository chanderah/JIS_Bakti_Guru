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

import java.util.ArrayList;

import pnj.jejaringsosial.chandrasa.GroupChatActivity;
import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.models.ModelGroupChats;

public class AdapterGroupChats extends RecyclerView.Adapter<AdapterGroupChats.HolderGroupChatList> {

    private Context context;
    private ArrayList<ModelGroupChats> groupChatLists;

    public AdapterGroupChats(Context context, ArrayList<ModelGroupChats> groupChatLists) {
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
        ModelGroupChats model = groupChatLists.get(position);
        final String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

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

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    //viewholder class
    class HolderGroupChatList extends RecyclerView.ViewHolder{

        //ui views
        private ImageView groupIconIv;
        private TextView groupTitleTv, nameTv, messageTv, timeTv;

        public HolderGroupChatList(@NonNull @NotNull View itemView) {
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
