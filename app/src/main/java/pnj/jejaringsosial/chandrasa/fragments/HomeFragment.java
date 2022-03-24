package pnj.jejaringsosial.chandrasa.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pnj.jejaringsosial.chandrasa.AddPostActivity;
import pnj.jejaringsosial.chandrasa.AddVideoActivity;
import pnj.jejaringsosial.chandrasa.R;
import pnj.jejaringsosial.chandrasa.TimelineActivity;

public class HomeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home,container,false);

        CardView cvUser = view.findViewById(R.id.cvUser); // creating a CardView and assigning a value.
        CardView cvMarket = view.findViewById(R.id.cvMarket); // creating a CardView and assigning a value.
        CardView cvInsurance = view.findViewById(R.id.cvInsurance); // creating a CardView and assigning a value.
        CardView cvTimeline = view.findViewById(R.id.cvTimeline); // creating a CardView and assigning a value.

        cvUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //actionBar.setTitle("Users");
                UsersFragment nextFrag= new UsersFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, nextFrag)
                        .addToBackStack(null)
                        .commit();

                //Toast.makeText(getActivity(), "You have clicked Surety Bond!", Toast.LENGTH_SHORT).show();

                //actionBar.setTitle("Users");
            }
        });

        cvMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //actionBar.setTitle("Users");
                MarketFragment nextFrag= new MarketFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, nextFrag)
                        .addToBackStack(null)
                        .commit();

                //Toast.makeText(getActivity(), "You have clicked Surety Bond!", Toast.LENGTH_SHORT).show();

                //actionBar.setTitle("Users");
            }
        });

        cvInsurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //actionBar.setTitle("Users");
                InsuranceFragment nextFrag= new InsuranceFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, nextFrag)
                        .addToBackStack(null)
                        .commit();

                //Toast.makeText(getActivity(), "You have clicked Surety Bond!", Toast.LENGTH_SHORT).show();

                //actionBar.setTitle("Users");
            }
        });

        cvTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //actionBar.setTitle("Users");
                TimelineFragment nextFrag= new TimelineFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content, nextFrag)
                        .addToBackStack(null)
                        .commit();

                //Toast.makeText(getActivity(), "You have clicked Surety Bond!", Toast.LENGTH_SHORT).show();

                //actionBar.setTitle("Users");
            }
        });

        return view;
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //signed user stay here
        }
        else {
            startActivity(new Intent(getActivity(), TimelineActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //show menu option
        super.onCreate(savedInstanceState);
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
        menu.findItem(R.id.aboutApp).setVisible(true);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

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

        if (id == R.id.action_add_post) {

            //show dialog
            AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
            builder.setItems(new String[]{"Add Photo"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which==0) {
                        //Add Photo Dialog clicked
                        startActivity(new Intent(getActivity(), AddPostActivity.class));

                    }
                    if (which==1) {
                        //Add Video Dialog clicked
                        startActivity(new Intent(getActivity(), AddVideoActivity.class));
                    }

                }
            });
            builder.create().show();
        };

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


}
