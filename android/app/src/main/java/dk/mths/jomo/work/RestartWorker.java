package dk.mths.jomo.work;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import dk.mths.jomo.service.JomoTriggerService;

public class RestartWorker extends Worker {
    private final Context context;
    private final String TAG = "MyWorker";
    private final JomoTriggerService myService;

    public RestartWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
        myService = new JomoTriggerService();
    }

    @NonNull
    @Override
    public Result doWork() {
        boolean serviceIsRunning = foregroundServiceRunning(this.context);
        if (!serviceIsRunning) {
            Intent intent = new Intent(this.context , JomoTriggerService.class);
            ContextCompat.startForegroundService(this.context , intent);
        }
        return Result.success();
    }

    public boolean foregroundServiceRunning(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if (JomoTriggerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }
}