package com.example.rentifyapp_kotlin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    public interface OnCarClickListener {
        void onCarClick(Car car);
    }

    private final List<Car> carList;
    private final OnCarClickListener listener;

    public CarAdapter(List<Car> carList, OnCarClickListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);
        holder.bind(car, listener);
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView tvCarTitle, tvCarPrice, tvCarPurpose, tvCarLocation;

        CarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCarTitle    = itemView.findViewById(R.id.tv_car_title);
            tvCarPrice    = itemView.findViewById(R.id.tv_car_price);
            tvCarPurpose  = itemView.findViewById(R.id.tv_car_purpose);
            tvCarLocation = itemView.findViewById(R.id.tv_car_location);
        }

        void bind(Car car, OnCarClickListener listener) {
            tvCarTitle.setText(car.getTitle());
            tvCarPrice.setText(car.getPriceLabel());
            tvCarPurpose.setText("rent".equals(car.getPurpose()) ? "For Rent" : "For Sale");
            tvCarLocation.setText(car.getLocation() != null && !car.getLocation().isEmpty()
                    ? car.getLocation() : "Location N/A");

            itemView.setOnClickListener(v -> listener.onCarClick(car));
        }
    }
}