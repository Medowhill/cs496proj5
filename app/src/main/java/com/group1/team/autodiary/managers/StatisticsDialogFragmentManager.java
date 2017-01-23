package com.group1.team.autodiary.managers;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;

import com.group1.team.autodiary.Fragments.StatisticsFragment;
import com.group1.team.autodiary.R;

/**
 * Created by q on 2017-01-23.
 */

public class StatisticsDialogFragmentManager {
    public static final int CALL_LOG_DIALOG = 0;
    public static final int ASSET_INFO_DIALOG = 1;
    public static final int APP_USAGE_DIALOG = 2;
    public static final int VISITED_PLACE_DIALOG = 3;
    public static final int PLAYED_MUSIC_DIALOG = 4;

    private Context mContext;

    public StatisticsDialogFragmentManager(Context context) {
        mContext = context;
    }

    public void generateStatisticsDialog(int type, Object history) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AppTheme));
        builder.setView(R.layout.fragment_statistics_dialog);
        switch(type) {
            case CALL_LOG_DIALOG :
                break;
            case ASSET_INFO_DIALOG :
                break;
            case APP_USAGE_DIALOG :
                break;
            case VISITED_PLACE_DIALOG :
                break;
            case PLAYED_MUSIC_DIALOG :
                break;
            default : return;
        }
        builder.setTitle("오늘의 통화 내역");

        builder.create().show();
    }
}
