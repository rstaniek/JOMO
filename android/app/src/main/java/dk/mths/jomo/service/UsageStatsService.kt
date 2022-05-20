package dk.mths.jomo.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.google.common.collect.ImmutableList
import dk.mths.jomo.R
import dk.mths.jomo.utils.App
import java.util.*
import java.util.concurrent.TimeUnit

class UsageStatsService(context: Context) {

    private val mContext = context
    private val usageStatsManager = mContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager


    fun showUsageStats(historyInDays: Int): ArrayList<App> {
        val c: Calendar = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)

        val start = c.timeInMillis - (1000 * 3600 * 24 * historyInDays)
        val end = System.currentTimeMillis()
        var queryUsageStats: Map<String, UsageStats> = usageStatsManager.queryAndAggregateUsageStats(
            start,
            end
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
        val eventMap = getUsageEvents(start, end)

        return showAppsUsage(mySortedMap, eventMap)
    }

    private fun showAppsUsage(mySortedMap: Map<String, UsageStats>, eventMap: Map<String, Int>): ArrayList<App> {
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
                val openCount: Int = eventMap.getOrDefault(packageName, 0)
                var averageDuration: String = "0"
                if(openCount != 0)
                  averageDuration = getDurationBreakdown(usageStats.totalTimeInForeground / openCount.toLong())

                val usageStatDTO = App(
                    icon,
                    appName,
                    usagePercentage,
                    percentageOfLongestRunningApp,
                    usageDuration,
                    openCount,
                    averageDuration
                )
                appsList.add(usageStatDTO)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace();
            }
        }

        return appsList
    }

    fun getUsageEvents(start: Long, end: Long): Map<String, Int> {
        var eventMap: MutableMap<String, Int> = mutableMapOf()
        val currentEvent: UsageEvents.Event = UsageEvents.Event()
        val usageEvents = usageStatsManager.queryEvents(start, end)
        val nonSystemApps = getNonSystemAppsList()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(currentEvent)
            if (currentEvent.getEventType() === UsageEvents.Event.ACTIVITY_RESUMED
            ) {
                val key: String = currentEvent.getPackageName()
                if(key in nonSystemApps){
                    if (eventMap.get(key) == null) {
                        eventMap.put(key, 1)
                    }
                    eventMap.merge(key,1, Int::plus)
                }
            }
        }

        return eventMap
    }

    fun getEventhistoryForBadApps(start: Long, end: Long, packages: ImmutableList<String>): ArrayList<UsageEvents.Event>{
        var eventList: ArrayList<UsageEvents.Event> = ArrayList()
        val currentEvent: UsageEvents.Event = UsageEvents.Event()
        val usageEvents = usageStatsManager.queryEvents(start, end)
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(currentEvent)
            if (currentEvent.getEventType() === UsageEvents.Event.ACTIVITY_RESUMED &&
                        currentEvent.packageName in packages &&
                        eventList.any {it.packageName != currentEvent.packageName }
            ) {
                eventList.add(currentEvent)
            }
        }
        return eventList
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

    fun getForegroundPackage(): String? {
        val time = System.currentTimeMillis()
        val list = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 1000, time
        )
        if (list != null && !list.isEmpty()) {
            val map: SortedMap<Long, UsageStats> = TreeMap()
            for (stats in list) {
                map[stats.lastTimeUsed] = stats
            }
            if (!map.isEmpty()) {
                return map[map.lastKey()]!!.packageName
            }
        }

        return null
    }

    fun getForegroundPackagePretty(): String?{
        val packageName = getForegroundPackage()
        if(packageName != null){
            val ai: ApplicationInfo =
                mContext!!.getApplicationContext().getPackageManager()
                    .getApplicationInfo(packageName, 0)
            val appName = mContext!!.getApplicationContext().getPackageManager()
                .getApplicationLabel(ai)
                .toString()
            return appName
        }
        return packageName
    }
}