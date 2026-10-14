package fr.chatondearu.nmcc.screening

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager

object ScreeningRole {
    const val REQUEST_CODE = 4401

    fun holdsCallScreeningRole(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java) ?: return false
            if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                return rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            }
        }
        return false
    }

    fun requestCallScreeningRole(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = activity.getSystemService(RoleManager::class.java) ?: return
            if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                activity.startActivityForResult(
                    rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING),
                    REQUEST_CODE,
                )
                return
            }
        }
        // Fallback intent (rare OEMs)
        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
        activity.startActivity(intent)
    }
}
