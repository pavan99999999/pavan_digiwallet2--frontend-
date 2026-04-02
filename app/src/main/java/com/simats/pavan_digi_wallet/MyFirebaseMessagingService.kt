package com.simats.pavan_digi_wallet

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // When a new token is generated (e.g. app first install)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Store the token immediately
        val sharedPreferences = getSharedPreferences("digi_wallet", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("FCM_TOKEN", token).apply()
        
        // Also try to send it to the backend if the user is logged in
        val userId = sharedPreferences.getInt("user_id", -1)
        if (userId != -1) {
             val request = FcmTokenRequest(token)
             RetrofitClient.apiService.updateFcmToken(userId, request).enqueue(object : Callback<Map<String, Any>> {
                 override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                     // Token synced successfully
                 }
                 override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                     // Retry later or ignore
                 }
             })
        }
    }

    // When a Push Notification arrives
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val channelId = "PlannedPaymentsChannel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0+ Requires Notification Channels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Planned Payments", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Use your app's icon
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(0, notificationBuilder.build())
    }
}
