package com.q4tech.googlefittest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by alex.perez on 05/07/2016.
 */
public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.DataPointVH> {
    List<DataPoint> dataSet;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private DataPointClickListener listener;

    public ActivityAdapter(DataPointClickListener activity, List<DataPoint> dataSet) {
        this.dataSet = dataSet;
        this.listener = activity;
    }

    @Override
    public DataPointVH onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_view, parent, false);
        DataPointVH vh = new DataPointVH(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(DataPointVH holder, int position) {
        DataPoint dataPoint = dataSet.get(position);
        for (Field field : dataPoint.getDataType().getFields()) {
            holder.date.setText("Fecha: " + dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS)));
            holder.value.setText("Pasos: " + dataPoint.getValue(field));
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    protected class DataPointVH extends RecyclerView.ViewHolder {
        TextView date;
        TextView value;

        public DataPointVH(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.item_date);
            value = (TextView) itemView.findViewById(R.id.item_value);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DataPoint dp = dataSet.get(getAdapterPosition());
                    listener.openDayTasks(dp, dp.getDataType().getFields().get(0));
                }
            });
        }
    }

    public interface DataPointClickListener {
        void openDayTasks(DataPoint dp, Field field);
    }
}
