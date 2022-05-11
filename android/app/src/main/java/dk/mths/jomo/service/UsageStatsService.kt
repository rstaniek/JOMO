package dk.mths.jomo.service

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import dk.mths.jomo.R
import dk.mths.jomo.utils.App
import java.util.*
import java.util.concurrent.TimeUnit

class UsageStatsService(context: Context) {

    val mContext = context;

    fun showUsageStats(historyInDays: Int): ArrayList<App> {
        var usageStatsManager: UsageStatsManager =
            mContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        var queryUsageStats: Map<String, UsageStats> = usageStatsManager.queryAndAggregateUsageStats(
            System.currentTimeMillis() - (1000 * 3600 * 24 * historyInDays),
            System.currentTimeMillis()
        )
        queryUsageStats = queryUsageStats.filter { it.value.totalTimeInForeground > 0 }

        val nonSystemApps = getNonSystemAppsList()
        val mySortedMap: MutableMap<String, UsageStats> = TreeMap()
        // Group the usageStats by application and sort them by total time in foreground
        if (queryUsageStats.size > 0) {
            for (usageStats in queryUsageStats.values) {
                if (usageStats.packageName in nonSystemApps)
                    mySortedMap[usageStats.packageName] = usageStats
            }
        }
        return showAppsUsage(mySortedMap)
    }

    private fun showAppsUsage(mySortedMap: Map<String, UsageStats>): ArrayList<App> {
        //public void showAppsUsage(List<UsageStats> usageStatsList) {
        var appsList: ArrayList<App> = ArrayList()
        var usageStatsList: List<UsageStats> = ArrayList(mySortedMap.values)

        // sort the applications by time spent in foreground
        var sortedList = usageStatsList.sortedByDescending { it.totalTimeInForeground }

        // get total time of apps usage to calculate the usagePercentage for each app
        val totalTime = sortedList.stream().map { obj: UsageStats -> obj.totalTimeInForeground }
            .mapToLong { obj: Long -> obj }.sum()

        val longestAppRunTime = sortedList.maxOf { it.totalTimeInForeground }

        //fill the appsList
        for (usageStats in sortedList) {
            try {
                val packageName = usageStats.packageName
                var icon: Drawable? =
                    AppCompatResources.getDrawable(mContext, R.drawable.no_image)
                val packageNames = packageName.split("\\.").toTypedArray()
                var appName = packageNames[packageNames.size - 1].trim { it <= ' ' }

                if (isAppInfoAvailable(usageStats)) {
                    val ai: ApplicationInfo =
                        mContext!!.getApplicationContext().getPackageManager()
                            .getApplicationInfo(packageName, 0)
                    icon = mContext!!.getApplicationContext().getPackageManager()
                        .getApplicationIcon(ai)
                    appName = mContext!!.getApplicationContext().getPackageManager()
                        .getApplicationLabel(ai)
                        .toString()
                }

                val usageDuration: String = getDurationBreakdown(usageStats.totalTimeInForeground)
                val usagePercentage = (usageStats.totalTimeInForeground * 100 / totalTime).toInt()
                val percentageOfLongestRunningApp =
                    (usageStats.totalTimeInForeground * 100 / longestAppRunTime).toInt()

                val usageStatDTO = App(
                    icon,
                    appName,
                    usagePercentage,
                    percentageOfLongestRunningApp,
                    usageDuration
                )
                appsList.add(usageStatDTO)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace();
            }
        }

        return appsList
    }

    /**
     * check if the application info is still existing in the device / otherwise it's not possible to show app detail
     * @return true if application info is available
     */
    private fun isAppInfoAvailable(usageStats: UsageStats): Boolean {
        return try {
            mContext.getApplicationContext().getPackageManager()
                .getApplicationInfo(usageStats.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * helper method to get string in format hh:mm:ss from miliseconds
     *
     * @param millis (application time in foreground)
     * @return string in format hh:mm:ss from miliseconds
     */
    private fun getDurationBreakdown(millis: Long): String {
        require(millis >= 0) { "Duration must be greater than zero!" }

        var milliseconds = millis

        var hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        var minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        var seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "$hours h $minutes m $seconds s"
    }

    private fun getNonSystemAppsList(): Map<String, String> {
        val appInfos =
            mContext.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val appInfoMap = HashMap<String, String>()
        for (appInfo in appInfos) {
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM <= 0) {
                appInfoMap[appInfo.packageName] =
                    mContext.packageManager.getApplicationLabel(appInfo).toString()
            }
        }
        return appInfoMap
    }
}