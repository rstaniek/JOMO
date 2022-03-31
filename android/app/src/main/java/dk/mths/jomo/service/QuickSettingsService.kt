package dk.mths.jomo.service

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class QuickSettingsService : TileService() {

    override fun onClick() {
        super.onClick()


    }

    override fun onStartListening() {
        super.onStartListening()


    }
}