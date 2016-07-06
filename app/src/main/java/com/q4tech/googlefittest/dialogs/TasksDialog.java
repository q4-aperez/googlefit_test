package com.q4tech.googlefittest.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.q4tech.googlefittest.R;

/**
 * Created by alex.perez on 05/07/2016.
 */
public class TasksDialog extends DialogFragment {

    public static TasksDialog newInstance() {

        Bundle args = new Bundle();

        TasksDialog fragment = new TasksDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tasks_dialog, container, false);

        TextView dateTitle = (TextView) view.findViewById(R.id.date_title);
        TextView stepsCount = (TextView) view.findViewById(R.id.steps_count);
        ImageView breakfastCheck = (ImageView) view.findViewById(R.id.breakfast_check);
        ImageView lunchCheck = (ImageView) view.findViewById(R.id.lunch_check);
        ImageView meriendaCheck = (ImageView) view.findViewById(R.id.merienda_check);
        ImageView dinnerCheck = (ImageView) view.findViewById(R.id.dinner_check);
        view.findViewById(R.id.breakfast_container).setOnClickListener(new TaskClickListener(breakfastCheck));
        view.findViewById(R.id.lunch_container).setOnClickListener(new TaskClickListener(lunchCheck));
        view.findViewById(R.id.merienda_container).setOnClickListener(new TaskClickListener(meriendaCheck));
        view.findViewById(R.id.dinner_container).setOnClickListener(new TaskClickListener(dinnerCheck));
        view.findViewById(R.id.inform_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return view;
    }

    private class TaskClickListener implements View.OnClickListener {

        private ImageView check;

        public TaskClickListener(ImageView check) {
            this.check = check;
        }

        @Override
        public void onClick(View view) {
            if (check.getVisibility() == View.VISIBLE) {
                check.setVisibility(View.GONE);
            } else {
                check.setVisibility(View.VISIBLE);
            }
        }
    }
}
