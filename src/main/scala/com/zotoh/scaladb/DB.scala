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

import java.sql.{SQLException, Connection}

/**
 * @author kenl
 */
object ScalaDB {

    def apply( fac : DBFactory) : DBFactory = {
        fac
    }

}

/**
 * @author kenl
 */
trait DBFactory {

    def apply(dbDriver: String, dbUrl: String, dbUser: String, dbPwd: String): DB
}

/**
 * @author kenl
 */
trait DB extends Loggable {

    protected val dbDriver : String
    protected val dbUrl : String
    protected val dbUser : String
    protected val dbPwd : String

    def newCompositeSQLProcessor : CompositeSQLr = {
        new CompositeSQLr(this)
    }

    def newSimpleSQLProcessor : SimpleSQLr = {
        new SimpleSQLr(this)
    }

    def close(c: Connection): Unit = {
        try {
            c.close()
        }
        catch {
            case e : Exception => error("", e)
        }
    }

    def open : Connection

    def finz : Unit

    override def toString : String = {
        "Database Driver: " + dbDriver + " | " +
        "Database Url: " + dbUrl + " | " +
        "Database User: " + dbUser
    }

}
