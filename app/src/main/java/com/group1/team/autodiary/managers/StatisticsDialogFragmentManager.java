package com.group1.team.autodiary.managers;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.group1.team.autodiary.Adapters.AppUsageStatisticsAdapter;
import com.group1.team.autodiary.Adapters.AssetInfoStatisticsAdapter;
import com.group1.team.autodiary.Adapters.CallLogStatisticsAdapter;
import com.group1.team.autodiary.Adapters.PlayedMusicStatisticsAdapter;
import com.group1.team.autodiary.Adapters.VisitedPlaceStatisticsAdapter;
import com.group1.team.autodiary.Fragments.StatisticsFragment;
import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.AssetInfo;
import com.group1.team.autodiary.objects.CallLog;
import com.group1.team.autodiary.objects.Music;
import com.group1.team.autodiary.objects.Place;

import java.util.List;

/**
 * Created by q on 2017-01-23.
 */

public class StatisticsDialogFragmentManager {
    private Context mContext;

    public StatisticsDialogFragmentManager(Context context) {
        mContext = context;
    }

    public void generateStatisticsDialog(int type, Object history) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.AppTheme));
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_statistics_dialog, null);
        ListView listView = (ListView) view.findViewById(R.id.statistics);

        switch(type) {
            case R.id.call_log_view :
                CallLogStatisticsAdapter callLogStatisticsAdapter = new CallLogStatisticsAdapter(mContext, R.layout.statistics_call_log_item, (List<CallLog>)history);
                listView.setAdapter(callLogStatisticsAdapter);
                builder.setTitle(mContext.getString(R.string.call_logs));
                break;
            case R.id.total_spent_money_view :
                AssetInfoStatisticsAdapter assetInfoStatisticsAdapter = new AssetInfoStatisticsAdapter(mContext, R.layout.statistics_call_log_item, (List<AssetInfo>)history);
                listView.setAdapter(assetInfoStatisticsAdapter);
                builder.setTitle(mContext.getString(R.string.asset_infos));
                break;
            case R.id.most_used_apps_view :
                AppUsageStatisticsAdapter appUsageStatisticsAdapter = new AppUsageStatisticsAdapter(mContext, R.layout.statistics_call_log_item, (List<AppUsage>)history);
                listView.setAdapter(appUsageStatisticsAdapter);
                builder.setTitle(mContext.getString(R.string.app_usages));
                break;
            case R.id.longest_visited_places_view :
                VisitedPlaceStatisticsAdapter visitedPlaceStatisticsAdapter = new VisitedPlaceStatisticsAdapter(mContext, R.layout.statistics_call_log_item, (List<Place>)history);
                listView.setAdapter(visitedPlaceStatisticsAdapter);
                builder.setTitle(mContext.getString(R.string.visited_places));
                break;
            case R.id.most_frequently_played_music_view :
                PlayedMusicStatisticsAdapter playedMusicStatisticsAdapter = new PlayedMusicStatisticsAdapter(mContext, R.layout.statistics_call_log_item, (List<Music>)history);
                listView.setAdapter(playedMusicStatisticsAdapter);
                builder.setTitle(mContext.getString(R.string.played_musics));
                break;
            default : return;
        }

        builder.setView(view);

        builder.create().show();
    }
}
