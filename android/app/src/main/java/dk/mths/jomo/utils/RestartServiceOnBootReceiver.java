package dk.mths.jomo.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import dk.mths.jomo.service.JomoTriggerService;
import dk.mths.jomo.work.RestartWorker;

public class RestartServiceOnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            WorkManager workManager = WorkManager.getInstance(context);
            OneTimeWorkRequest startServiceRequest = new OneTimeWorkRequest.Builder(RestartWorker.class)
                    .build();
            workManager.enqueue(startServiceRequest);
        }
    }
}
