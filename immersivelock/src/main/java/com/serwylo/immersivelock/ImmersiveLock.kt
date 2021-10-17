package com.serwylo.immersivelock

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

class ImmersiveLock(
   private val tapToUnlockView: View,
   private val touchesRequiredToUnlock: Int = DEFAULT_TOUCHES_REQUIRED_TO_UNLOCK,
   private val maxTimeBetweenTouches: Long = DEFAULT_TIME_BETWEEN_TOUCHES,
   private val unlockedMessageStringRes: Int = R.string.screen_unlocked,
   private val touchLockToUnlockedMessagePluralRes: Int = R.plurals.touch_lock_to_unlock,
   private val onStopImmersiveMode: (() -> Unit)? = null,
) {

   companion object {

      /**
       * To escape sticky immersive mode when locking the screen, the user must press the lock
       * icon in the corner this many times.
       */
      const val DEFAULT_TOUCHES_REQUIRED_TO_UNLOCK = 5

      const val DEFAULT_TIME_BETWEEN_TOUCHES = 750L

   }

   class Builder(private val tapToUnlockView: View) {

      private var touchesRequiredToUnlock: Int = DEFAULT_TOUCHES_REQUIRED_TO_UNLOCK

      private var maxTimeBetweenTouches: Long = DEFAULT_TIME_BETWEEN_TOUCHES

      @StringRes
      private var unlockedMessageStringRes: Int = R.string.screen_unlocked

      @PluralsRes
      private var touchLockToUnlockedMessagePluralRes: Int = R.plurals.touch_lock_to_unlock

      private var onStopImmersiveMode: (() -> Unit)? = null

      fun touchesRequiredToUnlock(value: Int): Builder {
         touchesRequiredToUnlock = value
         return this
      }

      fun maxTimeBetweenTouches(value: Long): Builder {
         maxTimeBetweenTouches = value
         return this
      }

      fun unlockedMessageStringRes(@StringRes value: Int): Builder {
         unlockedMessageStringRes = value
         return this
      }

      fun touchLockToUnlockedMessagePluralRes(@PluralsRes value: Int): Builder {
         touchLockToUnlockedMessagePluralRes = value
         return this
      }

      fun onStopImmersiveMode(value: () -> Unit): Builder {
         onStopImmersiveMode = value
         return this
      }

      fun build(): ImmersiveLock {
         return ImmersiveLock(
            tapToUnlockView,
            touchesRequiredToUnlock,
            maxTimeBetweenTouches,
            unlockedMessageStringRes,
            touchLockToUnlockedMessagePluralRes,
            onStopImmersiveMode,
         )
      }
   }

   /**
    * Put the screen in "Immersive sticky mode" which prevents accidental clicking of home or other
    * buttons that could leave the app. However the UX to escape this is pretty terrible (for
    * somewhat understandable reasons), so also show a lock which can be used to unlock this mode.
    *
    * To unlock this mode, you need to tap the unlock button [touchesRequiredToUnlock] times, each
    * time no more than [maxTimeBetweenTouches] apart.
    */
   @SuppressLint("ShowToast") // We keep the toast so that we can cancel when the user is prompted to press many times quickly. Therefore "show()" is called later on.
   fun startImmersiveMode(activity: Activity) {

      try {
         @Suppress("DEPRECATION") // The recommended alternative was only introduced in API 30.
         activity.window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
      } catch (e: Exception) {}

      lockActivityOrientation(activity)

      activity.startLockTask()

      tapToUnlockView.visibility = View.VISIBLE

      var lastClickTime = -1L
      var counter = touchesRequiredToUnlock

      // Remember the toast, so that we can update it when the user touches multiple times quickly,
      // rather than showing multiple toasts on top of eachother, whch tends to have a bad UX on
      // some Android versions.
      var toast: Toast? = null

      tapToUnlockView.setOnClickListener {
         if (lastClickTime == -1L || System.currentTimeMillis() - lastClickTime > maxTimeBetweenTouches) {
            counter = touchesRequiredToUnlock - 1
            toast?.cancel()
            toast = null
         } else {
            counter -= 1
         }

         if (counter == 0) {
            toast?.cancel()
            toast = null
            stopImmersiveMode(activity)
         } else {
            lastClickTime = System.currentTimeMillis()

            val toastMessage = activity.resources.getQuantityString(touchLockToUnlockedMessagePluralRes, counter, counter)

            if (toast == null) {
               toast = Toast.makeText(
                  activity,
                  toastMessage,
                  Toast.LENGTH_SHORT
               )
            } else {
               toast?.setText(toastMessage)
            }

            toast?.show()
         }
      }

   }

   /**
    * During immersive mode, don't let the screen change orientation in immersive mode - the baby
    * will likely be moving the phone all about and there is no benefit to changing orientation
    * (there is no real sense of "up" when in full screen).
    *
    * See:
    *  - Original bug report: https://github.com/babydots/babydots/issues/40 and
    *  - Source for this fix: https://stackoverflow.com/questions/3611457/android-temporarily-disable-orientation-changes-in-an-activity
    */
   @SuppressLint("NewApi", "SourceLockedOrientationActivity")
   private fun lockActivityOrientation(activity: Activity) {

      val display = activity.windowManager.defaultDisplay
      val rotation = display.rotation

      val size = Point()
      display.getSize(size)

      val height = size.y
      val width = size.x

      activity.requestedOrientation = when (rotation) {
         Surface.ROTATION_90 ->
            if (width > height) {
               ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
               ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            }
         Surface.ROTATION_180 ->
            if (height > width) {
               ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            } else {
               ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
         Surface.ROTATION_270 ->
            if (width > height) {
               ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            } else {
               ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
         else ->
            if (height > width) {
               ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
               ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
      }
   }

   /**
    * You probably don't want want to call this directly, instead letting it be invoked when the
    * user has successfully performed the requirements to unlock.
    */
   fun stopImmersiveMode(activity: Activity) {

      tapToUnlockView.visibility = View.GONE

      activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
      activity.stopLockTask()

      try {
         @Suppress("DEPRECATION") // The recommended alternative was only introduced in API 30.
         activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
      } catch (e: Exception) {}

      Toast.makeText(activity, activity.getString(unlockedMessageStringRes), Toast.LENGTH_SHORT).show()

      onStopImmersiveMode?.invoke()

   }

}