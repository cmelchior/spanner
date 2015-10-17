package dk.ilios.gauge;

import android.util.Log;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class SecondOppinionBenchmark {

    // Public test parameters (value chosen and injected by Experiment)
    public String value;

    // Private fields used by benchmark methods
    private Class testClass;

    public void before(String value) {
        try {
            testClass = Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void runTests() {
        String[] values =  {"java.util.Date", "java.lang.Object"};

        for (String clazz : values) {
            before(clazz);

            long start = 0;
            long end = 0;
            int reps = 1000000;
            start = System.nanoTime();
            equalsTo(reps);
            end = System.nanoTime();
            Log.e("Benchmark", String.format("equalsTo(%s): %.2f", clazz, ((end - start) / (double) reps)));

            start = System.nanoTime();
            directComparison(reps);
            end = System.nanoTime();
            Log.e("Benchmark", String.format("directComparison(%s): %.2f", clazz, ((end - start) / (double) reps)));
            Collections.emptyList();
            start = System.nanoTime();
            instanceOf(reps);
            end = System.nanoTime();
            Log.e("Benchmark", String.format("instanceOf(%s): %.2f", clazz, ((end - start)/(double)reps)));
        }
    }

    public boolean instanceOf(int reps) {
        boolean result = false;
        for (int i = 0; i < reps; i++) {
            result = (testClass instanceof Object);
        }
        return result;
    }

    public boolean directComparison(int reps) {
        boolean result = false;
        for (int i = 0; i < reps; i++) {
            result = testClass == Object.class;
        }
        return result;
    }

    public boolean equalsTo(int reps) {
        boolean result = false;
        for (int i = 0; i < reps; i++) {
            result = testClass.equals(Object.class);
        }
        return result;
    }
}
