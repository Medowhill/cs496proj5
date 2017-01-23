package com.group1.team.autodiary.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.group1.team.autodiary.Fragments.DiaryFragment;
import com.group1.team.autodiary.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.Date;


public class HistoryActivity extends AppCompatActivity {

    private long mTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        if (Build.VERSION.SDK_INT >= 19)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mTime = System.currentTimeMillis();
        DiaryFragment fragment = DiaryFragment.getInstance(mTime);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.history_container, fragment);
        transaction.commit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.history_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.dialog_calendar, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setView(view);
                AlertDialog dialog = builder.show();
                MaterialCalendarView calendarView = (MaterialCalendarView) view.findViewById(R.id.history_calendarView);
                calendarView.setDateSelected(new Date(mTime), true);
                calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
                    @Override
                    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                        mTime = date.getDate().getTime();
                        fragment.load(mTime);
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.from_right, R.anim.to_left);
    }
}
