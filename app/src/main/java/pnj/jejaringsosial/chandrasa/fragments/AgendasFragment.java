package pnj.jejaringsosial.chandrasa.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import pnj.jejaringsosial.chandrasa.AddAgendaActivity;
import pnj.jejaringsosial.chandrasa.AddPostActivity;
import pnj.jejaringsosial.chandrasa.AddVideoActivity;
import pnj.jejaringsosial.chandrasa.MainActivity;
import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.adapters.AdapterAgendas;
import pnj.jejaringsosial.chandrasa.adapters.AdapterNotification;
import pnj.jejaringsosial.chandrasa.adapters.AdapterUsers;
import pnj.jejaringsosial.chandrasa.models.ModelAgenda;
import pnj.jejaringsosial.chandrasa.models.ModelNotification;
import pnj.jejaringsosial.chandrasa.models.ModelUser;


public class AgendasFragment extends Fragment {

    //rv
    RecyclerView agendasRv;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelAgenda> agendaList;
    private AdapterAgendas adapterAgendas;

    public AgendasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_agenda,container,false);
        //init rv
        agendasRv = view.findViewById(R.id.agendasRv);

        firebaseAuth = FirebaseAuth.getInstance();

        getAllAgendas();

        //swipe refresh
        SwipeRefreshLayout swiperefreshlayout;
        swiperefreshlayout = view.findViewById(R.id.swiperefreshLayout);
        swiperefreshlayout.setOnRefreshListener(() -> {
            Toast.makeText(getActivity(), "Refresh running...", Toast.LENGTH_SHORT).show();
            getAllAgendas();
            swiperefreshlayout.setRefreshing(false);
        });

        return view;
    }

    private void getAllAgendas() {
        agendaList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Agendas");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                agendaList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    ModelAgenda model = ds.getValue(ModelAgenda.class);

                    long currentTime = Calendar.getInstance().getTimeInMillis();
                    long modelTimeMillis = Long.parseLong(model.getaDateMillis());

                        if (modelTimeMillis>currentTime){
                            agendaList.add(model);
                        }
                    }

                    //adapter
                    adapterAgendas = new AdapterAgendas(getActivity(), agendaList) ;
                    //set adapter to recycler view
                    agendasRv.setAdapter(adapterAgendas);
                }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    //menu inflate
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflate menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide option
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_video).setVisible(false);
        menu.findItem(R.id.action_add_participant_group).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        menu.findItem(R.id.action_add_agenda).setVisible(true);
        menu.findItem(R.id.aboutApp).setVisible(true);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search btn
                if (!TextUtils.isEmpty(s)) {
                    //searchPosts(s);

                } else {
                    //loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    //searchPosts(s);

                } else {
                    //loadPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle menu click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }

        if (id == R.id.action_add_agenda) {
            startActivity(new Intent(getActivity(), AddAgendaActivity.class));
        }

        if (id == R.id.aboutApp) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_about_app, null);
            Button closeBtn = view.findViewById(R.id.closeBtn);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view);

            AlertDialog dialog = builder.create();
            dialog.show();

            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //show menu option

        super.onCreate(savedInstanceState);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //signed user stay here
        }
        else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}