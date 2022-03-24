package pnj.jejaringsosial.chandrasa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AddAgendaActivity extends AppCompatActivity {

    //rv
    private FirebaseAuth firebaseAuth;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private Button dateTimeBtn, dateTimeMillisBtn;

    EditText aTitleEt;
    EditText aDescEt;

    ProgressDialog progressDialog;

    FloatingActionButton uploadBtn;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_agenda);

        firebaseAuth = FirebaseAuth.getInstance();

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Agenda");

        //enable back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        aTitleEt = findViewById(R.id.aTitleEt);
        aDescEt = findViewById(R.id.aDescEt);
        dateTimeBtn = findViewById(R.id.dateTimePickerBtn);
        uploadBtn = findViewById(R.id.aAgendaBtn);


        Date currentTime = Calendar.getInstance().getTime();
        String currentDateTime = DateFormat.format("HH:mm, MMM d yyyy", currentTime).toString();

        dateTimeBtn.setText(currentDateTime);

        dateTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddingAgenda(date.getTimeInMillis());
            }
        });
    }

    Calendar date;
    public void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        date = Calendar.getInstance();
        new DatePickerDialog(AddAgendaActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(AddAgendaActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        date.set(Calendar.MINUTE, minute);

                        String dateTime = DateFormat.format("HH:mm, MMM d yyyy", date).toString();
                        long millis = date.getTimeInMillis();

                        dateTimeBtn.setText(dateTime);
                    }
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void startAddingAgenda(long millis) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding Agenda...");

        //input title, description
        String aTimestamp = ""+ System.currentTimeMillis();
        String aTitle = aTitleEt.getText().toString().trim();
        String aDesc = aDescEt.getText().toString().trim();
        String aDate = dateTimeBtn.getText().toString().trim();

        //validation
        if (TextUtils.isEmpty(aTitle)){
            Toast.makeText(this, "Please enter the agenda title...", Toast.LENGTH_SHORT).show();
            return; //dont proceed
        }
        if (TextUtils.isEmpty(aDesc)){
            Toast.makeText(this, "Please enter the agenda description...", Toast.LENGTH_SHORT).show();
            return; //dont proceed
        }
        progressDialog.show();

        //setup agenda info
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("aId", ""+aTimestamp);
        hashMap.put("aTitle", ""+aTitle);
        hashMap.put("aDesc", ""+aDesc);
        hashMap.put("aDate", ""+aDate);
        hashMap.put("aDateMillis", ""+millis);
        hashMap.put("aParticipants", "0");
        hashMap.put("timestamp", ""+aTimestamp);
        hashMap.put("createdBy", ""+firebaseAuth.getUid());

        //create
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Agendas");
        ref.child(aTimestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(AddAgendaActivity.this, "Agenda Added!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //failed
                        progressDialog.dismiss();
                        Toast.makeText(AddAgendaActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //signed user stay here
        }
        else {
            startActivity(new Intent(AddAgendaActivity.this, TimelineActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous activity
        return super.onSupportNavigateUp();
    }
}