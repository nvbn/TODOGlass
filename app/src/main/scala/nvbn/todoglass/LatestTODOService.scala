package nvbn.todoglass

import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.timeline.LiveCard.PublishMode
import android.app.Service
import android.content.Intent
import android.widget.RemoteViews
import org.scaloid.common.{LocalService, pendingActivity}

/** Service for controlling TODOs live card. **/
class LatestTODOService extends LocalService {
  val LIVE_CARD_TAG = "LatestTODOService"
  var liveCard: Option[LiveCard] = None

  /** Creates live card instance if it not created before. And shows last entry text. **/
  override def onStartCommand(intent: Intent, flags: Int, startId: Int) = {
    liveCard match {
      case Some(card) => card.navigate()
      case None =>
        val card = new LiveCard(this, LIVE_CARD_TAG)
        liveCard = Some(card)
        showLastEntry()
        card.setAction(pendingActivity[LatestTODOActivity])
          .setVoiceActionEnabled(true)
          .publish(PublishMode.REVEAL)
    }
    Service.START_STICKY
  }

  /** Updates text on live card. **/
  def updateCardText(text: String) = for (card <- liveCard) {
    val remoteViews = new RemoteViews(getPackageName, R.layout.entry)
    remoteViews.setTextViewText(R.id.text, text)
    card.setViews(remoteViews)
  }

  /** Shows last entry text or notification about no entries on live card. **/
  def showLastEntry() = TODOEntry.entries.lastOption match {
    case Some(entry) => updateCardText(entry.text)
    case None => updateCardText("Nothing to do...")
  }

  /** Unpublishes live card before destroing. **/
  override def onDestroy() = {
    for (card <- liveCard) {
      card.unpublish()
      liveCard = None
    }
    super.onDestroy()
  }
}
