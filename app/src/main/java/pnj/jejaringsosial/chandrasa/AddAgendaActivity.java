package pnj.jejaringsosial.chandrasa;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class AddAgendaActivity extends AppCompatActivity {

    //rv
    private FirebaseAuth firebaseAuth;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private Button dateTimeBtn;

    FloatingActionButton uploadBtn;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_agenda);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Agenda");

        //enable back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

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