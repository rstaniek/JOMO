package dk.mths.jomo.service

import android.content.ContentResolver
import android.provider.Settings

open class DimmerService(private var contentResolver: ContentResolver) : IJomoTrigger {

    private var originalBrightness: Int = 0
    private var customBrightness: Int = 0
    private var isEnabled: Boolean = false

    private val serviceTag = "dimmer"

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

    override fun enable(packageName: String) {
        try {
            Settings.System.putInt(
                contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                0
            )
            isEnabled = true
            FireLog()
                .withContext(FireLog.SETTING, FireLog.ENABLED)
                .withContext(FireLog.REASON, packageName)
                .sendLog(serviceTag)
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
            isEnabled = false
            Settings.System.putInt(
                contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            )
            FireLog()
                .withContext(FireLog.SETTING, FireLog.DISABLED)
                .sendLog(serviceTag)
        } catch (e: Settings.SettingNotFoundException) {
            FireLog().sendError(e)
        }
    }

    override fun isEnabled(): Boolean{
        return isEnabled
    }
}