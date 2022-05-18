package dk.mths.jomo.service

import android.content.ContentResolver
import android.provider.Settings

open class DimmerService(private var contentResolver: ContentResolver) : IJomoTrigger {

    private var originalBrightness: Int = 0
    private var customBrightness: Int = 0

    init {
        originalBrightness = Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        );

        Settings.System.putInt(
            contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
    }

    override fun enable() {
        try {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                0
            )
            FireLog()
                .withContext("brightnessSetting", "true")
                .sendLog("dimmer")
        } catch (e: Settings.SettingNotFoundException) {
            FireLog().sendError(e)
        }
    }

    override fun disable() {
        try {
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                originalBrightness
            )
            FireLog()
                .withContext("brightnessSetting", "false")
                .sendLog("dimmer")
        } catch (e: Settings.SettingNotFoundException) {
            FireLog().sendError(e)
        }
    }

    override fun isEnabled(): Boolean{
        if(Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS) != originalBrightness){
            return true
        }
        return false
    }

}