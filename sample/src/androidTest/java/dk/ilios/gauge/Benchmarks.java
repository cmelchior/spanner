package dk.ilios.gauge;

import android.os.SystemClock;

import org.junit.runner.RunWith;

import dk.ilios.gauge.runner.GaugeRunner;

//@RunWith(CaliperXRunner.class)
@RunWith(GaugeRunner.class)
public class Benchmarks {

    @Benchmark
    public void foo(int repititions) {
        for (int i = 0; i < repititions; i++) {
            int foo = 42 + 42;
        }
    }

//    @Benchmark
//    public void foo() {
//        SystemClock.sleep(50);
//        int foo = 42 + 42;
//    }
//
}
