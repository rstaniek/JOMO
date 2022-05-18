package dk.mths.jomo.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.common.collect.ImmutableList;

import dk.mths.jomo.MainActivity;
import dk.mths.jomo.R;
import dk.mths.jomo.utils.RestartReceiver;
import dk.mths.jomo.utils.StatusNotificationHelper;

public class JomoTriggerService extends Service {

    private UsageStatsService usageStatsService;
    private String CHANNEL_ID = "NOTIFICATION_CHANNEL";
    private final int TRIGGER_THRESHOLD = 5;
    private final int TRIGGER_TIME_RANGE_IN_MINS = 5;
    private final int LOCKOUT_PERIOD_IN_MINS = 1;
    private static final ImmutableList<String> BAD_APPS =
            ImmutableList.of(
                    "Facebook", "com.facebook.katana",
                    "Instagram", "com.instagram.android",
                    "YouTube", "com.google.android.youtube",
                    "Messenger", "com.facebook.orca",
                    "Twitter", "com.twitter.android",
                    "LinkedIn", "com.linkedin.android",
                    "Snapchat", "com.snapchat.android",
                    "Tumblr", "com.tumblr",
                    "Pinterest", "com.pinterest",
                    "Reddit", "com.reddit.frontpage",
                    "TikTok", "com.zhiliaoapp.musically",
                    "Tinder", "com.tinder",
                    "Bumble", "com.bumble.app",
                    "happn", "com.ftw_and_co.happn",
                    "Discord", "com.discord",
                    "rif is fun", "com.andrewshu.android.reddit",
                    "Twitch", "com.twitch.android.app"
            );

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
                            String foregroundApp = getForegroundAppName();
                            //updateNotification("Foreground app: " + foregroundApp, getApplicationContext());
                            int badAppCount = usageStatsService.getEventhistoryForBadApps(
                                    System.currentTimeMillis()- (1000*60*TRIGGER_TIME_RANGE_IN_MINS),
                                    System.currentTimeMillis(),
                                    BAD_APPS).size();

                            if(BAD_APPS.contains(foregroundApp) && badAppCount > TRIGGER_THRESHOLD){
                                service.enable();
                                notificationHelper.updateNotification("Overuse detected, grayscaling for " + LOCKOUT_PERIOD_IN_MINS + " minutes ", getApplicationContext());
                                try {
                                    Thread.sleep(1000*60*LOCKOUT_PERIOD_IN_MINS);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            else{
                                Integer remainingContextSwitches = TRIGGER_THRESHOLD-badAppCount;
                                if(remainingContextSwitches < 0)
                                    remainingContextSwitches = 0;
                                updateNotification(remainingContextSwitches + " context switches remaining", getApplicationContext());
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


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("JoMo")
                .setContentText("Service Initialized")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

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

    private void updateNotification(String message, Context context) {
        NotificationManagerCompat nmc = NotificationManagerCompat.from(context);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("JoMo")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        nmc.notify(1001, notification);
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
