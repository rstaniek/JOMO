package dk.mths.jomo.ui.home

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dk.mths.jomo.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var tvUsageStats: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //val textView: TextView = binding.textHome
        //homeViewModel.text.observe(viewLifecycleOwner) {       textView.text = it       }
        tvUsageStats = binding.tvUsageStats
        if(checksageStatsPermission())
            showUsageStats()
        else{
            startActivity((Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)))
        }

        return root
    }

    private fun showUsageStats() {
        var usageStatsManager: UsageStatsManager = activity?.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        var cal: Calendar = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -1)
        var queryUsageStats: List<UsageStats> =
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                cal.timeInMillis,
                System.currentTimeMillis()
            )

        var stats_data : String = ""
        for(i in 0..queryUsageStats.size-1){
            var date: Date = Date(queryUsageStats.get(i).lastTimeUsed)
            if(date == Date(0))
                continue

            stats_data = stats_data +
                    "Package Name : "+ queryUsageStats.get(i).packageName + "\n"+
                    "Last Time Used : "+ convertTime(queryUsageStats.get(i).lastTimeUsed) + "\n" +
                    "Total Time in Foreground :\n"+ convertTime2(queryUsageStats.get(i).totalTimeInForeground) + "\n\n";
        }
        tvUsageStats.setText(stats_data)
    }

    private fun convertTime(ms: Long) : String{
        var date: Date = Date(ms)
        var format : SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
        return format.format(date)
    }

    private fun convertTime2(ms: Long) : String{
        var x: Long = ms / 1000
        val seconds = x % 60
        x /= 60
        val minutes = x % 60
        x /= 60
        val hours = x % 24
        x /= 24
        val days = x

        var formattedString: String = ""

        if(days > 0)
            formattedString += String.format("%d days\n", days)

        if(hours > 0)
            formattedString += String.format("%d hours\n", hours)

        if(minutes > 0)
            formattedString += String.format("%d minutes\n", minutes)

        if(seconds > 0)
            formattedString += String.format("%d seconds\n", seconds)

        return formattedString
    }

    private fun checksageStatsPermission(): Boolean {
        var appOpsManager : AppOpsManager ? = null
        var mode: Int = 0
        appOpsManager = activity?.getSystemService(Context.APP_OPS_SERVICE) !! as AppOpsManager
        mode = appOpsManager.checkOpNoThrow(OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), activity?.packageName.toString()
        );
        return mode == MODE_ALLOWED

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}