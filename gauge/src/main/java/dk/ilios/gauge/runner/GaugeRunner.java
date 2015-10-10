package dk.ilios.gauge.runner;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import dk.ilios.gauge.Benchmark;
import dk.ilios.gauge.BenchmarkConfiguration;
import dk.ilios.gauge.Gauge;
import dk.ilios.gauge.GaugeConfig;
import dk.ilios.gauge.model.Run;

/**
 * Runner for handling the individual Benchmarks.
 * If a new benchmark is outside it the allowed variance, it will fail.
 */
public class GaugeRunner extends Runner {

    private Object testInstance;
    private TestClass testClass;
    private List<Method> testMethods = new ArrayList();
    private GaugeConfig benchmarkConfiguration = new GaugeConfig.Builder().build();

    public GaugeRunner(Class clazz) {
        testClass = new TestClass(clazz);
        try {
            testInstance = testClass.getJavaClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Setup config (if any)
        List<FrameworkField> fields = testClass.getAnnotatedFields(BenchmarkConfiguration.class);
        if (fields.size() > 1) {
            throw new IllegalStateException("Only one @BenchmarkConfiguration allowed");
        }
        if (fields.size() > 0) {
            try {
                FrameworkField field = fields.get(0);
                if (!field.getType().equals(GaugeConfig.class)) {
                    throw new IllegalArgumentException("@BenchmarkConfiguration can only be set on " +
                            "GaugeConfiguration fields.");
                }
                benchmarkConfiguration = (GaugeConfig) field.get(testInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        Method[] classMethods = clazz.getDeclaredMethods();
        for (int i = 0; i < classMethods.length; i++) {
            Method classMethod = classMethods[i];
            Class retClass = classMethod.getReturnType();
            int length = classMethod.getParameterTypes().length;
            int modifiers = classMethod.getModifiers();
            if (retClass == null || Modifier.isStatic(modifiers)
                    || !Modifier.isPublic(modifiers) || Modifier.isInterface(modifiers)
                    || Modifier.isAbstract(modifiers)) {
                continue;
            }
            String methodName = classMethod.getName();
            if (classMethod.getAnnotation(Benchmark.class) != null) {
                testMethods.add(classMethod);
            }
            if (classMethod.getAnnotation(Ignore.class) != null) {
                testMethods.remove(classMethod);
            }
        }
    }
    @Override
    public Description getDescription() {
        Description spec = Description.createSuiteDescription(
                this.testClass.getName(),
                this.testClass.getJavaClass().getAnnotations()
        );
        return spec;
    }

    @Override
    public void run(RunNotifier runNotifier) {
        for (int i = 0; i < testMethods.size(); i++) {
            Method method = testMethods.get(i);
            Description spec = Description.createTestDescription(method.getClass(), method.getName());
            runNotifier.fireTestStarted(spec);
            try {
                Gauge.runBenchmark(testClass.getJavaClass(), method);
            } catch (Throwable e) {
                runNotifier.fireTestFailure(new Failure(spec, e));
            }
            runNotifier.fireTestFinished(spec);
        }
    }

}
