package dk.mths.jomo.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import dk.mths.jomo.R;

public class AppsAdapter extends ArrayAdapter<App> {
    public AppsAdapter(Context context, ArrayList<App> usageStatDTOArrayList) {
        super(context, 0, usageStatDTOArrayList);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        App usageStats = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_app, parent, false);
        }

        // Lookup view for data population
        TextView app_name_tv = convertView.findViewById(R.id.app_name_tv);
        TextView app_open_count = convertView.findViewById(R.id.app_open_count);
        TextView usage_duration_tv =  convertView.findViewById(R.id.usage_duration_tv);
        TextView usage_perc_tv = convertView.findViewById(R.id.usage_perc_tv);
        ImageView icon_img =  convertView.findViewById(R.id.icon_img);
        ProgressBar progressBar = convertView.findViewById(R.id.progressBar);



        // Populate the data into the template view using the data object
        app_name_tv.setText(usageStats.appName);
        app_open_count.setText("Launch count: " + usageStats.openCount);
        usage_duration_tv.setText(usageStats.usageDuration);
        usage_perc_tv.setText(usageStats.usagePercentage + "%");
        icon_img.setImageDrawable(usageStats.appIcon);
        progressBar.setProgress(usageStats.percentageOfLongestRunning);



        // Return the completed view to render on screen
        return convertView;
    }
}