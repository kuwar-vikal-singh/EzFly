package com.axel.myapplication;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.axel.myapplication.Adapter.FlightAdapter;
import com.axel.myapplication.Model.Flight;
import com.axel.myapplication.databinding.ActivitySearchBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends BaseActivity {

    private ActivitySearchBinding binding;
    private  String from,to,date;
    private int numPassenger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        initList();
        setVariable();

    }
    private void setVariable(){
        binding.backBtn.setOnClickListener(v ->{
            onBackPressed();
        } );
    }
    private void initList(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Flights");
        ArrayList<Flight> list = new ArrayList<>();
        Query query = myRef.orderByChild("from").equalTo(from);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot issu : snapshot.getChildren()){
                        Flight flight = issu.getValue(Flight.class);
                        if (flight.getTo().equals(to)){
                            list.add(flight);
                        }

                        /* for filter by date, if you using this then comment on top code

                        if (flight.getTo().equals(to) && flight.getDate().equals(date)){
                            list.add(flight)
                        }
                         */
                        if (!list.isEmpty()){
                          binding.recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this,LinearLayoutManager.VERTICAL,false));
                          binding.recyclerView.setAdapter(new FlightAdapter(list));
                        }
                        binding.progressBar.setVisibility(View.GONE);


                    }
                }
                if (!snapshot.exists()){

                        binding.noTicketShowText.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getIntentExtra() {
        from = getIntent().getStringExtra("from");
        to = getIntent().getStringExtra("to");
        date=getIntent().getStringExtra("date");
    }
}