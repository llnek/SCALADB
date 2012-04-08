# About Stratum
A simple Scala ORM.

# Supported Platforms
## Scala
* 2.9.1

## Database
* Microsoft SQL Server
* MySQL
* Oracle
* PostgreSQL
* HSQLDB
* H2

# Connect to a Datasource
<pre>
import com.zotoh.scaladb._
//fac= new PoolableDBFactory(4,10)
fac= new SimpleDBFactory 
db= ScalaDB(fac)("org.h2.Driver","jdbc:h2:/tmp/h2db", "sa", "secret")
dbComplexr=db.newCompositeSQLProcessor // manual commit
dbSimpler= db.newSimpleSQLProcessor // auto commit
</pre>

# Define a Table Abstraction
<pre>
class TestTable extends SRecord {
    override def getSchemaFactory() = TestTable
}
object TestTable extends SRecordFactory {
  override def getUpdatableCols = {
      columns - "CID" // all columns except the prim-key-col
  }
  override val columns =
      Set("CID", "NAME", "AGE", "WEIGHT", "BIRTHDATE", "BIRTHTSTAMP", "BIGDEC", "BIGINT", "DESC", "BIN", "CLOBY", "BLOBY") // all the column ids
  override def getTableName = {
      "TESTTABLE"
  }
  override def getPrimaryKeys = {
      Set("CID") // add more if composite prim-key
  }
  override def createEmptyRecord() = {
      new TestTable
  }
}
</pre>

# Create a Record
<pre>
val obj = TestTable.create(Map( ("name" -> "joeb"),
  ("age", 46), ("weight", 150.74), ... ))
dbComplexr.execWith { tx =>  tx.insert(obj)   }
</pre>

# Read a Record
<pre>
val lst : Seq[SRecord] = dbSimpler.findAll(TestTable)
val rec = lst.head
val id = rec.getVal("CID").getOrElse(0L).asInstanceOf[Long]
</pre>

# Update a Record
<pre>
dbComplexr.execWith { tx =>
    val lst = tx.findSome(TestTable, Map("cid" -> 100L))
    val rec = lst.head
    rec.setVal("age", Some(81))
    rec.setVal("weight", Some(2000.23))
    tx.update(rec, Set("age", "weight"))
}
</pre>

# Delete a Record
<pre>
val lst : Seq[SRecord] = dbSimpler.findAll(TestTable)
val rec = lst.head
dbSimpler.delete(rec)
</pre>


# Contact
For questions and bug reports, please email [contactzotoh@gmail.com](mailto:contactzotoh@gmail.com)


# Latest binary
Download the latest bundle [1.0.0](http://www.zotoh.com/packages/scaladb/stable/1.0.0/scaladb-1.0.0.zip)





