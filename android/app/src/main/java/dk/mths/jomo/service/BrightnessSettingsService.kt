package dk.mths.jomo.service

import android.content.ContentResolver
import android.provider.Settings

open class BrightnessSettingsService(private var contentResolver: ContentResolver) : IJomoTrigger {

    private var originalBrightness: Int = 0
    private var customBrightness: Int = 0

    override fun enable() {
        try {
            originalBrightness = Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS);
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, customBrightness)
        } catch(e: Settings.SettingNotFoundException) {
            FireLog().sendError(e)
        }
    }

    override fun disable() {
        try {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, originalBrightness)
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
        } catch(e: Settings.SettingNotFoundException) {
            FireLog().sendError(e)
        }
    }
}