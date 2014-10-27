package nvbn.todoglass

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.{Menu, MenuItem}
import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.view.WindowUtils
import org.scaloid.common.{LoggerTag, LocalServiceConnection, SActivity, debug}

class LatestTODOActivity extends SActivity {
  val todoService = new LocalServiceConnection[LatestTODOService]
  var fromLiveCardVoice = false
  var waitForSpeech = false
  val SPEECH_REQUEST = 0

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

  def recordNewEntry() = {
    waitForSpeech = true
    val intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    startActivityForResult(intent, SPEECH_REQUEST)
    true
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) = {
    if (requestCode == SPEECH_REQUEST && resultCode == Activity.RESULT_OK) {
      val text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                     .get(0)
      TODOEntry.entries = TODOEntry.entries:::List(TODOEntry(text))
      todoService(s => s.showLastEntry())
      waitForSpeech = false
      finish()
    }
    super.onActivityResult(requestCode, resultCode, data)
  }

  override def onMenuItemSelected(featureId: Int, item: MenuItem) = item.getItemId match {
    case R.id.action_add => recordNewEntry()
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

  override def finish() = {
    if (!waitForSpeech) super.finish()
  }
}
