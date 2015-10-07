//package dk.ilios.caliperx.runner;
//
//import android.test.InstrumentationTestRunner;
//import android.test.suitebuilder.TestMethod;
//import android.test.suitebuilder.TestSuiteBuilder;
//import android.util.Log;
//
//import com.android.internal.util.Predicate;
//
//import junit.framework.TestSuite;
//
//import java.lang.annotation.Annotation;
//
///**
// * An Instrumentation that can run benchmarks on an actual device or emulator.
// */
//public class CaliperXInstrumentationRunner extends InstrumentationTestRunner {
//
//    private String LOG_TAG = CaliperXInstrumentationRunner.class.getSimpleName();
//
//    @Override
//    public TestSuite getAllTests() {
//        TestSuiteBuilder testSuiteBuilder = new TestSuiteBuilder(getClass().getName(),
//                getTargetContext().getClassLoader());
//
//        Predicate<TestMethod> testSizePredicate = null;
//        Predicate<TestMethod> testAnnotationPredicate = null;
//        Predicate<TestMethod> testNotAnnotationPredicate = null;
//
//        if (testSizePredicate != null) {
//            testSuiteBuilder.addRequirements(testSizePredicate);
//        }
//        if (testAnnotationPredicate != null) {
//            testSuiteBuilder.addRequirements(testAnnotationPredicate);
//        }
//        if (testNotAnnotationPredicate != null) {
//            testSuiteBuilder.addRequirements(testNotAnnotationPredicate);
//        }
//
//        if (testClassesArg == null) {
//            if (mPackageOfTests != null) {
//                testSuiteBuilder.includePackages(mPackageOfTests);
//            } else {
//                TestSuite testSuite = getTestSuite();
//                if (testSuite != null) {
//                    testSuiteBuilder.addTestSuite(testSuite);
//                } else {
//                    // no package or class bundle arguments were supplied, and no test suite
//                    // provided so add all tests in application
//                    testSuiteBuilder.includePackages("");
//                }
//            }
//        } else {
//            parseTestClasses(testClassesArg, testSuiteBuilder);
//        }
//
//        testSuiteBuilder.addRequirements(getBuilderRequirements());
//    }
//
//
//    /**
//     * Returns the test predicate object, corresponding to the annotation class value provided via
//     * the {@link ARGUMENT_ANNOTATION} argument.
//     *
//     * @return the predicate or <code>null</code>
//     */
//    private Predicate<TestMethod> getAnnotationPredicate(String annotationClassName) {
//        Class<? extends Annotation> annotationClass = getAnnotationClass(annotationClassName);
//        if (annotationClass != null) {
//            return new HasAnnotation(annotationClass);
//        }
//        return null;
//
//    }
//
//    /**
//     * Helper method to return the annotation class with specified name
//     *
//     * @param annotationClassName the fully qualified name of the class
//     * @return the annotation class or <code>null</code>
//     */
//    private Class<? extends Annotation> getAnnotationClass(String annotationClassName) {
//        if (annotationClassName == null) {
//            return null;
//        }
//        try {
//            Class<?> annotationClass = Class.forName(annotationClassName);
//            if (annotationClass.isAnnotation()) {
//                return (Class<? extends Annotation>) annotationClass;
//            } else {
//                Log.e(LOG_TAG, String.format("Provided annotation value %s is not an Annotation",
//                        annotationClassName));
//            }
//        } catch (ClassNotFoundException e) {
//            Log.e(LOG_TAG, String.format("Could not find class for specified annotation %s",
//                    annotationClassName));
//        }
//        return null;
//    }
//}
