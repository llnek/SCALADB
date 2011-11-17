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

/**
 * @author kenl
 */
sealed abstract case class Vendor(val name: String)

/**
 * @author kenl
*/
sealed case class PostgreSQL(override val name: String = "postgresql") extends Vendor(name)

/**
 * @author kenl
*/
sealed case class MySQL(override val name: String = "mysql") extends Vendor(name)

/**
 * @author kenl
*/
sealed case class Oracle(override val name: String = "oracle") extends Vendor(name)

/**
 * @author kenl
*/
sealed case class MSSQL(override val name: String = "mssql") extends Vendor(name)

/**
 * @author kenl
*/
sealed case class H2(override val name: String = "h2") extends Vendor(name)

/**
 * @author kenl
*/
sealed case class HYPERSQL(override val name: String = "hypersql") extends Vendor(name)
sealed case class HSQLDB(override val name: String = "hsqldb") extends Vendor(name)


