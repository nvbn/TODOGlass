package nvbn.todoglass

import android.content.Context
import org.json4s.NoTypeHints
import org.scaloid.common.defaultSharedPreferences
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

object TODOEntry {
  val preferenceName = "TODOEntries"
  implicit val formats = Serialization.formats(NoTypeHints)

  def entries(implicit context: Context) = {
    parse(defaultSharedPreferences.getString(preferenceName, "[]"))
      .extractOpt[List[String]] match {
      case Some(items) => items.map(x => TODOEntry(x))
      case None => Nil
    }
  }

  def entries_=(items: List[TODOEntry])(implicit context: Context) {
    val editor = defaultSharedPreferences.edit()
    editor.putString(preferenceName, write(items.map(_.text)))
    editor.commit()
  }
}

case class TODOEntry(text: String)
