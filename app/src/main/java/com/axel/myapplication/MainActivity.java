package com.axel.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.axel.myapplication.Model.Location;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends BaseActivity {
    private ProgressBar progressBarTo, progressBarFrom, progressBarClass;
    private Button seachFlightBtn;
    private Spinner fromSp, toSp, classSp;
    private DatabaseReference myRef;
    private List<Location> locationList;
    private List<String> locationNames;

    private int adultPassenger = 1;
    private int childPassenger = 1;
    private TextView plusAdultBtn, minusAdultBtn, AdultTxt;
    private TextView minusChildBtn, plusChildBtn, childTxt;
    private TextView departureDateTxt;
    private TextView returnDateTxt;

    // Store departure date
    private String departureDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUi();
        fetchLocationData();
        initPassengers();
        initClassSeat();
        initDates();

        // Set default dates
        setDefaultDates();
        setupVariable();
    }

    private void setupVariable() {
        seachFlightBtn.setOnClickListener(v -> {
            // Collect data to be sent to SearchActivity
            String fromLocation = fromSp.getSelectedItem().toString();
            String toLocation = toSp.getSelectedItem().toString();
            String classType = classSp.getSelectedItem().toString();
            String departure = departureDateTxt.getText().toString();
            String returnDate = returnDateTxt.getText().toString();

            // Create an Intent to start SearchActivity
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);

            // Put the collected data into the Intent
            intent.putExtra("from", fromLocation);
            intent.putExtra("to", toLocation);
            intent.putExtra("CLASS_TYPE", classType);
            intent.putExtra("numPassenger", adultPassenger+childPassenger);
            intent.putExtra("date", departure);
            intent.putExtra("RETURN_DATE", returnDate);

            // Start the SearchActivity
            startActivity(intent);
        });

    }

    private void setDefaultDates() {
        // Get today's date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", getResources().getConfiguration().locale);

        String todayDate = sdf.format(calendar.getTime());
        departureDateTxt.setText(todayDate); // Set today's date as departure date
        departureDate = todayDate; // Store today's date as departure date

        // Set return date to the next day
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Add one day
        String tomorrowDate = sdf.format(calendar.getTime());
        returnDateTxt.setText(tomorrowDate); // Set return date to the next day
    }

    private void initDates() {
        departureDateTxt.setOnClickListener(v -> showDatePickerDialog(departureDateTxt, true));
        returnDateTxt.setOnClickListener(v -> showDatePickerDialog(returnDateTxt, false));
    }

    private void showDatePickerDialog(TextView dateTextView, boolean isDepartureDate) {
        final Calendar calendar = Calendar.getInstance();

        // Set the default date based on the text view's current text
        if (dateTextView.getId() == R.id.departureDateTxt) {
            String[] parts = departureDate.split(" ");
            int day = Integer.parseInt(parts[0]);
            int month = getMonthNumber(parts[1]);
            int year = Integer.parseInt(parts[2]);
            calendar.set(year, month, day);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Create a calendar instance and set the selected date
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);

                    // Format the selected date using SimpleDateFormat
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", getResources().getConfiguration().locale);
                    String formattedDate = sdf.format(selectedDate.getTime());
                    dateTextView.setText(formattedDate);

                    if (isDepartureDate) {
                        departureDate = formattedDate; // Store the departure date
                        // Automatically set the return date to the next day
                        selectedDate.add(Calendar.DAY_OF_MONTH, 1);
                        String returnDateFormatted = sdf.format(selectedDate.getTime());
                        returnDateTxt.setText(returnDateFormatted); // Update return date
                    } else {
                        validateReturnDate(selectedYear, selectedMonth, selectedDay);
                    }
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void validateReturnDate(int selectedYear, int selectedMonth, int selectedDay) {
        // Parse the stored departure date
        if (departureDate != null) {
            String[] parts = departureDate.split(" ");
            int depDay = Integer.parseInt(parts[0]);
            int depMonth = getMonthNumber(parts[1]); // Get month number from name
            int depYear = Integer.parseInt(parts[2]);

            Calendar departureCalendar = Calendar.getInstance();
            departureCalendar.set(depYear, depMonth, depDay);

            Calendar returnCalendar = Calendar.getInstance();
            returnCalendar.set(selectedYear, selectedMonth, selectedDay);

            // Compare the dates
            if (returnCalendar.before(departureCalendar)) {
                Toast.makeText(this, "Return date cannot be before departure date", Toast.LENGTH_SHORT).show();
                returnDateTxt.setText("-"); // Reset return date if it's invalid
            }
        }
    }

    private void initClassSeat() {
        String[] classOptions = {"Business Class", "First Class", "Economy Class"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        classSp.setAdapter(adapter);
        progressBarClass.setVisibility(View.GONE);

        classSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedClass = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void initPassengers() {
        AdultTxt.setText(adultPassenger + " Adult");
        childTxt.setText(childPassenger + " Child");

        plusAdultBtn.setOnClickListener(v -> {
            adultPassenger++;
            AdultTxt.setText(adultPassenger + " Adult");
        });

        minusAdultBtn.setOnClickListener(v -> {
            if (adultPassenger > 1) {
                adultPassenger--;
                AdultTxt.setText(adultPassenger + " Adult");
            }
        });

        plusChildBtn.setOnClickListener(v -> {
            childPassenger++;
            childTxt.setText(childPassenger + " Child");
        });

        minusChildBtn.setOnClickListener(v -> {
            if (childPassenger > 0) {
                childPassenger--;
                childTxt.setText(childPassenger + " Child");
            }
        });
    }

    private void setupUi() {
        progressBarTo = findViewById(R.id.progressBarTo);
        progressBarFrom = findViewById(R.id.progressBarFrom);
        progressBarClass = findViewById(R.id.progressBarClass);
        seachFlightBtn = findViewById(R.id.searchFlightsBtn);
        fromSp = findViewById(R.id.formSp);
        toSp = findViewById(R.id.toSp);
        classSp = findViewById(R.id.classSp);

        plusAdultBtn = findViewById(R.id.plusAdultBtn);
        minusAdultBtn = findViewById(R.id.minusAdultBtn);
        AdultTxt = findViewById(R.id.adultTxt);

        plusChildBtn = findViewById(R.id.plusChildBtn);
        minusChildBtn = findViewById(R.id.minusChildBtn);
        childTxt = findViewById(R.id.childTxt);

        departureDateTxt = findViewById(R.id.departureDateTxt);
        returnDateTxt = findViewById(R.id.returnDateTxt);
        seachFlightBtn.setEnabled(false);  // Disable the button initially

    }

    private void fetchLocationData() {
        myRef = FirebaseDatabase.getInstance().getReference("Locations");

        showLoading(true);
        locationList = new ArrayList<>();
        locationNames = new ArrayList<>();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locationList.clear();
                locationNames.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Location location = snapshot.getValue(Location.class);
                    if (location != null) {
                        locationList.add(location);
                        locationNames.add(location.getName());
                    }
                }

                runOnUiThread(() -> updateSpinners(locationNames));
                showLoading(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error fetching data", databaseError.toException());
                showLoading(false);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateSpinners(List<String> locationNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locationNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        fromSp.setAdapter(adapter);
        toSp.setAdapter(adapter);

        // Set default selections
        if (locationNames.size() > 2) { // Ensure there are enough items
            fromSp.setSelection(1); // Set default 'from' location to index 2
            toSp.setSelection(0);   // Set default 'to' location to index 1
        }

        // Enable the search button once the spinners are populated
        seachFlightBtn.setEnabled(true);
    }



    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBarTo.setVisibility(View.VISIBLE);
            progressBarFrom.setVisibility(View.VISIBLE);
        } else {
            progressBarTo.setVisibility(View.GONE);
            progressBarFrom.setVisibility(View.GONE);
        }
    }

    // Helper method to get month number from month name
    private int getMonthNumber(String monthName) {
        switch (monthName) {
            case "Jan": return 0;
            case "Feb": return 1;
            case "Mar": return 2;
            case "Apr": return 3;
            case "May": return 4;
            case "Jun": return 5;
            case "Jul": return 6;
            case "Aug": return 7;
            case "Sep": return 8;
            case "Oct": return 9;
            case "Nov": return 10;
            case "Dec": return 11;
            default: return -1; // Invalid month
        }
    }
}
