package dk.ilios.gauge.internal.trial;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import dk.ilios.gauge.bridge.ShouldContinueMessage;
import dk.ilios.gauge.bridge.StartMeasurementLogMessage;
import dk.ilios.gauge.bridge.StopMeasurementLogMessage;
import dk.ilios.gauge.internal.MeasurementCollectingVisitor;
import dk.ilios.gauge.internal.benchmark.BenchmarkClass;
import dk.ilios.gauge.model.Trial;
import dk.ilios.gauge.options.GaugeOptions;
import dk.ilios.gauge.util.ShortDuration;
import dk.ilios.gauge.worker.Worker;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A Trial that is running on the thread it was started on.
 * This can be useful when e.g. running tests on the Android UI thread.
 * <p/>
 * Warning: This will block the executing thread until the trial is complete.
 */
public class AndroidUnitTestTrial implements Callable<Trial.Result> {

    private final Trial trial;
    private final GaugeOptions options;
    private final MeasurementCollectingVisitor measurementCollectingVisitor;
    private final TrialOutputLogger trialOutput;
    private final Stopwatch trialStopwatch = Stopwatch.createUnstarted();
    private final BenchmarkClass benchmark;

    public AndroidUnitTestTrial(
            Trial trial,
            BenchmarkClass benchmarkClass,
            MeasurementCollectingVisitor measurementCollectingVisitor,
            GaugeOptions options,
            TrialOutputLogger trialOutput) {
        this.trial = trial;
        this.options = options;
        this.measurementCollectingVisitor = measurementCollectingVisitor;
        this.trialOutput = trialOutput;
        this.benchmark = benchmarkClass;
    }

    // TODO Timeout not possible when running on the same thread
    // TODO Error checking has been removed. Any crash in benchmark code will crash everything.
    @Override
    public Trial.Result call() throws Exception {


//        public MacrobenchmarkWorker(Object benchmark, Method method, Ticker ticker, Map<String, String> workerOptions) {
//            super(benchmark, method);
//            this.stopwatch = Stopwatch.createUnstarted(ticker);
//            this.beforeRepMethods = getAnnotatedMethods(benchmark.getClass(), BeforeRep.class);
//            this.afterRepMethods = getAnnotatedMethods(benchmark.getClass(), AfterRep.class);
//            this.gcBeforeEach = Boolean.parseBoolean(workerOptions.get("gcBeforeEach"));
//        }

        Worker worker = (Worker) trial.experiment().instrumentation().workerClass().getDeclaredConstructors()[0].newInstance(
                benchmark,
                Ticker.systemTicker(),
                trial.experiment().instrumentation().workerOptions()
        );

//        log.notifyWorkerStarted(request.trialId);
//        try {
//            worker.setUpBenchmark();
//            log.notifyBootstrapPhaseStarting();
//            worker.bootstrap();
//            log.notifyMeasurementPhaseStarting();
//            boolean keepMeasuring = true;
//            boolean isInWarmup = true;
//            while (keepMeasuring) {
//                worker.preMeasure(isInWarmup);
//                log.notifyMeasurementStarting();
//                try {
//                    ShouldContinueMessage message = log.notifyMeasurementEnding(worker.measure());
//                    keepMeasuring = message.shouldContinue();
//                    isInWarmup = !message.isWarmupComplete();
//                } finally {
//                    worker.postMeasure();
//                }
//            }
//        } catch (Exception e) {
//            log.notifyFailure(e);
//        } finally {
//            System.out.flush(); // ?
//            worker.tearDownBenchmark();
//            log.close();
//        }




//        log.notifyWorkerStarted(request.trialId); // StartupAnnounceMessage
        worker.setUpBenchmark();
//        log.notifyBootstrapPhaseStarting(); // String
        worker.bootstrap();
//        log.notifyMeasurementPhaseStarting(); // String

        boolean keepMeasuring = true;
        boolean isInWarmup = true;
        boolean doneCollecting = false;
        while (keepMeasuring) {
            new StartMeasurementLogMessage().accept(measurementCollectingVisitor);
            worker.preMeasure(isInWarmup);
            new StopMeasurementLogMessage(worker.measure()).accept(measurementCollectingVisitor);
            if (!doneCollecting && measurementCollectingVisitor.isDoneCollecting()) {
                doneCollecting = true;
            }
            ShouldContinueMessage msg = new ShouldContinueMessage(!doneCollecting, measurementCollectingVisitor.isWarmupComplete());
            keepMeasuring = msg.shouldContinue();
            isInWarmup = !msg.isWarmupComplete();
            worker.postMeasure();
        }
        worker.tearDownBenchmark();
        trial.addAllMeasurements(measurementCollectingVisitor.getMeasurements());
        trial.addAllMessages(measurementCollectingVisitor.getMessages());
        return trial.getResult();
    }


    private long getTrialTimeLimitTrialNanos(){
        ShortDuration timeLimit=options.timeLimit();
        if(ShortDuration.zero().equals(timeLimit)){
        return Long.MAX_VALUE;
        }
        return timeLimit.to(NANOSECONDS);
        }

        }
