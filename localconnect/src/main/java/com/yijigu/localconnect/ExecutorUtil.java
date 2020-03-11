package com.yijigu.localconnect;

/**
 * Created by Administrator on 2018/11/10.
 */

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池工具类
 *
 * @author Administrator
 */
public class ExecutorUtil {

    public static final String TAG = "ExecutorUtil";

//    /**
//     * CPU数量
//     */
//    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /**
     * 核心线程数量大小
     */
//    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int CORE_POOL_SIZE = 1;

    /**
     * 线程池最大容纳线程数
     */
    private static final int MAXIMUM_POOL_SIZE = 1000;

    private static final int MINIMUM_POOL_SIZE = 1;

    private static RejectedExecutionHandler defaultHandler = new MyThreadRejectedExecution();

    /**
     * 线程空闲后的存活时长
     */
    private static final int KEEP_ALIVE_TIME = 30;

    /**
     * 单线程线程池，用于本地文件管道通讯
     */
    private static final ExecutorService EXECUTE_FOR_SOCKET = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MINIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new DefaultThreadFactory("LOCAL_SOCKET"), defaultHandler);

    /**
     * 线程池，线程池大小为1000
     */
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0,
            MAXIMUM_POOL_SIZE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new DefaultThreadFactory("default"), defaultHandler);

    public static ExecutorService getExecutor() {
        return EXECUTOR;
    }

    public static void doExecute(Runnable paramRunnable) {
        Log.e(TAG, "<---ExecutorUtil doExecute currentThread--->" + Thread.currentThread().getName() + "-" + Thread.currentThread().getId());
        EXECUTOR.execute(paramRunnable);
    }

    public static void doExecuteForLocalSocket(Runnable paramRunnable) {
        Log.e(TAG, "<---ExecutorUtil doExecuteForLocalSocket currentThread--->" + Thread.currentThread().getName() + "-" + Thread.currentThread().getId());
        EXECUTE_FOR_SOCKET.execute(paramRunnable);
    }

    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String mark) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "msg-handle-pool-" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-" + mark + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    static class MyThreadRejectedExecution implements RejectedExecutionHandler {

        MyThreadRejectedExecution() {
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Log.i(TAG, "rejected task--->" + r + "---" + executor.getActiveCount());
        }
    }
}
