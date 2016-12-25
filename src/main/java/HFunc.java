import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <li>Description :高阶函数实现,支持{@link HFunc#map}{@link HFunc#filter}{@link HFunc#reduce}串行，并行计算
 * <li>Created by tangxiaolv on 2016/12/20.
 * <li>Job number：138710
 * <li>Phone ：13601100793
 * <li>Email：tangxiaolv@syswin.com
 * <li>Person in charge : tangxiaolv
 * <li>Leader：tangxiaolv
 */
public class HFunc {

    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final int MAX_THREAD = PROCESSORS * 2;
    private static final int THREAD_TIME_OUT = 60;//SECONDS
    private static volatile ThreadPoolExecutor mThreadPoolExecutor;

    /**
     * Map转换函数：作用于集合每个Item，串行计算
     *
     * @param c    集合
     * @param func 转换函数
     * @param <E>  集合泛型
     * @param <R>  返回值集合泛型
     * @return List<R>
     */
    public static <E, R> List<R> map(Collection<E> c, Func0<E, R> func) {
        if (c == null || c.size() == 0 || func == null) {
            return Collections.emptyList();
        }
        ArrayList<R> result = new ArrayList<>();
        for (E item : c) {
            R r = func.call(item);
            result.add(r);
        }
        return result;
    }

    /**
     * Map转换函数：作用于集合每个Item，并行计算，最大并发量{@link HFunc#MAX_THREAD}
     *
     * @param c    数据集合
     * @param func 转换函数
     * @param <E>  集合泛型
     * @param <R>  返回值集合泛型
     * @return List<R>
     */
    public static <E, R> List<R> mapParallel(Collection<E> c, final Func0<E, R> func) {
        return mapParallel(null, c, func);
    }

    /**
     * Map转换函数：作用于集合每个Item，并行计算，最大并发量{@link HFunc#MAX_THREAD}
     *
     * @param executor {@link ExecutorService}
     * @param c        集合
     * @param func     转换函数
     * @param <E>      集合泛型
     * @param <R>      返回值集合泛型
     * @return List<R>
     */
    public static <E, R> List<R> mapParallel(ExecutorService executor, Collection<E> c, final Func0<E, R> func) {
        if (c == null || c.size() == 0 || func == null) {
            return Collections.emptyList();
        }
        int size = c.size();
        if (size == 1) {
            return map(c, new Func0<E, R>() {
                @Override
                public R call(E item) {
                    return func.call(item);
                }
            });
        }
        executor = executor != null ? executor : defES(size);
        @SuppressWarnings("unchecked") final R[] arr = (R[]) new Object[size];
        final CountDownLatch latch = new CountDownLatch(size);
        int order = 0;
        for (final E item : c) {
            final int index = order;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Func0Inner<E, R> f = new Func0Inner<>(func, index);
                    R r = f.call(item);
                    arr[f.getIndex()] = r;
                    latch.countDown();
                }
            });
            order++;
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            return Collections.emptyList();
        }
        return Arrays.asList(arr);
    }

    /**
     * filter过滤函数：作用于集合每个Item，串行计算
     *
     * @param c    数据集合
     * @param func 过滤函数
     * @param <E>  集合泛型
     * @return List<E>
     */
    public static <E> List<E> filter(Collection<E> c, Func0<E, Boolean> func) {
        if (c == null || c.size() == 0 || func == null) {
            return Collections.emptyList();
        }
        ArrayList<E> result = new ArrayList<>();
        for (E item : c) {
            if (func.call(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * filter过滤函数：作用于集合每个Item，并行计算，最大并发量{@link HFunc#MAX_THREAD}
     *
     * @param c    数据集合
     * @param func 过滤函数
     * @param <E>  集合泛型
     * @return List<E>
     */
    public static <E> List<E> filterParallel(Collection<E> c, Func0<E, Boolean> func) {
        return filterParallel(null, c, func);
    }

    /**
     * filter过滤函数：作用于集合每个Item，并行计算，最大并发量{@link HFunc#MAX_THREAD}
     *
     * @param executor {@link ExecutorService}
     * @param c        数据集合
     * @param func     过滤函数
     * @param <E>      集合泛型
     * @return List<E>
     */
    public static <E> List<E> filterParallel(ExecutorService executor, Collection<E> c, final Func0<E, Boolean> func) {
        if (c == null || c.size() == 0 || func == null) {
            return Collections.emptyList();
        }
        int size = c.size();
        if (size == 1) {
            return filter(c, new Func0<E, Boolean>() {
                @Override
                public Boolean call(E item) {
                    return func.call(item);
                }
            });
        }
        executor = executor != null ? executor : defES(size);
        @SuppressWarnings("unchecked") final E[] arr = (E[]) new Object[size];
        final CountDownLatch latch = new CountDownLatch(size);
        int order = 0;
        for (final E item : c) {
            final int index = order;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Func0Inner<E, Boolean> f = new Func0Inner<>(func, index);
                    if (f.call(item)) {
                        arr[f.getIndex()] = item;
                    }
                    latch.countDown();
                }
            });
            order++;
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            return Collections.emptyList();
        }

        return filter(Arrays.asList(arr), new Func0<E, Boolean>() {
            @Override
            public Boolean call(E item) {
                return item != null;
            }
        });
    }

    /**
     * reduce作用函数：作用于集合每个Item
     *
     * @param c    数据集合
     * @param func 集合泛型
     * @param <E>  集合泛型
     * @return 最后结果
     */
    public static <E> E reduce(Collection<E> c, Func1<E, E> func) {
        if (c == null || c.size() == 0 || func == null) {
            return null;
        }
        Iterator<E> it = c.iterator();
        if (c.size() == 1) return it.next();
        E merge = it.next();
        while (it.hasNext()) {
            E next = it.next();
            merge = func.call(merge, next);
        }
        return merge;
    }

    /**
     * reduce作用函数：作用于集合每个Item，并行计算，最大并发量{@link HFunc#MAX_THREAD}
     *
     * @param c    数据集合
     * @param func 集合泛型
     * @param <E>  集合泛型
     * @return 最后结果
     */
    public static <E> E reduceParallel(Collection<E> c, Func1<E, E> func) {
        return reduceParallel(null, c, func);

    }

    /**
     * reduce作用函数：作用于集合每个Item，并行计算，最大并发量{@link HFunc#MAX_THREAD}
     *
     * @param executor {@link ExecutorService}
     * @param c        数据集合
     * @param func     集合泛型
     * @param <E>      集合泛型
     * @return 最后结果
     */
    public static <E> E reduceParallel(ExecutorService executor, Collection<E> c, Func1<E, E> func) {
        if (c == null || c.size() == 0 || func == null) {
            return null;
        }
        int size = c.size();
        if (size == 1) return c.iterator().next();
        executor = executor != null ? executor : defES(size);
        //sub collection num
        int splits = mThreadPoolExecutor.getCorePoolSize();
        CountDownLatch latch = new CountDownLatch(splits);
        List<E> finals = Collections.synchronizedList(new ArrayList<E>(splits));

        Iterator<E> it = c.iterator();
        int elements = size / splits;
        boolean hasRemainder = size % splits != 0;
        for (int i = 0; i < splits; i++) {
            if (hasRemainder && i == splits - 1) elements += size % splits;
            ArrayList<E> split = new ArrayList<>();
            int count = 0;
            while (it.hasNext()) {
                split.add(it.next());
                count++;
                if (count >= elements) break;
            }
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    finals.add(reduce(split, new Func1<E, E>() {
                        @Override
                        public E call(E merge, E next) {
                            return func.call(merge, next);
                        }
                    }));
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            return null;
        }

        //Summary
        return reduce(finals, new Func1<E, E>() {
            @Override
            public E call(E merge, E next) {
                return func.call(merge, next);
            }
        });
    }

    private synchronized static ExecutorService defES(int size) {
        if (mThreadPoolExecutor != null) {
            return mThreadPoolExecutor;
        }
        int nThreads = size <= PROCESSORS ? size : size > MAX_THREAD ? MAX_THREAD : size;
        mThreadPoolExecutor = new ThreadPoolExecutor(nThreads, nThreads, THREAD_TIME_OUT, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        mThreadPoolExecutor.allowCoreThreadTimeOut(true);
        return mThreadPoolExecutor;
    }

    /**
     * 有序函数
     */
    private static class Func0Inner<E, R> {
        private Func0<E, R> func0;
        private int index;

        Func0Inner(Func0<E, R> func, int index) {
            this.func0 = func;
            this.index = index;
        }

        int getIndex() {
            return index;
        }

        R call(E item) {
            return func0.call(item);
        }
    }

    /**
     * 作用函数
     *
     * @param <E> 入参泛型
     * @param <R> 返回值泛型
     */
    public interface Func0<E, R> {
        public R call(E item);
    }

    /**
     * 作用函数
     *
     * @param <E> 入参泛型
     * @param <R> 返回值泛型
     */
    public interface Func1<E, R> {
        public R call(E merge, E next);
    }
}