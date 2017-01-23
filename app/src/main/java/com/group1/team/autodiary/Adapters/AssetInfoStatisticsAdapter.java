package com.group1.team.autodiary.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.group1.team.autodiary.R;
import com.group1.team.autodiary.objects.AppUsage;
import com.group1.team.autodiary.objects.AssetInfo;

import java.util.List;

/**
 * Created by q on 2017-01-24.
 */

public class AssetInfoStatisticsAdapter extends ArrayAdapter<AssetInfo> {
    Context mContext;
    List<AssetInfo> assetInfos;
    LayoutInflater inflater;

    public AssetInfoStatisticsAdapter(Context context, int resource, List<AssetInfo> assetInfos) {
        super(context, resource);
        mContext = context;
        this.assetInfos = assetInfos;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { return assetInfos.size(); }

    @Override
    public AssetInfo getItem(int position) { return assetInfos.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.statistics_call_log_item, viewGroup, false);

        return convertView;
    }
}

