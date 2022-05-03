package dk.mths.jomo.service

import android.content.ContentResolver
import android.provider.Settings

open class DaltonizerService(private var contentResolver: ContentResolver) : IJomoTrigger {
    override fun enable() {
        writeSetting("accessibility_display_daltonizer_enabled", "1")
        writeSetting("accessibility_display_daltonizer", "0")
        FireLog().withContext("daltonizer", "true").sendLog("daltonizer")
    }

    override fun disable() {
        writeSetting("accessibility_display_daltonizer_enabled", "0")
        writeSetting("accessibility_display_daltonizer", "-1")
        FireLog().withContext("daltonizer", "false").sendLog("daltonizer")
    }

    private fun writeSetting(name: String, value: String){
        Settings.Secure.putString(contentResolver, name, value)
    }
}