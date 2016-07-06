package com.q4tech.googlefittest.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.q4tech.googlefittest.adapters.ActivityAdapter;
import com.q4tech.googlefittest.adapters.DividerItemDecoration;
import com.q4tech.googlefittest.R;
import com.q4tech.googlefittest.dialogs.TasksDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements ActivityAdapter.DataPointClickListener {

    private final static String RESULTS = "RESULTS";

    private GoogleApiClient mClient = null;
    private static String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private ArrayList<DataPoint> readResult;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private DateFormat dateTextFormat = new SimpleDateFormat("d 'de' MMMM");
    private TextView todayText;
    private TextView todaySteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
//        initializeLogging();

        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        if (!checkPermissions()) {
            requestPermissions();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.days_list);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        todayText = (TextView) findViewById(R.id.today_text);
        todaySteps = (TextView) findViewById(R.id.today_steps);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));
        if (savedInstanceState != null) {
            readResult = savedInstanceState.getParcelableArrayList(RESULTS);
            setView(readResult);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This ensures that if the user denies the permissions then uses Settings to re-enable
        // them, the app will start working.
        buildFitnessClient();
    }

    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or having
     * multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        if (mClient == null && checkPermissions()) {
            mClient = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.HISTORY_API)
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i(TAG, "Connected!!!");
                                    // Now you can make calls to the Fitness APIs.
                                    if (readResult == null) {
                                        new AskForStepsData().execute();
                                    }
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.i(TAG,
                                                "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.i(TAG, "Google Play services connection failed. Cause: " +
                                    result.toString());
                            Snackbar.make(
                                    MainActivity.this.findViewById(R.id.main_activity_view),
                                    "Exception while connecting to Google Play services: " +
                                            result.getErrorMessage(),
                                    Snackbar.LENGTH_INDEFINITE).show();
                        }
                    })
                    .build();
        }
    }

    private boolean checkPermissions() {
        return true;
    }

    private void requestPermissions() {

    }

    private DataReadResult requestWeekData() {
        // Setting a start and end dayOfTheWeek using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startTime = cal.getTimeInMillis();

        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName("com.google.android.gms")
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .build();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
//                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(dataSource, DataType.AGGREGATE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.

        return Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
    }

    private DailyTotalResult getDailyTotal() {
        return Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_STEP_COUNT_DELTA).await(1, TimeUnit.MINUTES);
    }

    private void dumpDataSet(DataSet dataSet) {
//        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    private class AskForDailyTotal extends AsyncTask<Void, Void, DailyTotalResult> {

        @Override
        protected DailyTotalResult doInBackground(Void... voids) {
            return getDailyTotal();
        }

        @Override
        protected void onPostExecute(DailyTotalResult dailyTotalResult) {
            DataPoint dp = dailyTotalResult.getTotal().getDataPoints().get(0);
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
            }
        }
    }

    @Override
    public void openDayTasks(DataPoint dp, Field field) {
//        Intent intent = new Intent(this, TasksActivity.class);
//        startActivity(intent);
        TasksDialog dialog = new TasksDialog();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        dialog.show(transaction, TAG);
    }

    private class AskForStepsData extends AsyncTask<Void, Void, DataReadResult> {
        @Override
        protected DataReadResult doInBackground(Void... voids) {
            return requestWeekData();
        }

        @Override
        protected void onPostExecute(DataReadResult dataReadResult) {
//            new AskForDailyTotal().execute();

            //Used for aggregated data
            if (dataReadResult.getBuckets().size() > 0) {
                Log.i("History", "Number of buckets: " + dataReadResult.getBuckets().size());
                List<DataPoint> dataPoints = new ArrayList<>();
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        dumpDataSet(dataSet);
                        if (dataSet.getDataPoints().size() > 0) {
                            dataPoints.addAll(0, dataSet.getDataPoints());
                        }
                    }
                }
                setView(dataPoints);
                readResult = (ArrayList<DataPoint>) dataPoints;
            }
        }
    }

    private void setView(List<DataPoint> dataPoints) {
        if (dataPoints.size() > 0) {
            ActivityAdapter mAdapter = new ActivityAdapter(this, dataPoints);
            mRecyclerView.setAdapter(mAdapter);
            DataPoint first = dataPoints.get(0);
            String date = getString(R.string.steps_today).replace("$DATE$", dateTextFormat.format(first.getStartTime(TimeUnit.MILLISECONDS)));
            todayText.setText(date);
            Field stepsField = first.getDataType().getFields().get(0);
            todaySteps.setText(first.getValue(stepsField).toString());
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            todaySteps.setVisibility(View.VISIBLE);
            todayText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (mProgressBar.getVisibility() == View.GONE) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    todaySteps.setVisibility(View.INVISIBLE);
                    todayText.setVisibility(View.INVISIBLE);
                    new AskForStepsData().execute();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(RESULTS, readResult);

        super.onSaveInstanceState(outState);
    }
}