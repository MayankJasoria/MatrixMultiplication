import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/* parallel implementation of matrix multiplication */
class ParallelMatrixMultiplier {

    private final int[][] A;
    private final int[][] B;
    private final int numRowsA;
    private final int numColsA;
    private final int numRowsB;
    private final int numColsB;

    public ParallelMatrixMultiplier(int[][] A, int[][] B) {
        this.A = A;
        this.B = B;
        this.numRowsA = A.length;
        this.numColsA = A[0].length;
        this.numRowsB = B.length;
        this.numColsB = B[0].length;
        if (numColsA != numRowsB)
            throw new Error(String.format("Invalid dimensions; Cannot multiply %dx%d*%dx%d\n", numRowsA, numRowsB, numColsA, numColsB));
    }

    /* returns matrix product C = A.B */
    public int[][] computeProduct() {
        int[][] result = new int[numRowsA][numColsB];
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            // parallelize on rows or columns, whichever is higher
            for (int i = 0; i < numRowsA; ++i) {
                executorService.submit(new RowValueComputer(i, result));
            }
            executorService.shutdown();
            if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Class to compute a single row of a result matrix derived by multiplying two matrices.
     */
    private class RowValueComputer implements Runnable {
        int row;
        int[][] result;

        public RowValueComputer(int row, int[][] result) {
            this.row = row;
            this.result = result;
        }

        @Override
        public void run() {
            for (int j = 0; j < numColsB; ++j) {
                int sum = 0;
                for (int i = 0; i < numColsA; ++i) {
                    sum += A[row][i] * B[i][j];
                }
                result[row][j] = sum;
            }
        }
    }
}