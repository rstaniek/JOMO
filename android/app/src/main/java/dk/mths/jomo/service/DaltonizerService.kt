package dk.mths.jomo.service

import android.content.ContentResolver
import android.provider.Settings

open class DaltonizerService(private var contentResolver: ContentResolver) : IJomoTrigger {
    private val serviceTag = "daltonizer"
    private val daltonizerEnabledStr = "accessibility_display_daltonizer_enabled"
    private val daltonizerModeStr = "accessibility_display_daltonizer"

    override fun enable(packageName: String) {
        writeSetting(daltonizerEnabledStr, "1")
        writeSetting(daltonizerModeStr, "0")
        FireLog()
            .withContext(FireLog.SETTING, FireLog.ENABLED)
            .withContext(FireLog.REASON, packageName)
            .sendLog(serviceTag)
    }

    override fun disable() {
        writeSetting(daltonizerEnabledStr, "0")
        writeSetting(daltonizerModeStr, "-1")
        FireLog()
            .withContext(FireLog.SETTING, FireLog.DISABLED)
            .sendLog(serviceTag)
    }

    private fun writeSetting(name: String, value: String){
        Settings.Secure.putString(contentResolver, name, value)
    }

    override fun isEnabled(): Boolean{
        if(getSetting("accessibility_display_daltonizer_enabled") == "1"){
            return true
        }
        return false
    }

    private fun getSetting(name: String): String{
        return Settings.Secure.getString(contentResolver, name)
    }
}