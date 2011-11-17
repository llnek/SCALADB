/*??
 * COPYRIGHT (C) 2011 CHERIMOIA LLC. ALL RIGHTS RESERVED.
 *
 * THIS IS FREE SOFTWARE; YOU CAN REDISTRIBUTE IT AND/OR
 * MODIFY IT UNDER THE TERMS OF THE APACHE LICENSE,
 * VERSION 2.0 (THE "LICENSE").
 *
 * THIS LIBRARY IS DISTRIBUTED IN THE HOPE THAT IT WILL BE USEFUL,
 * BUT WITHOUT ANY WARRANTY; WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 *
 * SEE THE LICENSE FOR THE SPECIFIC LANGUAGE GOVERNING PERMISSIONS
 * AND LIMITATIONS UNDER THE LICENSE.
 *
 * You should have received a copy of the Apache License
 * along with this distribution; if not, you may obtain a copy of the
 * License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 ??*/

package com.zotoh.scaladb

import java.lang.reflect.{Field, Modifier}
import java.util.regex.Pattern
import scala.collection.mutable
import java.sql._
import scala._
import java.util.NoSuchElementException
import java.math.{BigDecimal => JBigDec, BigInteger => JBigInt}
import java.util.{Date => JDate}
import java.sql.{Date => SDate, Timestamp => STimestamp, Blob => Bloby, Clob => Cloby}
import java.io.{Reader, InputStream}

/**
 * @author kenl
*/
sealed abstract case class NullValue(val typeVal: Int)
sealed case class NullAny(override val typeVal: Int = -1) extends NullValue(typeVal)
object NullAny {}

/**
 * @author kenl
 */
class SQuery(
	private val connection: Connection, 
	private val sql: String, 
	private val params: Seq[Any]) extends Loggable {

  def this(connection: Connection, sql: String) = {
      this (connection, sql, Nil)
  }

  private def using[X](f : PreparedStatement => X) : X = {
      val stmt = buildStmt(connection, sql, params)
      try {
          f(stmt)
      }
      finally {
          try { stmt.close() } catch { case _ => }
      }
  }


  def select[X](f: ResultSet => X): Seq[X] = {
      using { stmt =>
          stmt.executeQuery()
          val rs = stmt.getResultSet
          try {
              val rows = new mutable.ListBuffer[X]
              while (rs.next()) {
                  rows += f(rs)
              }
              rows
          } finally {
              rs.close()
          }
      }
  }

  def execute() : Int = {
      using { stmt =>
          stmt.executeUpdate()
      }
  }

  private def buildStmt(c: Connection, sql: String, params: Seq[Any]): PreparedStatement = {

      debug("SQL Stmt: " + sql)

      val ps = c.prepareStatement(sql)
      var pos=1
      params.foreach { p => setBindVar(ps, pos, p); pos=pos+1;  }
      ps
  }

  private def setBindVar(ps : PreparedStatement, pos : Int, param : Any) = {
      param match {
          case s: String => ps.setString(pos, s)
          case l: Long => ps.setLong(pos, l)
          case i: Int => ps.setInt(pos, i)
          case si: Short => ps.setShort(pos, si)

          case bi : BigInt => ps.setBigDecimal(pos, new JBigDec(bi.bigInteger))
          case bd : BigDecimal => ps.setBigDecimal(pos, bd.bigDecimal)

          case jbd : JBigDec => ps.setBigDecimal(pos, jbd)
          case jbi : JBigInt => ps.setBigDecimal( pos, new JBigDec(jbi))

          case inp : InputStream => ps.setBinaryStream(pos, inp)
          case rdr : Reader => ps.setCharacterStream(pos, rdr)
          case bb : Bloby => ps.setBlob(pos, bb)
          case cb : Cloby => ps.setClob(pos, cb)

          case b: scala.Array[Byte] => ps.setBytes(pos, b)
          case b: Boolean => ps.setBoolean(pos, b)
          case d: Double => ps.setDouble(pos, d)
          case t: STimestamp => ps.setTimestamp(pos, t)
          case dt: JDate => ps.setDate(pos, new SDate(dt.getTime()))

          case _ => throw new SQLException("Unsupported param type: " + param)
      }
  }

}
