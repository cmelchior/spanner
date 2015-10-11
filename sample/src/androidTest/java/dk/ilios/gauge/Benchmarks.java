package dk.ilios.gauge;

import android.support.test.InstrumentationRegistry;

import org.junit.runner.RunWith;

import dk.ilios.caliperx.caliperx.test.BuildConfig;
import dk.ilios.gauge.junit.GaugeRunner;

@RunWith(GaugeRunner.class)
public class Benchmarks {

    @BenchmarkConfiguration
    GaugeConfig configuration = new GaugeConfig.Builder()
            .resultsFolder(InstrumentationRegistry.getTargetContext().getFilesDir())
            .uploadResults()
            .apiKey(BuildConfig.CALIPER_API_KEY)
            .build();

    // Public test parameters (value chosen and injected by Experiment)
    @Param(value = {"java.util.Date", "java.lang.Object"})
    public String value;

    // Private fields used by benchmark methods
    private Class testClass;

    @BeforeExperiment
    public void before() {
        try {
            testClass = Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterExperiment
    public void after() {

    }

    @Benchmark
    public boolean instanceOf(int reps) {
        boolean result = false;
        for (int i = 0; i < reps; i++) {
             result = (testClass instanceof Object);
        }
        return result;
    }

    @Benchmark
    public boolean directComparison(int reps) {
        boolean result = false;
        for (int i = 0; i < reps; i++) {
            result = testClass == Object.class;
        }
        return result;
    }

    @Benchmark
    public boolean equalsTo(int reps) {
        boolean result = false;
        for (int i = 0; i < reps; i++) {
            result = testClass.equals(Object.class);
        }
        return result;
    }
}
