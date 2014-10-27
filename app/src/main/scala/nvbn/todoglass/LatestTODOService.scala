package nvbn.todoglass

import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.timeline.LiveCard.PublishMode
import android.app.Service
import android.content.Intent
import android.widget.RemoteViews
import org.scaloid.common.{LocalService, pendingActivity}

object LatestTODOService {
  private final val LIVE_CARD_TAG: String = "LatestTODOService"
}

class LatestTODOService extends LocalService {
  var liveCard: Option[LiveCard] = None

  override def onStartCommand(intent: Intent, flags: Int, startId: Int) = {
    liveCard match {
      case Some(card) => card.navigate()
      case None =>
        val card = new LiveCard(this, LatestTODOService.LIVE_CARD_TAG)
        liveCard = Some(card)
        updateCardText("Nothing to do...")
        card.setAction(pendingActivity[LatestTODOActivity])
            .setVoiceActionEnabled(true)
            .publish(PublishMode.REVEAL)
    }
    Service.START_STICKY
  }

  def updateCardText(text: String) = for (card <- liveCard) {
    val remoteViews = new RemoteViews(getPackageName, R.layout.entry)
    remoteViews.setTextViewText(R.id.text, text)
    card.setViews(remoteViews)
  }

  override def onDestroy() = {
    for (card <- liveCard) {
      card.unpublish()
      liveCard = None
    }
    super.onDestroy()
  }
}
