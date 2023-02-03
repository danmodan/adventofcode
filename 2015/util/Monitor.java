package util;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import log.LoggerFactory;

public class Monitor {

    private static final Logger log = LoggerFactory.getLogger(Monitor.class);

    private final Supplier<String> logFunc;
    private final long delayMilliseconds;

    private Monitor(
            Supplier<String> logFunc,
            long delayMilliseconds) {
        this.logFunc = logFunc;
        this.delayMilliseconds = delayMilliseconds;
    }

    public void run() {

        while (true) {

            String logTxt = logFunc.get();

            log.info(logTxt);

            sleep();
        }
    }

    private void sleep() {
        try {
            Thread.sleep(delayMilliseconds);
        } catch (InterruptedException e) {
            log.log(Level.SEVERE, null, e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Supplier<String> logFunc;
        private long delayMilliseconds = 2_000L;

        public Builder logFunc(Supplier<String> logFunc) {
            this.logFunc = logFunc;
            return this;
        }

        public Builder delayMilliseconds(long delayMilliseconds) {
            this.delayMilliseconds = delayMilliseconds;
            return this;
        }

        public Monitor build() {

            Objects.requireNonNull(logFunc);
            Objects.requireNonNull(delayMilliseconds);

            return new Monitor(
                    logFunc,
                    delayMilliseconds);
        }
    }
}