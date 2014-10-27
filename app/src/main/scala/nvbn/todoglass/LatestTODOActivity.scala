package nvbn.todoglass

import android.os.Bundle
import android.view.{Menu, MenuItem}
import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.view.WindowUtils
import org.scaloid.common.{LocalServiceConnection, SActivity}

class LatestTODOActivity extends SActivity {
  val todoService = new LocalServiceConnection[LatestTODOService]
  var fromLiveCardVoice = false

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    fromLiveCardVoice = getIntent.getBooleanExtra(LiveCard.EXTRA_FROM_LIVECARD_VOICE, false)
    if (fromLiveCardVoice)
      getWindow.requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS)
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
      todoService(s => s.updateCardText("add"))
      true
    case R.id.action_done =>
      todoService(s => s.updateCardText("done!"))
      true
    case R.id.action_skip =>
      todoService(s => s.updateCardText("skip!"))
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
