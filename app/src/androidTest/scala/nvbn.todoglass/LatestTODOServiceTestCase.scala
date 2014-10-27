package nvbn.todoglass

import android.content.Intent
import android.test.ServiceTestCase

class LatestTODOServiceTestCase extends
ServiceTestCase[LatestTODOService](classOf[LatestTODOService]) {
  implicit def context = getSystemContext

  override def setUp() = {
    super.setUp()
    val intent = new Intent(context, classOf[LatestTODOService])
    startService(intent)
  }

  /** Ensures that live card created after service start. **/
  def testLiveCardShouldBeCreated() =
    assert {
      getService.liveCard match {
        case Some(_) => true
        case None => false
      }
    }

  /** Ensures that updating of text of live card works. **/
  def testUpdateCardText() = {
    getService.updateCardText("test card text")
    for (card <- getService.liveCard) assert(card.isPublished)
  }

  /** Ensures that show last entry works. **/
  def testShowLastEntry() = {
    getService.showLastEntry()
    for (card <- getService.liveCard) assert(card.isPublished)
  }

  /** Ensures that card unpublished after shutdown. **/
  def testOnDestroy() = {
    val liveCard = getService.liveCard
    shutdownService()
    for (card <- liveCard) assert(!card.isPublished)
    assert {
      getService.liveCard match {
        case None => true
        case _ => false
      }
    }
  }
}