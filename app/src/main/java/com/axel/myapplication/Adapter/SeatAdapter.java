package com.axel.myapplication.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axel.myapplication.Model.Seat;
import com.axel.myapplication.R;
import com.axel.myapplication.databinding.SeatItemBinding;

import java.util.ArrayList;
import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewholder> {

    private final List<Seat> seatList;
    private final Context context;
    private ArrayList<String> selectedSeatName = new ArrayList<>();  // Initialize the ArrayList here
    private final SelectedSeat selectedSeat;

    public SeatAdapter(List<Seat> seatList, Context context, SelectedSeat selectedSeat) {
        this.seatList = seatList;
        this.context = context;
        this.selectedSeat = selectedSeat;
        this.selectedSeatName = new ArrayList<>(); // Initialize the list in the constructor as well (just in case)
    }

    @NonNull
    @Override
    public SeatAdapter.SeatViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SeatItemBinding binding = SeatItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SeatViewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatAdapter.SeatViewholder holder, @SuppressLint("RecyclerView") int position) {
        Seat seat = seatList.get(position);
        holder.binding.SeatImageView.setText(seat.getName());

        switch (seat.getStatus()) {
            case AVAILABLE:
                holder.binding.SeatImageView.setBackgroundResource(R.drawable.ic_seat_available);
                holder.binding.SeatImageView.setTextColor(context.getResources().getColor(R.color.white));
                break;
            case SELECTED:
                holder.binding.SeatImageView.setBackgroundResource(R.drawable.ic_seat_selected);
                holder.binding.SeatImageView.setTextColor(context.getResources().getColor(R.color.black));
                break;
            case UNAVAILABLE:
                holder.binding.SeatImageView.setBackgroundResource(R.drawable.ic_seat_unavailable);
                holder.binding.SeatImageView.setTextColor(context.getResources().getColor(R.color.grey));
                break;
            case EMPTY:
                holder.binding.SeatImageView.setBackgroundResource(R.drawable.ic_seat_empty);
                holder.binding.SeatImageView.setTextColor(Color.parseColor("#00000000"));
                break;
        }

        holder.binding.SeatImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (seat.getStatus() == Seat.SeatStatus.AVAILABLE) {
                    seat.setStatus(Seat.SeatStatus.SELECTED);
                    selectedSeatName.add(seat.getName());  // Add selected seat
                    notifyItemChanged(position);
                } else if (seat.getStatus() == Seat.SeatStatus.SELECTED) {
                    seat.setStatus(Seat.SeatStatus.AVAILABLE);
                    selectedSeatName.remove(seat.getName());  // Remove unselected seat
                    notifyItemChanged(position);
                }

                String selected = selectedSeatName.toString()
                        .replace("[", "")
                        .replace("]", "")
                        .replace(" ", "");
                selectedSeat.Return(selected, selectedSeatName.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return seatList.size();
    }

    public class SeatViewholder extends RecyclerView.ViewHolder {
        SeatItemBinding binding;

        public SeatViewholder(@NonNull SeatItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface SelectedSeat {
        Void Return(String selectedName, int num);
    }
}
