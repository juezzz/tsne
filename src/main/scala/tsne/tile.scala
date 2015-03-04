package tsne

import _root_.breeze.generic.UFunc
import _root_.breeze.linalg.DenseMatrix
import _root_.breeze.linalg.DenseVector
import _root_.breeze.math.Semiring
import _root_.breeze.storage.Zero
import breeze.linalg._
import breeze.generic.UFunc
import breeze.math.Semiring
import breeze.storage.Zero
import scala.reflect.ClassTag

/**
 * @author Rakesh Chalasani <vnit.rakesh@gmail.com>
 */
object tile extends UFunc {

  // implementations

  implicit def tile_DV_Impl2[T:ClassTag:Semiring:Zero]: Impl2[DenseVector[T], Int, DenseVector[T]] = {
    new Impl2[DenseVector[T],Int,DenseVector[T]] {
      def apply(x:DenseVector[T], n:Int):DenseVector[T] = {
        // DenseVector is column-major, i.e., it can be considered a matrix of size (v.length x 1)
        val out = new DenseVector[T](new Array(x.length * n))
        var i = 0
        while(i < n) {
          out(i*x.length until (i+1)*x.length) := x
          i += 1
        }
        out
      }
    }
  }

  implicit def tile_DV_Impl3[T:ClassTag:Semiring:Zero](implicit impl2: Impl2[DenseVector[T], Int, DenseVector[T]]):
  Impl3[DenseVector[T], Int, Int, DenseMatrix[T]] =
    new Impl3[DenseVector[T],Int, Int, DenseMatrix[T]] {
      def apply(x: DenseVector[T], m: Int, n: Int): DenseMatrix[T] = {
        // DenseVector is column-major, i.e., it can be considered a matrix of size (v.length x 1)
        // So, first construct the tiled column.
        val col = impl2(x, m)
        // Now, replicate the col.
        val out = new DenseMatrix[T](x.length * m, n)
        var i = 0
        while (i < n) {
          out(::,i) := col
          i += 1
        }
        out
      }
    }

  implicit def tile_DM_Impl2[T:ClassTag:Semiring:Zero]: Impl2[DenseMatrix[T], Int, DenseMatrix[T]] =
    new Impl2[DenseMatrix[T], Int, DenseMatrix[T]] {
      def apply(x: DenseMatrix[T], m: Int):DenseMatrix[T] = {
        // Replicate each column 'm' times.
        val out = new DenseMatrix[T](x.rows * m, x.cols)
        var i = 0
        while(i < x.cols) {
          val column = x(::, i)
          var j = 0
          while(j < m) {
            out(j*x.rows until (j+1)*x.rows, i) := column
            j += 1
          }
          i += 1
        }
        out
      }
    }

  implicit def tile_DM_Impl3[T:ClassTag:Semiring:Zero](implicit impl2:Impl2[DenseMatrix[T],Int, DenseMatrix[T]]):
  Impl3[DenseMatrix[T], Int, Int, DenseMatrix[T]] =
    new Impl3[DenseMatrix[T], Int, Int, DenseMatrix[T]] {
      def apply(x: DenseMatrix[T], m: Int, n:Int):DenseMatrix[T] = {
        // First, replicate each column 'm' times.
        val columnTiledMatrix = impl2(x,m)
        // Nom, replicate the columnTiledMatrix 'n' times.
        val out = new DenseMatrix[T](x.rows * m, x.cols * n)
        var i = 0
        while(i < n) {
          out(::,i*x.cols until (i+1)*x.cols) := columnTiledMatrix
          i += 1
        }
        out
      }
    }
}

