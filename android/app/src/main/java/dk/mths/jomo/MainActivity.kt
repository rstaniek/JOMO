package dk.mths.jomo

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dk.mths.jomo.databinding.ActivityMainBinding
import dk.mths.jomo.service.JomoTriggerService
import dk.mths.jomo.work.RestartWorker
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if(!foregroundServiceRunning()){
            val jomoTriggerIntent = Intent(this, JomoTriggerService::class.java)
            startForegroundService(jomoTriggerIntent)
        }

    }

    fun foregroundServiceRunning(): Boolean{
        val activityManager: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for(service in activityManager.getRunningServices(Integer.MAX_VALUE)){
            if (JomoTriggerService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun startServiceViaWorker() {
        val UNIQUE_WORK_NAME = "StartMyServiceViaWorker"
        val workManager = WorkManager.getInstance(this)

        // As per Documentation: The minimum repeat interval that can be defined is 15 minutes
        // (same as the JobScheduler API), but in practice 15 doesn't work. Using 16 here
        val request = PeriodicWorkRequest.Builder(
            RestartWorker::class.java,
            16,
            TimeUnit.MINUTES
        )
            .build()

        // to schedule a unique work, no matter how many times app is opened i.e. startServiceViaWorker gets called
        // do check for AutoStart permission
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}