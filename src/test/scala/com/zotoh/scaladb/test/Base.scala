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

package com.zotoh.scaladb.test


import java.sql.{DriverManager,Connection}
import java.util.{Date => JDate}
import org.h2.tools
import com.zotoh.scaladb._
import tools.DeleteDbFiles

/**
* @author kenl
*/
trait TestExhibit extends Loggable {

	protected val DBDIR= "/tmp/scaladbtestdir/"
	protected val DBID= "testdb"
	protected val DBUSER= "test"
	protected val DBPWD= "test123"

	protected val DBURL= "jdbc:h2:" + DBDIR + DBID
	protected val DBDRIVER= "org.h2.Driver"

	protected var dbComplexr : CompositeSQLr = _
	protected var dbSimpler : SimpleSQLr = _
	protected var dbRef : DB = _

	private val DROPDB= """drop table if exists TESTTABLE;"""
	private val CREATEDB= """
create cached table TESTTABLE (
	CID bigint identity (100,1),
	NAME varchar(255) not null,
	AGE integer not null,
	WEIGHT double,
	BIRTHDATE date,
	BIRTHTSTAMP timestamp,
	BIGDEC decimal,
	BIGINT bigint,
	DESC clob,
	BIN binary,
	CLOBY clob,
	BLOBY blob
);	
	"""

	private def useFactory : DBFactory = {
		if (true) new SimpleDBFactory else new PoolableDBFactory(4,10)
	}

	protected def genesis : Unit = {
		DeleteDbFiles.execute(DBDIR,DBID,true)
		Class.forName(DBDRIVER)
		createdb
	}

	private def createdb : Unit = {
		val c= DriverManager.getConnection(DBURL, DBUSER,DBPWD)
		c.setAutoCommit(true)
		val s=c.prepareStatement(CREATEDB)
		s.executeUpdate()
		s.close()
		c.close()
	}

	protected def iniz = {
		dbRef=ScalaDB(useFactory)(DBDRIVER,DBURL,DBUSER,DBPWD)
		dbComplexr=dbRef.newCompositeSQLProcessor
    	dbSimpler= dbRef.newSimpleSQLProcessor
        genesis
	}

	protected def finz = {
		dbRef.finz
	}

}


/**
 * @author kenl
 */
class TestTable extends SRecord {
    override def getSchemaFactory() = TestTable
}

/**
 * @author kenl
 */
object TestTable extends SRecordFactory {

    override def getUpdatableCols = {
        columns - "CID"
    }

    override val columns =
        Set("CID", "NAME", "AGE", "WEIGHT", "BIRTHDATE", "BIRTHTSTAMP", "BIGDEC", "BIGINT", "DESC", "BIN", "CLOBY", "BLOBY")

    override def getTableName = {
        "TESTTABLE"
    }

    override def getPrimaryKeys = {
        Set("CID")
    }

    override def createEmptyRecord() = {
        new TestTable
    }

}


