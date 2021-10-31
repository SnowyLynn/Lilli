package com.amelynn.lilli.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

// This codee is taken from Lucko's Spark (Licensed under GPL3)
// https://github.com/lucko/spark/blob/master/spark-common/src/main/java/me/lucko/spark/common/monitor/tick/TickStatistics.java
public class TickStats {
    private static final long SEC_IN_NANO = TimeUnit.SECONDS.toNanos(1);
    private static final int TPS = 20;
    private static final int TPS_SAMPLE_INTERVAL = 20;
    private static final BigDecimal TPS_BASE = new BigDecimal(SEC_IN_NANO).multiply(new BigDecimal(TPS_SAMPLE_INTERVAL));

    private final TpsRollingAverage tps5Sec = new TpsRollingAverage(5);
    private final TpsRollingAverage tps10Sec = new TpsRollingAverage(10);
    private final TpsRollingAverage tps1Min = new TpsRollingAverage(60);
    private final TpsRollingAverage tps5Min = new TpsRollingAverage(60 * 5);
    private final TpsRollingAverage tps15Min = new TpsRollingAverage(60 * 15);
    private final TpsRollingAverage[] tpsAverages = {this.tps5Sec, this.tps10Sec, this.tps1Min, this.tps5Min, this.tps15Min};

    private final RollingAverage tickDuration10Sec = new RollingAverage(TPS * 10);
    private final RollingAverage tickDuration1Min = new RollingAverage(TPS * 60);
    private final RollingAverage[] tickDurationAverages = {this.tickDuration10Sec, this.tickDuration1Min};

    private long last = 0;

    public TickStats() {
        AtomicInteger tick = new AtomicInteger();
        AtomicLong start = new AtomicLong();
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            onTick(tick.getAndIncrement());
            start.set(System.nanoTime());
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> onTick((System.nanoTime() - start.get()) / 1000000d));
    }

    public void onTick(int currentTick) {
        if (currentTick % TPS_SAMPLE_INTERVAL != 0) {
            return;
        }

        long now = System.nanoTime();

        if (this.last == 0) {
            this.last = now;
            return;
        }

        long diff = now - this.last;
        BigDecimal currentTps = TPS_BASE.divide(new BigDecimal(diff), 30, RoundingMode.HALF_UP);
        BigDecimal total = currentTps.multiply(new BigDecimal(diff));

        for (TpsRollingAverage rollingAverage : this.tpsAverages) {
            rollingAverage.add(currentTps, diff, total);
        }

        this.last = now;
    }

    public void onTick(double duration) {
        BigDecimal decimal = new BigDecimal(duration);
        for (RollingAverage rollingAverage : this.tickDurationAverages) {
            rollingAverage.add(decimal);
        }
    }

    public double tps5Sec() {
        return this.tps5Sec.getAverage();
    }

    public double tps10Sec() {
        return this.tps10Sec.getAverage();
    }

    public double tps1Min() {
        return this.tps1Min.getAverage();
    }

    public double tps5Min() {
        return this.tps5Min.getAverage();
    }

    public double tps15Min() {
        return this.tps15Min.getAverage();
    }

    public RollingAverage duration10Sec() {
        return this.tickDuration10Sec;
    }

    public RollingAverage duration1Min() {
        return this.tickDuration1Min;
    }


    /**
     * This code is taken from PaperMC/Paper, licensed under MIT.
     *
     * @author aikar (PaperMC) https://github.com/PaperMC/Paper/blob/master/Spigot-Server-Patches/0021-Further-improve-server-tick-loop.patch
     */
    public static final class TpsRollingAverage {
        private final int size;
        private long time;
        private BigDecimal total;
        private int index = 0;
        private final BigDecimal[] samples;
        private final long[] times;

        TpsRollingAverage(int size) {
            this.size = size;
            this.time = size * SEC_IN_NANO;
            this.total = new BigDecimal(TPS).multiply(new BigDecimal(SEC_IN_NANO)).multiply(new BigDecimal(size));
            this.samples = new BigDecimal[size];
            this.times = new long[size];
            for (int i = 0; i < size; i++) {
                this.samples[i] = new BigDecimal(TPS);
                this.times[i] = SEC_IN_NANO;
            }
        }

        public void add(BigDecimal x, long t, BigDecimal total) {
            this.time -= this.times[this.index];
            this.total = this.total.subtract(this.samples[this.index].multiply(new BigDecimal(this.times[this.index])));
            this.samples[this.index] = x;
            this.times[this.index] = t;
            this.time += t;
            this.total = this.total.add(total);
            if (++this.index == this.size) {
                this.index = 0;
            }
        }

        public double getAverage() {
            return this.total.divide(new BigDecimal(this.time), 30, RoundingMode.HALF_UP).doubleValue();
        }
    }
}
class RollingAverage {
    private final Queue<BigDecimal> samples;
    private final int windowSize;
    private BigDecimal total = BigDecimal.ZERO;

    public RollingAverage(int windowSize) {
        this.windowSize = windowSize;
        this.samples = new ArrayDeque<>(this.windowSize + 1);
    }

    public void add(BigDecimal num) {
        synchronized (this) {
            this.total = this.total.add(num);
            this.samples.add(num);
            if (this.samples.size() > this.windowSize) {
                this.total = this.total.subtract(this.samples.remove());
            }
        }
    }

    public double mean() {
        synchronized (this) {
            if (this.samples.isEmpty()) {
                return 0;
            }
            BigDecimal divisor = BigDecimal.valueOf(this.samples.size());
            return this.total.divide(divisor, 30, RoundingMode.HALF_UP).doubleValue();
        }
    }

    public double median() {
        return percentile(0.50d);
    }

    public double max() {
        synchronized (this) {
            BigDecimal max = null;
            for (BigDecimal sample : this.samples) {
                if (max == null || sample.compareTo(max) > 0) {
                    max = sample;
                }
            }
            return max == null ? 0 : max.doubleValue();
        }
    }

    public double min() {
        synchronized (this) {
            BigDecimal min = null;
            for (BigDecimal sample : this.samples) {
                if (min == null || sample.compareTo(min) < 0) {
                    min = sample;
                }
            }
            return min == null ? 0 : min.doubleValue();
        }
    }

    public double percentile(double percentile) {
        if (percentile < 0 || percentile > 1) {
            throw new IllegalArgumentException("Invalid percentile " + percentile);
        }

        BigDecimal[] sortedSamples;
        synchronized (this) {
            if (this.samples.isEmpty()) {
                return 0;
            }
            sortedSamples = this.samples.toArray(new BigDecimal[0]);
        }
        Arrays.sort(sortedSamples);

        int rank = (int) Math.ceil(percentile * (sortedSamples.length - 1));
        return sortedSamples[rank].doubleValue();
    }

}