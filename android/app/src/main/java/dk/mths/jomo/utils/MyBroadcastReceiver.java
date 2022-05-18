package dk.mths.jomo.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import dk.mths.jomo.service.JomoTriggerService;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent serviceIntent = new Intent(context, JomoTriggerService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}
