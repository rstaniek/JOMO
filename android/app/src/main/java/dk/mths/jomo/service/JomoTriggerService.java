package dk.mths.jomo.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import dk.mths.jomo.R;
import dk.mths.jomo.utils.RestartReceiver;
import dk.mths.jomo.utils.StatusNotificationHelper;

public class JomoTriggerService extends Service {

    private UsageStatsService usageStatsService;
    private String CHANNEL_ID = "NOTIFICATION_CHANNEL";

    @Override
    public void onCreate() {
        usageStatsService = new UsageStatsService(getApplicationContext());
        createNotificationChannel();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        StatusNotificationHelper notificationHelper = new StatusNotificationHelper();
        DaltonizerService service = new DaltonizerService(getContentResolver());

        new Thread(
                new Runnable(){
                    @Override
                    public void run(){
                        while(true){
                           // if(usageStatsService.getUsageStatsForPackage(System.currentTimeMillis()-1000*60,System.currentTimeMillis(), getForegroundApp()).size() > 3){
                            if(getForegroundAppName() == "Messenger"){
                                service.enable();
                                notificationHelper.updateNotification("Overuse detected, grayscaling " + getForegroundAppName(), getApplicationContext());
                            }
                            else{
                                service.disable();
                            }

                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).start();


        Notification notification = notificationHelper.makeJomoForegroundNotification("Jomo Service Running", this);

        startForeground(1001, notification);

        return START_STICKY;
    }


    public String getForegroundApp(){
        return usageStatsService.getForegroundPackage();
    }

    public String getForegroundAppName(){
        return usageStatsService.getForegroundPackagePretty();
    }

    public boolean foregroundServiceRunning(Context context){
        ActivityManager activityManager = (ActivityManager) getSystemService(context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if (JomoTriggerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String appName = getString(R.string.app_name);
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    appName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);

        // call MyReceiver which will restart this service via a worker
        Intent broadcastIntent = new Intent(this, RestartReceiver.class);
        sendBroadcast(broadcastIntent);

        super.onDestroy();
    }
}
