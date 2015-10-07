package dk.ilios.gauge.runner;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import dk.ilios.gauge.Benchmark;
import dk.ilios.gauge.Gauge;

/**
 * Runner for handling the individual Benchmarks.
 * If a new benchmark is outside it the allowed variance, it will fail.
 */
public class GaugeRunner extends Runner {

    private TestClass testClass;
    private List<Method> testMethods = new ArrayList();

    public GaugeRunner(Class clazz) {
        testClass = new TestClass(clazz);

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
        Description spec = Description.createSuiteDescription(this.testClass.getName(),
                this.testClass.getJavaClass().getAnnotations());
        return spec;
    }

    @Override
    public void run(RunNotifier runNotifier) {
        for (int i = 0; i < testMethods.size(); i++) {
            Method method = testMethods.get(i);
            Description spec = Description.createTestDescription(method.getClass(), method.getName());
            runNotifier.fireTestStarted(spec);
            Gauge.runBenchmark(testClass.getJavaClass(), method);
            runNotifier.fireTestFinished(spec);
        }
    }
}
