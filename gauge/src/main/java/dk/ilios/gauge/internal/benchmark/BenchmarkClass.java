/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.ilios.gauge.internal.benchmark;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagateIfInstanceOf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.ilios.gauge.AfterExperiment;
import dk.ilios.gauge.BeforeExperiment;
import dk.ilios.gauge.BenchmarkConfiguration;
import dk.ilios.gauge.GaugeConfig;
import dk.ilios.gauge.Param;
import dk.ilios.gauge.api.VmOptions;
import dk.ilios.gauge.exception.InvalidCommandException;
import dk.ilios.gauge.internal.InvalidBenchmarkException;
import dk.ilios.gauge.exception.SkipThisScenarioException;
import dk.ilios.gauge.exception.UserCodeException;
import dk.ilios.gauge.util.Reflection;

/**
 * An instance of this type represents a user-provided class benchmark class. It manages creating, setting up and
 * destroying instances of that class.
 */
public final class BenchmarkClass {

    private static final Logger logger = Logger.getLogger(BenchmarkClass.class.getName());
    private final Class<?> classReference;
    private final Object classInstance;
    private final Method method;
    private final ParameterSet userParameters;

    public BenchmarkClass(Class<?> benchmarkClass, Method method) throws InvalidBenchmarkException {
        this.classReference = checkNotNull(benchmarkClass);
        this.method = checkNotNull(method);

        if (!benchmarkClass.getSuperclass().equals(Object.class)) {
            throw new InvalidBenchmarkException(
                    "%s must not extend any class other than %s. Prefer composition.",
                    benchmarkClass, Object.class);
        }

        if (Modifier.isAbstract(benchmarkClass.getModifiers())) {
            throw new InvalidBenchmarkException("Class '%s' must not be abstract", benchmarkClass);
        }

        this.userParameters = ParameterSet.create(benchmarkClass, Param.class);
        try {
            classInstance = benchmarkClass.newInstance();
        } catch (InstantiationException  e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the simple name of the class that is being benchmarked.
     */
    public String getSimpleName() {
        return classReference.getSimpleName();
    }

    /**
     * Returns a instance of the Benchmark class.
     */
    public Object getInstance() {
        return classInstance;
    }

    /**
     * Returns the configuration for this class or the default configuration if no configuration is provided.
     */
    public GaugeConfig getConfiguration() {
        for (Field field : classReference.getDeclaredFields()) {
            if (field.isAnnotationPresent(BenchmarkConfiguration.class)) {
                try {
                    field.setAccessible(true);
                    return (GaugeConfig) field.get(classInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return new GaugeConfig.Builder().build();
    }


    /**
     * Returns the method should should be benchmarked.
     */
    public Method getMethod() {
        return method;
    }

    ImmutableSet<Method> beforeExperimentMethods() {
        return Reflection.getAnnotatedMethods(classReference, BeforeExperiment.class);
    }

    ImmutableSet<Method> afterExperimentMethods() {
        return Reflection.getAnnotatedMethods(classReference, AfterExperiment.class);
    }

    public ParameterSet userParameters() {
        return userParameters;
    }

    // TODO(gak): use these methods in the worker as well
    public void setUpBenchmark(Object benchmarkInstance) throws UserCodeException {
        boolean setupSuccess = false;
        try {
            callSetUp(benchmarkInstance);
            setupSuccess = true;
        } finally {
            // If setUp fails, we should call tearDown. If this method throws an exception, we
            // need to call tearDown from here, because no one else has the reference to the
            // Benchmark.
            if (!setupSuccess) {
                try {
                    callTearDown(benchmarkInstance);
                } catch (UserCodeException e) {
                    // The exception thrown during setUp shouldn't be lost, as it's probably more
                    // important to the user.
                    logger.log(
                            Level.INFO,
                            "in @AfterExperiment methods called because @BeforeExperiment methods failed",
                            e);
                }
            }
        }
    }

    public void cleanup(Object benchmark) throws UserCodeException {
        callTearDown(benchmark);
    }

    public String name() {
        return classReference.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BenchmarkClass) {
            BenchmarkClass that = (BenchmarkClass) obj;
            return this.classReference.equals(that.classReference);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(classReference);
    }

    @Override
    public String toString() {
        return name();
    }

    private void callSetUp(Object benchmark) throws UserCodeException {
        for (Method method : beforeExperimentMethods()) {
            try {
                method.invoke(benchmark);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                propagateIfInstanceOf(e.getCause(), SkipThisScenarioException.class);
                throw new UserCodeException(
                        "Exception thrown from a @BeforeExperiment method", e.getCause());
            }
        }
    }

    private void callTearDown(Object benchmark) throws UserCodeException {
        for (Method method : afterExperimentMethods()) {
            try {
                method.invoke(benchmark);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            } catch (InvocationTargetException e) {
                propagateIfInstanceOf(e.getCause(), SkipThisScenarioException.class);
                throw new UserCodeException(
                        "Exception thrown from an @AfterExperiment method", e.getCause());
            }
        }
    }

    void validateParameters(ImmutableSetMultimap<String, String> parameters)
            throws InvalidCommandException {
        for (String paramName : parameters.keySet()) {
            Parameter parameter = userParameters.get(paramName);
            if (parameter == null) {
                throw new InvalidCommandException("unrecognized parameter: " + paramName);
            }
            try {
                parameter.validate(parameters.get(paramName));
            } catch (InvalidBenchmarkException e) {
                // TODO(kevinb): this is weird.
                throw new InvalidCommandException(e.getMessage());
            }
        }
    }
}
