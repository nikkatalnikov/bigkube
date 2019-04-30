package it_tests

import org.apache.spark.sql.avro._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{DecimalType, IntegerType, StructType}
import org.apache.spark.sql.{Row, SparkSession, _}

case class TestClass(field1: Int, field2: Int)(implicit spark: SparkSession) {
  private val schema = new StructType()
    .add("field1", IntegerType)
    .add("field2", DecimalType(38, 2))

  def toAvroDF: DataFrame = {
    val df = spark.createDataFrame(
      spark.sparkContext.parallelize(Seq(Row(field1, BigDecimal(field2, 2)))),
      StructType(schema)
    )

    df.select(to_avro(struct(df.columns.map(column):_*)).alias("value"))
  }
}
