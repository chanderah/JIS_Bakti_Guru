package pnj.jejaringsosial.chandrasa.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Layout;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import pnj.jejaringsosial.chandrasa.PostDetailActivity;
import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.UserProfileActivity;
import pnj.jejaringsosial.chandrasa.models.ModelAgenda;
import pnj.jejaringsosial.chandrasa.models.ModelNotification;

public class AdapterAgendas extends RecyclerView.Adapter<AdapterAgendas.HolderAgendas>{

    private Context context;
    private ArrayList<ModelAgenda> agendaList;

    public AdapterAgendas(Context context, ArrayList<ModelAgenda> agendaList) {
        this.context = context;
        this.agendaList = agendaList;
    }

    @NonNull
    @NotNull
    @Override
    public HolderAgendas onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //inflate
        View view = LayoutInflater.from(context).inflate(R.layout.row_agenda,parent,false);

        return new HolderAgendas(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HolderAgendas holder, int position) {
        //get and set data to views

        //get data
        ModelAgenda model = agendaList.get(position);
        String aTitle = model.getaTitle();
        String aDesc = model.getaDesc();
        String aId = model.getaId();
        String aDate = model.getaDate();
        String cName = model.getcName();
        String cEmail = model.getcEmail();
        String createdBy = model.getCreatedBy();

        //set
        holder.aTitleTv.setText(aTitle);
        holder.aDateTv.setText(aDate);

        holder.cEmailTv.setText(cEmail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("uid", createdBy);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return agendaList.size();
    }

    class HolderAgendas extends RecyclerView.ViewHolder{

        //declare
        TextView aTitleTv, aDescTv, aDateTv, cNameTv, cEmailTv;

        public HolderAgendas(@NonNull @NotNull View itemView) {
            super(itemView);

            //init
            aTitleTv = itemView.findViewById(R.id.aTitleTv);
            aDescTv = itemView.findViewById(R.id.aDescEt);
            aDateTv = itemView.findViewById(R.id.aDateTv);
            cNameTv = itemView.findViewById(R.id.cNameTv);
            cEmailTv = itemView.findViewById(R.id.cEmailTv);
        }
    }
}
