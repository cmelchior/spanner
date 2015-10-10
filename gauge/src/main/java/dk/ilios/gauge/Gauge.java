package dk.ilios.gauge;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.TypeAdapters;

import org.threeten.bp.Instant;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Executors;

import dk.ilios.gauge.config.GaugeConfiguration;
import dk.ilios.gauge.config.CaliperConfigLoader;
import dk.ilios.gauge.config.InstrumentConfig;
import dk.ilios.gauge.config.InvalidConfigurationException;
import dk.ilios.gauge.exception.InvalidCommandException;
import dk.ilios.gauge.internal.AndroidExperimentSelector;
import dk.ilios.gauge.internal.GaugeRun;
import dk.ilios.gauge.internal.ExperimentSelector;
import dk.ilios.gauge.internal.ExperimentingGaugeRun;
import dk.ilios.gauge.internal.Instrument;
import dk.ilios.gauge.internal.InvalidBenchmarkException;
import dk.ilios.gauge.internal.benchmark.BenchmarkClass;
import dk.ilios.gauge.json.AnnotationExclusionStrategy;
import dk.ilios.gauge.json.InstantTypeAdapter;
import dk.ilios.gauge.log.AndroidStdOut;
import dk.ilios.gauge.log.StdOut;
import dk.ilios.gauge.model.Run;
import dk.ilios.gauge.options.GaugeOptions;
import dk.ilios.gauge.options.CommandLineOptions;
import dk.ilios.gauge.output.OutputFileDumper;
import dk.ilios.gauge.output.ResultProcessor;
import dk.ilios.gauge.util.NanoTimeGranularityTester;
import dk.ilios.gauge.util.ShortDuration;
import dk.ilios.gauge.util.Util;

/**
 * Main class for starting a benchmark
 */
public class Gauge {

    private static final String RUNNER_MAX_PARALLELISM_OPTION = "runner.maxParallelism";

    private final Class<?> benchmarkClass;
    private final Method method;
    private GaugeConfig benchmarkConfig;

    public static void runBenchmark(Class benchmarkClass, Method method) {
        new Gauge(benchmarkClass, method).start();
    }

    private Gauge(Class benchmarkClass, Method method) {
        this.benchmarkClass = benchmarkClass;
        this.method = method;
    }

    public void start() {
        try {

            BenchmarkClass benchmarkClassWrapper = new BenchmarkClass(benchmarkClass, method);
            benchmarkConfig = benchmarkClassWrapper.getConfiguration();

            // Setup components needed by the Runner
            GaugeOptions options = CommandLineOptions.parse(new String[]{benchmarkClass.getCanonicalName()});
            CaliperConfigLoader configLoader = new CaliperConfigLoader(options);
            GaugeConfiguration config = configLoader.loadOrCreate();

            ImmutableSet<Instrument> instruments = getInstruments(options, config);

            int poolSize = Integer.parseInt(config.properties().get(RUNNER_MAX_PARALLELISM_OPTION));
            ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(poolSize));

            StdOut stdOut = new AndroidStdOut();
            Run runInfo = new Run.Builder(UUID.randomUUID())
                    .label("Gauge benchmark test")
                    .startTime(Instant.now())
                    .configuration(config)
                    .options(options)
                    .build();

            ExperimentSelector experimentSelector = new AndroidExperimentSelector(benchmarkClassWrapper, method, instruments);

            GsonBuilder gsonBuilder = new GsonBuilder().setExclusionStrategies(new AnnotationExclusionStrategy());
            gsonBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(Instant.class, new InstantTypeAdapter()));
            OutputFileDumper dumper = new OutputFileDumper(runInfo, benchmarkClassWrapper, gsonBuilder.create(), benchmarkConfig);
            ImmutableSet<ResultProcessor> resultProcessors = ImmutableSet.<ResultProcessor>of(dumper);

            // Configure runner
            GaugeRun run = new ExperimentingGaugeRun(
                    options,
                    stdOut,
                    runInfo,
                    instruments,
                    resultProcessors,
                    experimentSelector,
                    executor
            );

            // Run benchmark
            run.run();

        } catch (InvalidBenchmarkException e) {
            throw new RuntimeException(e);
        } catch (InvalidCommandException e) {
            throw new RuntimeException(e);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public ImmutableSet<Instrument> getInstruments(GaugeOptions options, final GaugeConfiguration config) throws InvalidCommandException {
        ImmutableSet.Builder<Instrument> builder = ImmutableSet.builder();
        ImmutableSet<String> configuredInstruments = config.getConfiguredInstruments();
        for (final String instrumentName : options.instrumentNames()) {
            if (!configuredInstruments.contains(instrumentName)) {
                throw new InvalidCommandException("%s is not a configured instrument (%s). "
                        + "use --print-config to see the configured instruments.",
                        instrumentName, configuredInstruments);
            }
            final InstrumentConfig instrumentConfig = config.getInstrumentConfig(instrumentName);
//                Injector instrumentInjector = injector.createChildInjector(new AbstractModule() {
//                    @Override protected void configure() {
//                        bind(InstrumentConfig.class).toInstance(instrumentConfig);
//                    }
//
//                    @Provides @InstrumentOptions
//                    ImmutableMap<String, String> provideInstrumentOptions(InstrumentConfig config) {
//                        return config.options();
//                    }
//
//                    @Provides @InstrumentName String provideInstrumentName() {
//                        return instrumentName;
//                    }
//                });
            String className = instrumentConfig.className();
            try {
                Class<? extends Instrument> clazz = Util.lenientClassForName(className).asSubclass(Instrument.class);
                ShortDuration timerGranularity = new NanoTimeGranularityTester().testNanoTimeGranularity();
                Instrument instrument = (Instrument) clazz.getDeclaredConstructors()[0].newInstance(timerGranularity);
                instrument.setOptions(config.properties());
                builder.add(instrument);
            } catch (ClassNotFoundException e) {
                throw new InvalidCommandException("Cannot find instrument class '%s'", className);
//                } catch (ProvisionException e) {
//                    throw new InvalidInstrumentException("Could not create the instrument %s", className);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return builder.build();
    }
}
