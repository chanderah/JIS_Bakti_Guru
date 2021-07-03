package pnj.jejaringsosial.chandrasa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;

import pnj.jejaringsosial.chandrasa.fragments.AgendasFragment;

public class AddAgendaActivity extends AppCompatActivity {

    //rv
    private FirebaseAuth firebaseAuth;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private Button dateTimeBtn;

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

        dateTimeBtn.setText(getTodaysDate());

        initDatePicker();

        dateTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.setTitle("Select Date");
                datePickerDialog.show();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddingAgenda();
            }
        });
    }

    private void startAddingAgenda() {
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


    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return makeDateString(day, month, year);
    }

    private String makeDateString(int day, int month, int year) {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month) {
        if (month == 1)
            return "JAN";
        if (month == 2)
            return "FEB";
        if (month == 3)
            return "MAR";
        if (month == 4)
            return "APR";
        if (month == 5)
            return "MAY";
        if (month == 6)
            return "JUN";
        if (month == 7)
            return "JUL";
        if (month == 8)
            return "AUG";
        if (month == 9)
            return "SEP";
        if (month == 10)
            return "OCT";
        if (month == 11)
            return "NOV";
        if (month == 12)
            return "DEC";

        //default
        return "JAN";
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(day, month, year);
                String dateTarget = makeDateString(day, month, year);

                initTimePicker(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_DARK;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private void initTimePicker(String date) {
        // Get Current Time
        final Calendar calendar = Calendar.getInstance();
        final int[] mHour = {calendar.get(Calendar.HOUR_OF_DAY)};
        final int[] mMinute = {calendar.get(Calendar.MINUTE)};

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hourString;
                        String minuteString;

                        if (hourOfDay < 10) {
                            hourString = "0" + hourOfDay;
                        }
                        else {
                            hourString = "" + hourOfDay;
                        }

                        if (minute < 10){
                            minuteString = "0" + minute;
                        }
                        else {
                            minuteString = "" + minute;
                        }
                        dateTimeBtn.setText(hourString + ":" + minuteString + ", " + date+" ");
                    }
                }, mHour[0], mMinute[0], true);
        timePickerDialog.show();
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //signed user stay here
        }
        else {
            startActivity(new Intent(AddAgendaActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //goto previous activity
        return super.onSupportNavigateUp();
    }
}