package rx
import org.scalatest._
import util.{Failure, Success}
import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global
class BasicTests extends FreeSpec{


    "sig tests" - {
      "basic" - {
        "signal Hello World" in {
          val a = Var(1); val b = Var(2)
          val c = Sig{ a() + b() }
          assert(c() === 3)
          a() = 4
          assert(c() === 6)

        }
        "long chain" in {
          val a = Var(1) // 3

          val b = Var(2) // 2

          val c = Sig{ a() + b() } // 5
          val d = Sig{ c() * 5 } // 25
          val e = Sig{ c() + 4 } // 9
          val f = Sig{ d() + e() + 4 } // 25 + 9 + 4 =

          assert(f() === 26)
          a() = 3
          assert(f() === 38)
        }
      }
      "language features" - {
        "pattern matching" in {
          val a = Var(1); val b = Var(2)
          val c = Sig{
            a() match{
              case 0 => b()
              case x => x
            }
          }
          assert(c() === 1)
          a() = 0
          assert(c() === 2)
        }
        "implicit conversions" in {
          val a = Var(1); val b = Var(2)
          val c = Sig{
            val t1 = a() + " and " + b()
            val t2 = a() to b()
            t1 + t2
          }
        }
        "use in by name parameters" in {
          val a = Var(1);

          val c = Sig{

            Some(1).getOrElse(a())
          }
        }
      }

    }
    "obs tests" - {
      "obs Hello World" in {
        val a = Var(1)
        var s = 0
        val o = Obs(a){
          s = s + 1
        }
        a() = 2
        assert(s === 1)
      }
      "obs simple example" in {
        val a = Var(1)
        val b = Sig{ a() * 2 }
        val c = Sig{ a() + 1 }
        val d = Sig{ b() + c() }
        var bS = 0;     val bO = Obs(b){ bS += 1 }
        var cS = 0;     val cO = Obs(c){ cS += 1 }
        var dS = 0;     val dO = Obs(d){ dS += 1 }
        println("\n1")
        a() = 2
        println("1\n")
        assert(bS === 1);   assert(cS === 1);   assert(dS === 1)
        println("\n2")
        a() = 1
        println("2\n")
        assert(bS === 2);   assert(cS === 2);   assert(dS === 2)
      }
    }

    "error handling" - {
      "simple catch" in {
        val a = Var(1)
        val b = Sig{ 1 / a() }
        assert(b.toTry == Success(1))
        a() = 0
        assert(b.toTry match{ case Failure(_) => true; case _ => false} )
      }
      "long chain" in {
        val a = Var(1)

        val b = Var(2)

        val c = Sig{ a() / b() }
        val d = Sig{ a() * 5 }
        val e = Sig{ 5 / b() }
        val f = Sig{ a() + b() + 2 }
        val g = Sig{ f() + c() }

        assert(c.toTry match {case Success(_) => true; case _ => false})
        assert(d.toTry match {case Success(_) => true; case _ => false})
        assert(e.toTry match {case Success(_) => true; case _ => false})
        assert(f.toTry match {case Success(_) => true; case _ => false})
        assert(g.toTry match {case Success(_) => true; case _ => false})
        b() = 0
        assert(c.toTry match {case Failure(_) => true; case _ => false})
        assert(d.toTry match {case Success(_) => true; case _ => false})
        assert(e.toTry match {case Failure(_) => true; case _ => false})
        assert(f.toTry match {case Success(_) => true; case _ => false})
        assert(g.toTry match {case Failure(_) => true; case _ => false})
      }
    }
    "nested Sigs" - {
      val a = Var(1)
      val b = Sig{
        Sig{a()} -> Sig{math.random}
      }
      val r = b()._2()
      a() = 2
      assert(b()._2() === r)
    }


}