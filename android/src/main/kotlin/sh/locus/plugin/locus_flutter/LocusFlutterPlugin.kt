package sh.locus.plugin.locus_flutter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.reactivex.schedulers.Schedulers
import sh.locus.lotr.sdk.*
import sh.locus.lotr.sdk.auth.UserAuthParams
import sh.locus.lotr.sdk.exception.LotrSdkError
import sh.locus.lotr.sdk.logging.LotrSdkEventType

/** LocusFlutterPlugin */
class LocusFlutterPlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler,
  ActivityAware {

  private lateinit var eventChannel: EventChannel
  private lateinit var channel: MethodChannel
  private lateinit var context: Context
  private lateinit var activity: Activity


  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {}

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}

  override fun onDetachedFromActivity() {}

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "locus_flutter")
    channel.setMethodCallHandler(this)
    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "locus_flutter_stream")
    eventChannel.setStreamHandler(this)
  }


  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "login" -> login(call, result)
      "logout" -> logout(result)
      "startTracking" -> startTracking(result)
      "stopTracking" -> stopTracking(result)
      "getSdkState" -> getSdkState(result)
      else -> result.notImplemented()
    }

  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun login(@NonNull call: MethodCall, @NonNull result: Result) {
    val clientId = call.argument<String>("clientId") as String
    val userId = call.argument<String>("userId") as String
    val password = call.argument<String>("password") as String
    try {
      LocusLotrSdk.init(
        context,
        UserAuthParams(clientId, userId, password),
        true,
        object : LotrSdkReadyCallback {
          override fun onAuthenticated() {
            result.success("Logged in successfully")
          }

          override fun onError(error: LotrSdkError) {
            result.error("login", error.message, null)
          }
        }
      )
    } catch (e: Exception) {
      e.printStackTrace()
      result.error("login", e.message, e)
    }
  }

  private fun logout(result: Result) {
    try {
      LocusLotrSdk.logout(true, object : LogoutStatusListener {
        override fun onFailure() {
          result.error("onFailure", "Logout failed", null)
        }

        override fun onSuccess() {
          result.success("Logout success")
        }
      })
    } catch (e: IllegalStateException) {
      e.printStackTrace()
      result.error("init", e.message, e)
    }
  }

  private fun startTracking(result: Result) {
    try {
      val requestParams = TrackingRequestParams.Builder().build()
      val trackingListener = object : TrackingListener {
        override fun onLocationError(lotrSdkError: LotrSdkError) {
          Log.e("startTracking", "onLocationError: ${lotrSdkError.message}")
        }

        override fun onLocationUpdated(location: LocusLocation) {
          Log.i("startTracking", "onLocationUpdated: ${location.toIndentedString()}")
        }

        override fun onLocationUploaded(location: LocusLocation) {

        }
      }
      LocusLotrSdk.startTracking(trackingListener, requestParams)
      result.success("Started tracking")
    } catch (e: Exception) {
      result.error("startTracking", e.message, e)
    }
  }

  private fun stopTracking(result: Result) {
    try {
      LocusLotrSdk.stopTracking()
      result.success("Stopped tracking")
    } catch (e: Exception) {
      result.error("stopTracking", e.message, e)
    }
  }

  private fun getSdkState(result: Result) {
    try {
      val state = LocusLotrSdk.getSdkState()
      result.success(state.name)
    } catch (e: Exception) {
      result.error("getSdkState", e.message, e)
    }
  }

  @SuppressLint("CheckResult")
  private fun getSdkEvents(events: EventChannel.EventSink?) {
    LocusLotrSdk.getSdkEventsObservable()
      .subscribe(
        { lotrSdkEvent ->
          activity.runOnUiThread { events?.success(lotrSdkEvent.message) }

        },
        { throwable ->
          activity.runOnUiThread {
            throwable.printStackTrace()
            events?.error("sdkEvents", throwable.message, null)
          }
        }
      )
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    getSdkEvents(events)
  }

  override fun onCancel(arguments: Any?) {
  }


}
