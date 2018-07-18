package com.example.kafkastreamsscalatemplate.util

/** Helper class to check if a string is valid or not (ie. non-null and nonempty).
  * Easily check if a string is non-null and non-empty in one step, the same
  * way you would check an Option:
  * 
  *   if (ValidString(someString).nonEmpty) {...}
  * 
  *     (OR)
  * 
  *   ValidString(someString).map{ str => ... }
  */
object ValidString
{
  /** Convert a string to an Some[String], only if not None or null.
    * Otherwise, return None.
    */
  def apply(s: String): Option[String] = {
    if(null == s || s.isEmpty) None
    else Some(s)
  }

  /** Convert an Option[String] to an Some[String], only if not None or null.
    * Otherwise, return None.
    */
  def apply(opt: Option[String]): Option[String] = opt match {
    case None => None
    case Some(s) => if(s.isEmpty) None else opt
  }

}
