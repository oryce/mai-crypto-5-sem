package dora.crypto.mode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.IntStream;

final class ParallelBlockProcessor {

    private static final int BLOCKS_PER_TASK = 64;

    private ParallelBlockProcessor() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> List<T> processBlocks(
        byte[] data,
        int blockSize,
        ForkJoinPool pool,
        BlockSliceFunction<T> function
    ) throws InterruptedException {
        int blocks = data.length / blockSize;
        int tasks = Math.ceilDiv(blocks, BLOCKS_PER_TASK);

        if (tasks == 1) {
            return function.apply(0, blocks);
        } else {
            ForkJoinTask<List<T>> task = pool.submit(() ->
                IntStream.range(0, tasks)
                    .parallel()
                    .mapToObj((taskIdx) -> {
                        int startBlock = taskIdx * BLOCKS_PER_TASK;
                        int endBlock = Math.min(startBlock + BLOCKS_PER_TASK, blocks);
                        return function.apply(startBlock, endBlock);
                    })
                    .flatMap(Collection::stream)
                    .toList()
            );

            try {
                return task.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FunctionalInterface
    public interface BlockSliceFunction<T> {

        List<T> apply(int startBlock, int endBlock);
    }

    public static <T> List<T> processBlocks(
        byte[] data,
        int blockSize,
        ForkJoinPool pool,
        BlockFunction<T> function
    ) throws InterruptedException {
        return processBlocks(
            data,
            blockSize,
            pool,
            (startBlock, endBlock) -> {
                List<T> results = new ArrayList<>(endBlock - startBlock);

                for (int blockIdx = startBlock; blockIdx < endBlock; blockIdx++) {
                    int offset = blockIdx * blockSize;
                    results.add(function.apply(blockIdx, offset, offset + blockSize));
                }

                return results;
            }
        );
    }

    @FunctionalInterface
    public interface BlockFunction<T> {

        T apply(int idx, int start, int end);
    }
}
