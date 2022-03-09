package com.raywenderlich.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.raywenderlich.myapplication.R;
import com.raywenderlich.myapplication.model.Person;

import java.util.List;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.ViewHolder> {

    private List<Person> dataSet;
    private Context context;


    public PersonAdapter(List<Person> dataSet, Context context) {
        this.dataSet = dataSet;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.person_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Person item = dataSet.get(position);
        holder.titleView.setText(item.name + "  " + item.age);
    }

    @Override
    public int getItemCount() {
        if (dataSet != null)
            return dataSet.size();
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;

        public ViewHolder(final View itemView) {
            super(itemView);
            titleView = (TextView) itemView.findViewById(R.id.personinfo);
        }
    }


}