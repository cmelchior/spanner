package dk.ilios.gauge;

import java.io.File;

/**
 * Class for adding custom configuration of a Gauge run.
 */
public class GaugeConfig {

    private File resultsFolder;
    private File baseLineFile;
    private boolean warnIfWrongTestGranularity;
    private final boolean createBaseLine;

    private GaugeConfig(Builder builder) {
        this.resultsFolder = builder.resultsFolder;
        this.baseLineFile = builder.baseLineFile;
        this.warnIfWrongTestGranularity = builder.warnIfWrongTestGranularity;
        this.createBaseLine = builder.createBaseline;
    }

    public File getResultsFolder() {
        return resultsFolder;
    }

    public File getBaseLineFile() {
        return baseLineFile;
    }

    public boolean shouldWarnIfWrongTestGranularity() {
        return warnIfWrongTestGranularity;
    }

    public boolean shouldCreateBaseline() {
        return createBaseLine;
    }

    /**
     * Builder for fluent construction of a GaugeConfig object.
     */
    public static class Builder {
        private File resultsFolder = null;
        private File baseLineFile = null;
        private boolean warnIfWrongTestGranularity = false;
        private boolean createBaseline = false;

        public Builder() {

        }

        /**
         * Constructs an instance of {@link GaugeConfig}.
         */
        public GaugeConfig build() {
            return new GaugeConfig(this);
        }

        /**
         * Set the folder where any benchmark results should be stored.
         *
         * @param dir Reference to folder.
         * @return Builder object.
         */
        public Builder resultsFolder(File dir) {
            checkNotNull(dir, "Results folder was null.");
            this.resultsFolder = dir;
            return this;
        }

        // TODO Add support for overriding the filename

        /**
         * Set a baseline for the tests being run.
         *
         * @param file Reference to the baseline file (see .
         * @return Builder object.
         */
        public Builder baseline(File file) {
            checkNotNull(file, "Baseline file was null");
            this.baseLineFile = file;
            return this;
        }

        /**
         * Setting this will cause Gauge to verify that the granularity of the tests are set correctly or will
         * @return
         */
        public Builder warnIfWrongTestGranularity() {
            this.warnIfWrongTestGranularity = true;
            return this;
        }

        /**
         * Setting this will cause the benchmarks results to be saved in a new baseline file in the results folder.
         * @return
         */
        public Builder createBaseline() {
            this.createBaseline = true;
            return this;
        }

        private void checkNotNull(Object obj, String errorMessage) {
            if (obj == null) {
                throw new IllegalArgumentException(errorMessage);
            }
        }
    }
}
