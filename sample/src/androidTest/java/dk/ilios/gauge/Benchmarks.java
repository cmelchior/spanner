package dk.ilios.gauge;

import android.os.Environment;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.runner.RunWith;

import dk.ilios.caliperx.caliperx.test.BuildConfig;
import dk.ilios.gauge.config.GaugeConfiguration;
import dk.ilios.gauge.internal.benchmark.BenchmarkClass;
import dk.ilios.gauge.runner.GaugeRunner;

//@RunWith(CaliperXRunner.class)
@RunWith(GaugeRunner.class)
public class Benchmarks {

    @BenchmarkConfiguration
    GaugeConfig configuration = new GaugeConfig.Builder()
            .resultsFolder(InstrumentationRegistry.getTargetContext().getFilesDir())
            .uploadResults()
            .apiKey(BuildConfig.CALIPER_API_KEY)
            .build();

    private Object obj1 = new Object();
    private Object obj2 = new Object();
    private Class<?> clazz = obj1.getClass();

    @Benchmark
    public boolean instanceOf(int reps) {
        boolean result = false;
        for (int i = 0; i < reps; i++) {
             result = (obj1 instanceof Object);
        }
        return result;
    }

    @Benchmark
    public boolean compareClass(int reps) {
        boolean result = false;
        for (int i = 0; i < reps; i++) {
            result = clazz == Benchmarks.class;
        }
        return result;
    }
}
