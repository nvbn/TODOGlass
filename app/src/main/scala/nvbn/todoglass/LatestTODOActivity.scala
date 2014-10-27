package nvbn.todoglass

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.{Window, Menu, MenuItem}
import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.view.WindowUtils
import org.scaloid.common.{LocalServiceConnection, SActivity}

/** Activity for context menu on latest TODO live card. **/
class LatestTODOActivity extends SActivity {
  val todoService = new LocalServiceConnection[LatestTODOService]
  var fromLiveCardVoice = false
  var waitForSpeech = false
  val SPEECH_REQUEST = 0

  /** Enables voice commands when activity started using voice. **/
  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    fromLiveCardVoice = getIntent.getBooleanExtra(LiveCard.EXTRA_FROM_LIVECARD_VOICE, false)
    if (fromLiveCardVoice)
      getWindow.requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS)
  }

  /** Opens options menu if not started using voice. **/
  override def onAttachedToWindow() = {
    super.onAttachedToWindow()
    if (!fromLiveCardVoice) openOptionsMenu()
  }

  /** Inflates menu entires when menu opened using touchpad. **/
  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.entry, menu)
    true
  }

  /** Inflates menu entires when menu opened using voice. **/
  override def onCreatePanelMenu(featureId: Int, menu: Menu) =
    if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
      getMenuInflater.inflate(R.menu.entry, menu)
      true
    } else
      super.onCreatePanelMenu(featureId, menu)

  /** Starts speech recognition for creating new todo entry. **/
  def recordNewEntry() = {
    waitForSpeech = true
    val intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    startActivityForResult(intent, SPEECH_REQUEST)
    true
  }

  /** Handles callback when new todo entry recorded. **/
  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) = {
    if (requestCode == SPEECH_REQUEST && resultCode == Activity.RESULT_OK) {
      val text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        .get(0)
      TODOEntry.entries = TODOEntry.entries ::: List(TODOEntry(text))
      todoService(s => s.showLastEntry())
      waitForSpeech = false
      finish()
    }
    super.onActivityResult(requestCode, resultCode, data)
  }

  /** Remove last entry from entries list. **/
  def makeLastEntryDone() = {
    TODOEntry.entries = TODOEntry.entries.dropRight(1)
    todoService(s => s.showLastEntry())
    true
  }

  /** Moves last entry to begining of the list. **/
  def skipLastEntry() = {
    for (last <- TODOEntry.entries.lastOption) {
      TODOEntry.entries = last :: TODOEntry.entries.dropRight(1)
      todoService(s => s.showLastEntry())
    }
    true
  }

  /** Handles menu item selection. **/
  override def onMenuItemSelected(featureId: Int, item: MenuItem) = item.getItemId match {
    case R.id.action_add => recordNewEntry()
    case R.id.action_done => makeLastEntryDone()
    case R.id.action_skip => skipLastEntry()
    case _ => super.onMenuItemSelected(featureId, item)
  }

  /** Closes the activity when menu item selected and we don't wait for speech. **/
  override def onPanelClosed(featureId: Int, menu: Menu) = {
    super.onPanelClosed(featureId, menu)
    if ((featureId == WindowUtils.FEATURE_VOICE_COMMANDS ||
      featureId == Window.FEATURE_OPTIONS_PANEL) &&
      !waitForSpeech) finish()
  }
}
