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

import java.sql.{SQLException, Connection, ResultSet}
import scala.collection.mutable


/**
 * @author kenl
*/
sealed abstract class SQLBlockType
object SQLComplexType extends SQLBlockType {}
object SQLSimpleType extends SQLBlockType  {}

/**
 * @author kenl
*/
trait SQLProcessor extends Loggable {

    type DMap = Map[String, Any]

    def select[X]( sql: String, params: Seq[Any])(f: ResultSet => X): Seq[X]

    def select[X]( sql: String)(f: ResultSet => X): Seq[X]

    def execute(sql: String, params: Seq[Any]): Int

    def execute(sql: String): Int

    def insert( obj : SRecord) : Int

    def update( obj : SRecord) : Int = {
        update(obj, obj.getSchemaFactory.getUpdatableCols )
    }

    def update( obj : SRecord, cols : scala.collection.Set[String] ) : Int

    def delete( obj : SRecord) : Int

    def findSome(fac : SRecordFactory, filter : DMap) : Seq[SRecord]

    def findAll(fac : SRecordFactory) : Seq[SRecord]

    protected def doFindSome(fac : SRecordFactory, m : DMap) : Seq[SRecord] = {
        val lst = new mutable.ListBuffer[Any]
        val wb= new StringBuffer(512)
        val s= "SELECT * FROM " + fac.getTableName
        m.foreach {
            kv =>
            if (wb.length > 0) { wb.append(" AND ") }
            wb.append(kv._1)
            kv._2 match {
              case NullAny => wb.append("=NULL")
              case null => wb.append("=NULL")
              case _ => { wb.append("=?"); lst += kv._2 }
            }
        }
        val cb : ResultSet => SRecord = { row : ResultSet => fac.create(row)  }
        if (wb.length > 0) {
            select( s + " WHERE " + wb, lst.toSeq )(cb)
        } else {
            select( s)(cb)
        }
    }

    protected def doFindAll(fac : SRecordFactory) : Seq[SRecord] = {
        val cb : ResultSet => SRecord = { row : ResultSet => fac.create(row)  }
        select("SELECT * FROM " + fac.getTableName) (cb)
    }

    protected def pkeys(obj : SRecord) : (String, List[Any]) = {
        val lst = new mutable.ListBuffer[Any]
        val sf= obj.getSchemaFactory
        val sb1= new StringBuffer(512)
        sf.getPrimaryKeys foreach {
            k =>
            if (sb1.length > 0) { sb1.append(" AND ") }
            sb1.append(k).append("=?")
            val v =obj.getVal(k)
            v match {
                case None => throw new SQLException("Primary key has no value")
                case Some(NullAny) => throw new SQLException("Primary key has NULL value")
                case _ => {
                    lst += v.get
                }
            }
        }
        (sb1.toString, lst.toList)
    }

    protected def doUpdate(obj : SRecord, cols : scala.collection.Set[String]) : Int = {
        val sf = obj.getSchemaFactory
        val pks= pkeys(obj)
        val sb1= new StringBuffer(1024)
        val lst= new mutable.ListBuffer[Any]
        cols foreach {
            k =>
            val v= obj.getVal(k)
            if (sb1.length > 0) { sb1.append(",") }
            sb1.append(k)
            v match {
                case None => sb1.append("=NULL")
                case _ => {
                    sb1.append("=?")
                    lst += v.get
                }
            }
        }
        if (sb1.length > 0) {
            lst.appendAll(pks._2)
            execute("UPDATE " + sf.getTableName + " SET " + sb1 + " WHERE " + pks._1 , lst.toSeq)
        }
        else {
            0
        }
    }


    protected def doDelete(c : Connection, obj : SRecord) : Int = {
        val pks = pkeys(obj)
        if (pks._1.length > 0) {
            execute( "DELETE FROM " + obj.getSchemaFactory.getTableName +
                " WHERE " + pks._1 , pks._2.toSeq )
        } else {
            0
        }
    }

    protected def doInsert(c : Connection, obj: SRecord) : Int = {
        val lst = new mutable.ListBuffer[Any]
        val s1= new StringBuffer(1024)
        val s2 = new StringBuffer(1024)
        obj.getSchemaFactory.getCreationCols foreach { k =>
            val v = obj.getVal(k)
            if (s1.length > 0) { s1.append(",")}
            s1.append(k)
            if (s2.length > 0) { s2.append(",")}
            v match {
                case None => s2.append("NULL")
                case Some(_) =>  {
                    s2.append("?")
                    lst += v.get
                }
            }
        }
        if (s1.length > 0) {
            execute ( "INSERT INTO " + obj.getSchemaFactory.getTableName + "(" + s1 + ") VALUES (" + s2 + ")" , lst.toSeq )
        } else {
            0
        }
    }

}

/**
 * @author kenl
*/
class SimpleSQLr(private val db: DB) extends SQLProcessor {

    def findSome(fac : SRecordFactory, filter : DMap) : Seq[SRecord] = {
        doFindSome(fac,filter)
    }

    def findAll(fac : SRecordFactory) : Seq[SRecord] = {
        doFindAll(fac)
    }

    def update(obj : SRecord, cols : scala.collection.Set[String]) : Int = {
        doUpdate(obj, cols)
    }

    def delete(obj : SRecord) : Int = {
        val c= db.open
        try {
            c.setAutoCommit(true)
            doDelete(c, obj)
        }
        finally {
            db.close(c)
        }
    }

    def insert(obj : SRecord) : Int = {
        val c= db.open
        try {
            c.setAutoCommit(true)
            doInsert(c, obj)
        }
        finally {
            db.close(c)
        }
    }

    def select[X]( sql: String, params: Seq[Any])(f: ResultSet => X): Seq[X] = {
        val c= db.open
        try {
            c.setAutoCommit(true)
            new SQuery(c, sql, params  ).select(f)
        }
        finally {
            db.close(c)
        }
	  }

    def select[X]( sql: String)(f: ResultSet => X): Seq[X] = {
        select(sql, List[Any]())(f)
	  }

    def execute( sql: String, params: Seq[Any]): Int = {
        val c= db.open
        try {
            c.setAutoCommit(true)
            new SQuery(c, sql, params).execute()
        }
        finally {
            db.close(c)
        }
	  }

    def execute( sql: String): Int = {
        execute(sql, List[Any]())
	  }

}

/**
 * @author kenl
 */
class CompositeSQLr(private val db : DB) extends Loggable {

    def execWith[X](f: Transaction => X) = {
      val c= begin
      try {
        val t = new Transaction(c)
        f(t)
        commit(c)
      }
      catch {
        case e : Exception => { rollback(c) ; e.printStackTrace }
      }
      finally {
        close(c)
      }
	}

    private def rollback(c :Connection) : Unit = {
      try { c.rollback() } catch { case _ => }
    }
	
    private def commit(c : Connection) : Unit = {
      c.commit()
    }
	
    private def begin : Connection =  {
      val c= db.open
      c.setAutoCommit(false)
      c
    }

    private def close(c: Connection) : Unit = {
      try { c.close() } catch { case _ => }
    }

}

/**
 * @author kenl
 */
class Transaction(private val connection : Connection ) extends SQLProcessor {

    def insert(obj : SRecord) : Int = {
        doInsert(connection, obj)
    }

    def select[X]( sql: String, params: Seq[Any])(f: ResultSet => X): Seq[X] = {
        new SQuery(connection, sql, params  ).select(f)
	}

    def select[X]( sql: String)(f: ResultSet => X): Seq[X] = {
        select(sql, List[Any]()) (f)
	}

    def execute( sql: String, params: Seq[Any]): Int = {
        new SQuery(connection, sql, params ).execute()
	}

    def execute( sql: String): Int = {
        execute(sql, List[Any]())
	}

    def delete( obj : SRecord) : Int = {
        doDelete(connection, obj)
    }

    def update(obj : SRecord, cols : scala.collection.Set[String]) : Int = {
        doUpdate(obj, cols)
    }

    def findSome(fac : SRecordFactory, filter : DMap) : Seq[SRecord] = {
        doFindSome(fac,filter)
    }

    def findAll(fac : SRecordFactory) : Seq[SRecord] = {
        doFindAll(fac)
    }

}
