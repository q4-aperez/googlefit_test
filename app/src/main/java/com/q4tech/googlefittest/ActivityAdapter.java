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
import java.util.concurrent.TimeUnit;

/**
 * Created by alex.perez on 05/07/2016.
 */
public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.DataPointVH> {
    DataSet dataSet;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public ActivityAdapter(DataSet dataSet) {
        this.dataSet = dataSet;
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
        DataPoint dataPoint = dataSet.getDataPoints().get(position);
        for (Field field : dataPoint.getDataType().getFields()) {
            holder.date.setText("Fecha: " + dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS)));
            holder.value.setText("Pasos: " + dataPoint.getValue(field));
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.getDataPoints().size();
    }

    protected class DataPointVH extends RecyclerView.ViewHolder {
        TextView date;
        TextView value;

        public DataPointVH(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.item_date);
            value = (TextView) itemView.findViewById(R.id.item_value);
        }
    }
}
