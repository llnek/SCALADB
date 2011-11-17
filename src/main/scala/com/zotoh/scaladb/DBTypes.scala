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

import org.apache.commons.dbcp.{DriverManagerConnectionFactory, PoolableConnectionFactory, DriverConnectionFactory, PoolingDataSource}
import java.sql.{DriverManager, SQLException, Connection}
import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.commons.pool.KeyedObjectPoolFactory

/**
 * @author kenl
 */
class SimpleDBFactory extends DBFactory {

    override def apply(dbDriver: String, dbUrl: String, dbUser: String, dbPwd: String): DB = {
        new SimpleDB(dbDriver, dbUrl, dbUser, dbPwd)
    }
}

/**
 * @author kenl
 */
class SimpleDB( override val dbDriver : String,
    override val dbUrl : String,
    override val dbUser : String,
    override val dbPwd : String ) extends DB {

	// this is not used, don't know why I keep the code here :P
    private val props = new java.util.Properties()
    props.put("username", dbUser)
    props.put("password", dbPwd)
    Class.forName(dbDriver)
    private val dcf = new DriverManagerConnectionFactory(dbUrl, dbUser, dbPwd)

    override def finz : Unit = {
		// nothing needs to be cleaned
    }

    /*
    override def close(c : Connection) = {
		try { c.close() } catch { case _ => }
	}
     */

    override def open = dcf.createConnection

}


/**
 * @author kenl
 */
class PoolableDBFactory( private val minConns: Int,
	private val maxConns: Int,
	private val maxWaitForConnMillis: Long = 5000,
	private val checkEvictionIntervalMillis: Long = 180000,
	private val evictConnIfIdleMillis: Long = 300000) extends DBFactory {

    override def apply(dbDriver: String, dbUrl: String, dbUser: String, dbPwd: String): DB = {
        new PoolableDB(dbDriver, dbUrl, dbUser, dbPwd,
            minConns, maxConns,
            maxWaitForConnMillis,
            checkEvictionIntervalMillis,
            evictConnIfIdleMillis
        )
    }

}

/**
 * @author kenl
 */
class PoolableDB( override val dbDriver: String,
	override val dbUrl: String,
	override val dbUser: String,
	override val dbPwd: String,
	private val minConns: Int,
	private val maxConns: Int,
	private val maxWaitForConnMillis: Long,
	private val checkEvictionIntervalMillis: Long,
	private val evictConnIfIdleMillis: Long) extends DB {

    private val props = new java.util.Properties()
    props.put("username", dbUser)
    props.put("password", dbPwd)
    Class.forName(dbDriver)

    //private val dcf = new DriverConnectionFactory( DriverManager.getDriver(dbUrl), dbUrl, props);
    private val dcf = new DriverManagerConnectionFactory(dbUrl, dbUser, dbPwd)
    private var stmtFac: KeyedObjectPoolFactory = _
    private val connPool = new GenericObjectPool()
    connPool.setMaxActive(maxConns)
    connPool.setTestOnBorrow(true)
    connPool.setMaxIdle(maxConns)
    connPool.setMinIdle(minConns)
    connPool.setMaxWait(maxWaitForConnMillis)
    connPool.setMinEvictableIdleTimeMillis(evictConnIfIdleMillis)
    connPool.setTimeBetweenEvictionRunsMillis(checkEvictionIntervalMillis)
    private val pcf = new PoolableConnectionFactory(dcf, connPool, stmtFac, null, false, true);
    private val dataSource = new PoolingDataSource(connPool)

    def open : Connection = dataSource.getConnection()

    def finz : Unit = {
        connPool.close
    }

}
