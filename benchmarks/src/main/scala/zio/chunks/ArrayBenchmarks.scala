package zio.chunks

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class ArrayBenchmarks {

  var array: Array[Int] = _

  @Param(Array("100", "1000", "10000"))
  var size: Int = _

  @Setup(Level.Trial)
  def setup() =
    array = Array.fill(0)(size)

  @Benchmark
  def fold(): Int = array.sum

  @Benchmark
  def map(): Array[Int] = array.map(_ * 2)

  @Benchmark
  def mapOptimized(): Array[Int] = {
    val mapped = Array.ofDim[Int](size)
    var i      = 0

    while (i < size) {
      mapped(i) = array(i) * 2
      i += 1
    }

    mapped
  }
  @Benchmark
  def find(): Option[Int] = array.find(_ > 2)

  def findOptimized(): Option[Int] = {
    var i   = 0
    val len = array.length

    while (i < len) {
      if (array(i) > 2) return Some(array(i))
      i = i + 1
    }

    None
  }

  @Benchmark
  def flatMap(): Array[Int] = array.flatMap(n => Array(n + 2))

  @Benchmark
  def flatMapOptimized(): Array[Int] = {
    var mappings = List.empty[Array[Int]]
    var i        = 0
    var total    = 0

    while (i < size) {

      val mapped = Array(array(i) * 2)

      mappings ::= mapped
      total += mapped.length
      i += 1
    }

    val dest = Array.ofDim[Int](total)

    var n  = total
    val it = mappings.iterator
    while (it.hasNext) {
      val mapped = it.next

      n -= mapped.length

      Array.copy(mapped, 0, dest, n, mapped.length)
    }

    dest
  }

}
