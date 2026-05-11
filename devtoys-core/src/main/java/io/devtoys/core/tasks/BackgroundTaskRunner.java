package io.devtoys.core.tasks;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 把计算放到后台，把结果回调切回 FX 线程。
 * <p>使用示例:
 * <pre>{@code
 * private final DebouncedTaskRunner<String> runner = new DebouncedTaskRunner<>();
 *
 * input.textProperty().addListener((obs, o, n) -> {
 *     runner.run(() -> expensiveCompute(n),   // runs on background thread
 *                output::setText,             // runs on FX thread with result
 *                err -> status.setText("Error: " + err.getMessage()));
 * });
 * }</pre>
 * <p> 语义:
 * <ul>
 * <li> 每个线程只激活一个任务。提交一个新任务会取消之前的任务 (它的结果被丢掉)。
 * <li> 结果在JavaFX应用程序线程上传递。
 * <li> 一个共享的守护进程池支持所有运行程序，大小为 {@code max(2，availableProcessors())}，
 * DevToys工具对延迟敏感，但对吞吐量不敏感。
 * <ul>
 * <p> {@link busyProperty()} 暴露当前是否有正在运行的任务，UI可以在重要时显示进度指示器 (主要仅用于图像工具-文本工具以毫秒为单位)。
 */
public class BackgroundTaskRunner {
    private static final Logger LOG = LoggerFactory.getLogger(BackgroundTaskRunner.class);

    private static final ExecutorService POOL = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()),
            new DaemonThreadFactory());

    private BackgroundTaskRunner() {
    }

    /**
     * 当应用退出时调用，JVM退出时也会调用
     */
    public static void shutdown() {
        POOL.shutdownNow();
    }

    /**
     * 每个工具线程只保存一个正在运行的{@link Future}，
     * 当有新请求时会被取消。
     *
     * @param <R> 结果类型
     */
    public static final class DebouncedTaskRunner<R> {

        private final ReadOnlyBooleanWrapper busy = new ReadOnlyBooleanWrapper(false);
        private volatile Future<?> current;

        public ReadOnlyBooleanProperty busyProperty() {
            return busy.getReadOnlyProperty();
        }

        /**
         * 提交新计算。 之前待处理的计算会被取消
         *
         * @param work      后台线程上进行的计算
         * @param onSuccess JFX 线程调用获取计算结果
         * @param onError   JFX线程嗲用获取 {@code work} 线程抛出的异常
         */
        public void run(Supplier<R> work, Consumer<R> onSuccess, Consumer<Throwable> onError) {
            Future<?> prev = current;
            if (prev != null && !prev.isDone()) {
                prev.cancel(true);
            }
            setBusy(true);
            current = POOL.submit(() -> {
                try {
                    R result = work.get();
                    if (!Thread.currentThread().isInterrupted()) {
                        Platform.runLater(() -> {
                            try {
                                onSuccess.accept(result);
                            } finally {
                                setBusy(false);
                            }
                        });
                    } else {
                        Platform.runLater(() -> setBusy(false));
                    }
                } catch (RuntimeException e) {
                    if (Thread.currentThread().isInterrupted()) {
                        Platform.runLater(() -> setBusy(false));
                        return;
                    }
                    LOG.debug("任务失败", e);
                    Platform.runLater(() -> {
                        try {
                            onError.accept(e);
                        } finally {
                            setBusy(false);
                        }
                    });
                }
            });
        }

        private void setBusy(boolean b) {
            if (Platform.isFxApplicationThread()) {
                busy.set(b);
            } else {
                Platform.runLater(() -> busy.set(b));
            }
        }
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "devtoys-bg-" + counter.getAndIncrement());
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        }
    }
}
