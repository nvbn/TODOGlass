package nvbn.todoglass

import android.os.Binder
import com.google.android.glass.timeline.LiveCard
import com.google.android.glass.timeline.LiveCard.PublishMode
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.widget.RemoteViews

object LatestTODOService {
  private final val LIVE_CARD_TAG: String = "EntryActivity"

  class LocalBinder(service: LatestTODOService) extends Binder {
    def getService = service
  }

}

class LatestTODOService extends Service {
  val TAG = "EntryActivity"
  var liveCard: Option[LiveCard] = None

  val binder = new LatestTODOService.LocalBinder(this)

  def onBind(intent: Intent) = binder

  override def onStartCommand(intent: Intent, flags: Int, startId: Int) = {
    liveCard match {
      case Some(card) => card.navigate()
      case None =>
        val card = new LiveCard(this, LatestTODOService.LIVE_CARD_TAG)
        liveCard = Some(card)
        update("Nothing to do...")
        val menuIntent = new Intent(this, classOf[LatestTODOActivity])
        card.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0))
        card.setVoiceActionEnabled(true)
        card.publish(PublishMode.REVEAL)
    }
    Service.START_STICKY
  }

  def update(text: String) = for (card <- liveCard) {
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
