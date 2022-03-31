package dk.mths.jomo.preferences

import org.androidannotations.annotations.sharedpreferences.*


@SharedPref(SharedPref.Scope.APPLICATION_DEFAULT)
interface JomoPreferences {
    @DefaultBoolean(false)
    fun grayscaleEnabled(): Boolean

    @DefaultBoolean(false)
    fun isRunning(): Boolean

}