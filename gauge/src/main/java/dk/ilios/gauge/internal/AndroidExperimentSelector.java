package dk.ilios.gauge.internal;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Ordering;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.ilios.gauge.internal.benchmark.BenchmarkClass;

/**
 * Experiment selector for Android.
 * Based on the annotations in the benchmark class this class creates the Scenarios that needs to run.
 *
 * Scenario = Experiment?
 */
public class AndroidExperimentSelector implements ExperimentSelector {

    private final Method method;
    private final ImmutableSet<Instrument> instruments;
    private final BenchmarkClass benchmarkClass;

    public AndroidExperimentSelector(BenchmarkClass benchmarkClass, Method method, ImmutableSet<Instrument> instruments) {
        this.method = method;
        this.instruments = instruments;
        this.benchmarkClass = benchmarkClass;
    }

    @Override
    public BenchmarkClass benchmarkClass() {
        return benchmarkClass;
    }

    @Override
    public ImmutableSet<Instrument> instruments() {
        return instruments;
    }

    @Override
    public ImmutableSetMultimap<String, String> userParameters() {
        // TODO Figure out which user parameters to set
        ArrayListMultimap<String, String> multimap = ArrayListMultimap.create();
        return new ImmutableSetMultimap.Builder<String, String>()
                .orderKeysBy(Ordering.natural())
                .putAll(multimap)
                .build();
    }

    @Override
    public ImmutableSet<Experiment> selectExperiments() {
        try {
            Map<String, String> userParameters = new HashMap<>(); // TODO Wonder what should go here?
//            userParameters.put(CommonInstrumentOptions.MEASUREMENTS.getKey(), "9");
//            userParameters.put(CommonInstrumentOptions.WARMUP.getKey(), "true");
//            userParameters.put(CommonInstrumentOptions.MAX_WARMUP_WALL_TIME.getKey(), "10m");

            List<Experiment> experiments = new ArrayList<>();
            for (Instrument instrument : instruments) {
                Instrument.Instrumentation instrumentation = instrument.createInstrumentation(method);
                experiments.add(new Experiment(instrumentation, userParameters));
            }
            return ImmutableSet.copyOf(experiments);
        } catch (InvalidBenchmarkException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String selectionType() {
        return null;
    }

    protected static <K, V> ImmutableMap<K, V> zip(Set<K> keys, Collection<V> values) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();

        Iterator<K> keyIterator = keys.iterator();
        Iterator<V> valueIterator = values.iterator();

        while (keyIterator.hasNext() && valueIterator.hasNext()) {
            builder.put(keyIterator.next(), valueIterator.next());
        }

        if (keyIterator.hasNext() || valueIterator.hasNext()) {
            throw new AssertionError(); // I really screwed up, then.
        }
        return builder.build();
    }

}
