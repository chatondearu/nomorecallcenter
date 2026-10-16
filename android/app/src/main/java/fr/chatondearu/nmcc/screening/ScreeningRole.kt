package fr.chatondearu.nmcc.screening

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager

/**
 * Call filtering uses RoleManager.ROLE_CALL_SCREENING — a system *role* (default apps),
 * not a runtime permission. It will NOT appear under App info → Permissions (unlike SMS).
 */
object ScreeningRole {
    fun holdsCallScreeningRole(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java) ?: return false
            if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                return rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            }
        }
        return false
    }

    fun isRoleAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java) ?: return false
            return rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
        }
        return false
    }

    /** System dialog to hold ROLE_CALL_SCREENING (only one app at a time). */
    fun createRequestIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java) ?: return null
            if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                return rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            }
        }
        return Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
    }

    fun openDefaultAppsSettings(activity: Activity) {
        val candidates = listOf(
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS),
        )
        for (intent in candidates) {
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
                return
            }
        }
    }
}
