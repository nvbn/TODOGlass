package nvbn.todoglass

import android.app.Activity
import android.content.{Context, ComponentName, ServiceConnection, Intent}
import android.os.{Bundle, IBinder}
import android.util.Log
import android.view.{Menu, MenuItem}
import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.view.WindowUtils

class LatestTODOActivity extends Activity {
  val TAG = "LiveCardMenuActivity"
  var todoService: Option[LatestTODOService] = None
  var fromLiveCardVoice = false

  val mConnection = new ServiceConnection {
    override def onServiceConnected(className: ComponentName, service: IBinder) = {
      val binder = service.asInstanceOf[LatestTODOService.LocalBinder]
      todoService = Some(binder.getService)
    }

    override def onServiceDisconnected(className: ComponentName) = {
      todoService = None
    }
  }

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    val intent = new Intent(this, classOf[LatestTODOService])
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    fromLiveCardVoice = getIntent().getBooleanExtra(LiveCard.EXTRA_FROM_LIVECARD_VOICE, false)
    if (fromLiveCardVoice)
      getWindow.requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS)
  }

  override def onDestroy() = {
    super.onDestroy()
    unbindService(mConnection)
  }

  override def onAttachedToWindow() = {
    super.onAttachedToWindow()
    if (!fromLiveCardVoice) openOptionsMenu()
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.entry, menu)
    true
  }

  override def onCreatePanelMenu(featureId: Int, menu: Menu) =
    if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
      getMenuInflater.inflate(R.menu.entry, menu)
      true
    } else
      super.onCreatePanelMenu(featureId, menu)

  override def onMenuItemSelected(featureId: Int, item: MenuItem) = item.getItemId match {
    case R.id.action_add =>
      for (service <- todoService) service.update("add")
      true
    case R.id.action_done =>
      for (service <- todoService) service.update("done!")
      true
    case R.id.action_skip =>
      for (service <- todoService) service.update("skip!")
      true
    case _ =>
      super.onMenuItemSelected(featureId, item)
  }

  override def onOptionsMenuClosed(menu: Menu) = {
    super.onOptionsMenuClosed(menu)
    finish()
  }

  override def onPanelClosed(featureId: Int, menu: Menu) = {
    super.onPanelClosed(featureId, menu)
    if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) finish()
  }
}
