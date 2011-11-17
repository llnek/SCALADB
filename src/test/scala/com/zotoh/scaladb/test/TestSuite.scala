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

import org.scalatest._
import java.text.{ParsePosition=>JPPos, SimpleDateFormat => JSDT }
import java.io.{ByteArrayInputStream, StringReader}
import javax.sql.rowset.serial.{SerialClob, SerialBlob}
import java.math.{BigDecimal => JBigDec, BigInteger => JBigInt}
import com.zotoh.scaladb.{Transaction, SRecord}
import org.scalatest.Assertions._

/**
 * @author kenl
 */
class CRUDSuite extends FunSuite with BeforeAndAfterEach with BeforeAndAfterAll with TestExhibit {

    private val BDAYTS= ("yyyy/MM/dd'T'HH:mm:ss.SSSZ", "2011/01/31T21:22:23.666GMT")
    private val BDAY= ("yyyy/MM/ddZ", "2011/01/31GMT")
    private val DESC="this is a vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvveryvery long string"
    private val WEIGHT= 75.65
    private val AGE=10
    private val NAME="object-one"
    private val SBINLEN= 8888
    private val LBINLEN= 1024*1024*5

    private var cid= 0L

    override def beforeAll(configMap: Map[String, Any]) {
        iniz
    }

    override def afterAll(configMap: Map[String, Any]) {
        finz
    }

    override def beforeEach() {
    }

    override def afterEach() {
    }

    private def createLongDesc = {
        val r = 0 until 1024
        r.foldLeft(new StringBuilder) ( ( buf, _) =>  buf.append(DESC) ) .toString
    }

    test("1. create object") {
        val longstr = createLongDesc
        val bits= longstr.getBytes("UTF-8")
        val chs = longstr.toCharArray
        val obj = TestTable.create(Map( ("name" ->NAME),
        ("age", AGE), ("weight", WEIGHT),
            ("birthdate", new JSDT(BDAY._1).parse(BDAY._2, new JPPos(0))),
        ("birthtstamp",new JSDT(BDAYTS._1).parse(BDAYTS._2, new JPPos(0))),
        ("bigdec", new BigDecimal( new JBigDec(999.888))),
        ("bigint", new BigInt( new JBigInt("898989"))),
        ("desc", new StringReader(longstr)),
        ("bin", new ByteArrayInputStream(  bits)),
        ("cloby", new SerialClob(chs)   ),
        ("bloby", new SerialBlob(bits)   )))



        var c= 0
        dbComplexr.execWith { tx =>  c=tx.insert(obj)   }
        assert(c===1, "insert failed")
        info("create object - OK")
    }

    test("2. read object") {
        val lst : Seq[SRecord] = dbSimpler.findAll(TestTable)
        assert(lst.length === 1, "read failed")
        val rec = lst.head
        val id = rec.getVal("CID").getOrElse(0L).asInstanceOf[Long]
        assert(id === 100L, "object id is not 100")
        info("read object - OK")
    }

    test("3. update object") {
        dbComplexr.execWith { tx =>
            val lst = tx.findSome(TestTable, Map("cid" -> 100L))
            assert(lst.length === 1, "read failed")
            val rec = lst.head
            rec.setVal("age", Some(81))
            rec.setVal("weight", Some(2000.23))
            tx.update(rec, Set("age", "weight"))
        }
        info("update object - OK")
    }

    test("4. read object") {
        val lst : Seq[SRecord] = dbSimpler.findAll(TestTable)
        assert(lst.length === 1, "post update read failed")
        val rec = lst.head
        val id = rec.getVal("cid").getOrElse(0L).asInstanceOf[Long]
        assert(id === 100L, "object id is not 100")
        val age = rec.getVal("age").getOrElse(0).asInstanceOf[Int]
        assert(age === 81, "age was not updated")
        val wt = rec.getVal("weight").getOrElse(0.0).asInstanceOf[Double]
        assert(wt === 2000.23, "weight was not updated")
        info("read object post update - OK")
    }

    test("5. delete object") {
        val lst : Seq[SRecord] = dbSimpler.findAll(TestTable)
        assert(lst.length === 1, "read failed")
        val rec = lst.head
        dbSimpler.delete(rec)
        info("delete object - OK")
    }

    test("6. read object") {
        val lst : Seq[SRecord] = dbSimpler.findAll(TestTable)
        assert(lst.length === 0, "post delete read failed")
        info("read object post delete - OK")
    }


}

