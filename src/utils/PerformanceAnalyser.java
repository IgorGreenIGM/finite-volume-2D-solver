package utils;

public class PerformanceAnalyser {
    private long startTime;
    private long endTime;


    public void start() {
        startTime = System.nanoTime();
    }

    public void stop() {
        endTime = System.nanoTime();
    }

    public double getDurationMillis() {
        return (endTime - startTime) / 1_000_000.0;
    }

    public static double estimateDenseMatrixMemoryMb(int n, int m) {
        long size = (long) n * m;
        long numElements = size * size;
        long bytes = numElements * 8; // double == 08 bytes
        return bytes / (1024.0 * 1024.0);
    }

    public static double estimateCdsMatrixMemoryMb(int n, int m) {
        long size = (long) n * m;
        // 5 diagonales: principale(size), sup1(size-1), inf1(size-1), supN(size-n), infN(size-n)
        long numElements = size + 2 * (size - 1) + 2 * (size - n);
        long bytes = numElements * 8;
        return bytes / (1024.0 * 1024.0);
    }
}