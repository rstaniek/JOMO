package dk.mths.jomo.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import dk.mths.jomo.work.RestartWorker;

public class RestartReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        WorkManager workManager = WorkManager.getInstance(context);
        OneTimeWorkRequest startServiceRequest = new OneTimeWorkRequest.Builder(RestartWorker.class)
                .build();
        workManager.enqueue(startServiceRequest);
    }
}