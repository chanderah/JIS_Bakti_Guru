package pnj.jejaringsosial.chandrasa.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import pnj.jejaringsosial.chandrasa.ChatActivity;
import pnj.jejaringsosial.chandrasa.DashboardActivity;
import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.models.ModelAgenda;

public class AdapterAgendas extends RecyclerView.Adapter<AdapterAgendas.HolderAgendas>{

    private Context context;
    private ArrayList<ModelAgenda> agendaList;

    FirebaseAuth firebaseAuth;
    String myUid;

    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;

    final Calendar myCalendar = Calendar. getInstance () ;

    public AdapterAgendas(Context context, ArrayList<ModelAgenda> agendaList) {
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
        String cUid = model.getCreatedBy();

        //set
        holder.aTitleTv.setText(aTitle);

        holder.aDateTv.setText(aDate);

        DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(cUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            String name = ""+ds.child("name").getValue();
                            String image = ""+ds.child("image").getValue();
                            String email = ""+ds.child("email").getValue();

                            //set data
                            if (name.equals("")){
                                holder.cEmailTv.setText(email);
                                holder.cEmailTv.setVisibility(View.VISIBLE);
                                holder.cNameTv.setVisibility(View.GONE);
                            }
                            else {
                                holder.cNameTv.setText(name);
                                holder.cNameTv.setVisibility(View.VISIBLE);
                                holder.cEmailTv.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {
                    }
                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(context).inflate(R.layout.dialog_agenda_details, null);
                TextView aTitleTv = view.findViewById(R.id.aTitleTv);
                TextView aDescTv = view.findViewById(R.id.aDescTv);
                TextView aDateTv = view.findViewById(R.id.aDateTv);
                TextView aCreatorTv = view.findViewById(R.id.aCreatorTv);
                Button contactBtn = view.findViewById(R.id.contactBtn);
                Button joinBtn = view.findViewById(R.id.joinBtn);
                Button closeBtn = view.findViewById(R.id.closeBtn);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(view);

                AlertDialog dialog = builder.create();
                dialog.show();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Agendas");
                Query query = ref.orderByChild("aId").equalTo(aId);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()) {
                            //get data
                            String aTitle = ""+ds.child("aTitle").getValue();
                            String aDesc = ""+ds.child("aDesc").getValue();
                            String aDate = ""+ds.child("aDate").getValue();
                            String createdBy = ""+ds.child("createdBy").getValue();

                            //set data
                            aTitleTv.setText(aTitle);
                            aDescTv.setText(aDesc);
                            aDateTv.setText(aDate);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                    }
                });

                DatabaseReference reference  = FirebaseDatabase.getInstance().getReference("Users");
                reference.orderByChild("uid").equalTo(cUid)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                    String name = ""+ds.child("name").getValue();
                                    String image = ""+ds.child("image").getValue();
                                    String email = ""+ds.child("email").getValue();

                                    //set data
                                    if (name.equals("")){
                                        aCreatorTv.setText("("+email+")");
                                    }
                                    else {
                                        aCreatorTv.setText("("+name+")");
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {
                            }
                        });

                contactBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid", cUid);
                        context.startActivity(intent);
                    }
                });

                joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, DashboardActivity.class);
                        context.startActivity(intent);
                    }
                });

                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        if (cUid.equals(myUid)){
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //show confirm dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this agenda?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Query fquery = FirebaseDatabase.getInstance().getReference("Agendas").orderByChild("aId").equalTo(aId);
                            fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                        ds.getRef().removeValue(); //remove values matched pid

                                    }
                                    //deleted
                                    Toast.makeText(context, "Deleted successfully...", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //cancel
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();

                    return false;
                }
            });
        }

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
