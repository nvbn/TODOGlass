package nvbn.todoglass

import android.test.ActivityInstrumentationTestCase2

class TODOEntryTestCase extends
ActivityInstrumentationTestCase2[LatestTODOActivity](classOf[LatestTODOActivity]) {
  implicit def context = getInstrumentation.getTargetContext

  /** Ensures that setter and getter of entries works. **/
  def testEntries() = {
    val entries = TODOEntry("test") :: TODOEntry("test2") :: Nil
    TODOEntry.entries = entries
    assert(entries == TODOEntry.entries)
  }
}
