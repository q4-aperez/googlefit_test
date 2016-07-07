package com.q4tech.googlefittest.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.q4tech.googlefittest.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by alex.perez on 05/07/2016.
 */
public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.DataPointVH> {
    List<DataPoint> dataSet;
    private DateFormat dayFormat = new SimpleDateFormat("EEEE");
    private DateFormat dateTextFormat = new SimpleDateFormat("d 'de' MMMM");
    private DataPointClickListener listener;

    private final static int BASIC_VIEW = 0;
    private final static int CHALLENGES_VIEW = 1;

    public ActivityAdapter(DataPointClickListener activity, List<DataPoint> dataSet) {
        this.dataSet = dataSet;
        this.listener = activity;
    }

    @Override
    public DataPointVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        DataPointVH vh = null;
        // create a new view
        switch (viewType) {
            case BASIC_VIEW:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.steps_daily_basic_view, parent, false);
                vh = new DataPointVH(v);
                break;
            case CHALLENGES_VIEW:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.steps_daily_challenges_view, parent, false);
                vh = new ChallengesVH(v);
                break;
        }
        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        return BASIC_VIEW;
    }

    @Override
    public void onBindViewHolder(DataPointVH holder, int position) {
        //Ignore today
        DataPoint dataPoint = dataSet.get(position + 1);
        SharedPreferences sentItems = ((Activity) listener).getPreferences(Context.MODE_PRIVATE);
        switch (getItemViewType(position)) {
            case BASIC_VIEW:
                for (Field field : dataPoint.getDataType().getFields()) {
                    holder.dayOfTheWeek.setText(getDayString(dataPoint.getStartTime(TimeUnit.MILLISECONDS)));
                    holder.steps.setText(dataPoint.getValue(field).toString());
                    holder.date.setText(dateTextFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS)));
                    if (sentItems.getBoolean(String.valueOf(dataPoint.getStartTime(TimeUnit.MILLISECONDS)), false)) {
                        holder.btn.setText(R.string.informed);
                        holder.btn.setTextColor(ContextCompat.getColor((Context) listener, R.color.material_grey_600));
                    }
                }
                break;
            case CHALLENGES_VIEW:
                //TODO analyze challenges
                break;
        }

    }

    private String getDayString(long dateInMils) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(dateInMils);
        if (yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
            if (yesterday.get(Calendar.MONTH) == date.get(Calendar.MONTH)) {
                if (yesterday.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)) {
                    return ((Context) listener).getString(R.string.yesterday);
                }
            }
        }
        String day = dayFormat.format(dateInMils);
        day = day.substring(0, 1).toUpperCase() + day.substring(1);
        return day;
    }

    @Override
    public int getItemCount() {
        //Ignore today
        if (dataSet.size() > 0) {
            return dataSet.size() - 1;
        } else {
            return 0;
        }
    }

    protected class DataPointVH extends RecyclerView.ViewHolder {
        TextView dayOfTheWeek;
        TextView steps;
        TextView date;
        TextView btn;

        public DataPointVH(View itemView) {
            super(itemView);

            dayOfTheWeek = (TextView) itemView.findViewById(R.id.day_of_the_week);
            steps = (TextView) itemView.findViewById(R.id.steps);
            date = (TextView) itemView.findViewById(R.id.date);
            btn = (TextView) itemView.findViewById(R.id.inform_button);
            if (btn != null) {
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Today offset
                        DataPoint dp = dataSet.get(getAdapterPosition()+1);
                        listener.informDailySteps(dp);
                    }
                });
            }
        }
    }

    protected class ChallengesVH extends DataPointVH {
        ImageView breakfast, lunch, merienda, dinner;

        public ChallengesVH(View itemView) {
            super(itemView);
            breakfast = (ImageView) itemView.findViewById(R.id.breakfast_icon);
            lunch = (ImageView) itemView.findViewById(R.id.lunch_icon);
            merienda = (ImageView) itemView.findViewById(R.id.merienda_icon);
            dinner = (ImageView) itemView.findViewById(R.id.dinner_icon);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Today offset
                    DataPoint dp = dataSet.get(getAdapterPosition()+1);
                    listener.openDayTasks(dp);
                }
            });
        }
    }

    public interface DataPointClickListener {

        void openDayTasks(DataPoint dp);

        void informDailySteps(DataPoint dp);
    }
}
