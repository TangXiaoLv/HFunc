/**
 * Copyright 2014 XiaoLv Tang, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package hf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A fast and simple Java Higher-order function lib. Support serial compute and parallel compute.
 * <p>
 * Support {@link HFunc#map}{@link HFunc#filter}{@link HFunc#reduce}
 */
public class HFunc {

    /**
     * current available processors
     */
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    /**
     * corePoolSize and maximumPoolSize
     * <p>
     * see {@link ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue)}
     * <p>
     */
    private static final int MAX_THREAD = PROCESSORS * 2;

    /**
     * the keepAliveTime for Thread
     * <p>
     * see {@link ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue)}
     * <p>
     */
    private static final int THREAD_TIME_OUT = 60;//SECONDS

    /**
     * used to parallel compute for Higher-order function
     * <p>
     * see {@link ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue)}
     * <p>
     */
    private static volatile ThreadPoolExecutor mThreadPoolExecutor;

    /**
     * Returns an list that applies a specified function to each item.This is serial compute.
     *
     * @param c    collection with date item
     * @param func a function to apply to each item
     * @param <E>  collection item type
     * @param <R>  the result type
     * @return List<R> the result transformed by the specified function
     */
    public static <E, R> List<R> map(Collection<E> c, Func1<E, R> func) {
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
     * Returns an list that applies a specified function to each item.This is parallel compute.
     * The concurrent quantity see{@link HFunc#MAX_THREAD}
     *
     * @param c    collection with date item
     * @param func a function to apply to each item
     * @param <E>  collection item type
     * @param <R>  the result type
     * @return List<R> the result transformed by the specified function
     */
    public static <E, R> List<R> mapParallel(Collection<E> c, final Func1<E, R> func) {
        return mapParallel(null, c, func);
    }

    /**
     * Returns an list that applies a specified function to each item.This is parallel compute.
     * The concurrent quantity see{@link HFunc#MAX_THREAD}
     *
     * @param executor {@link ExecutorService}
     * @param c        collection with date item
     * @param func     a function to apply to each item
     * @param <E>      collection item type
     * @param <R>      the result type
     * @return List<R> the result transformed by the specified function
     */
    public static <E, R> List<R> mapParallel(ExecutorService executor, Collection<E> c, final Func1<E, R> func) {
        if (c == null || c.size() == 0 || func == null) {
            return Collections.emptyList();
        }
        int size = c.size();
        if (size == 1) {
            return map(c, new Func1<E, R>() {
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
                    Func1Inner<E, R> f = new Func1Inner<>(func, index);
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
     * Returns an list that applies a specified function to each item.This is serial compute.
     *
     * @param c    collection with date item
     * @param func a function to apply to each item
     * @param <E>  collection item type
     * @return List<E> the result transformed by the specified function
     */
    public static <E> List<E> filter(Collection<E> c, Func1<E, Boolean> func) {
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
     * Returns an list that applies a specified function to each item.This is parallel compute.
     * The concurrent quantity see{@link HFunc#MAX_THREAD}
     *
     * @param c    collection with date item
     * @param func a function to apply to each item
     * @param <E>  collection item type
     * @return List<E> the result transformed by the specified function
     */
    public static <E> List<E> filterParallel(Collection<E> c, Func1<E, Boolean> func) {
        return filterParallel(null, c, func);
    }

    /**
     * Returns an list that applies a specified function to each item.This is parallel compute.
     * The concurrent quantity see{@link HFunc#MAX_THREAD}
     *
     * @param executor {@link ExecutorService}
     * @param c        collection with date item
     * @param func     a function to apply to each item
     * @param <E>      collection item type
     * @return List<E> the result transformed by the specified function
     */
    public static <E> List<E> filterParallel(ExecutorService executor, Collection<E> c, final Func1<E, Boolean> func) {
        if (c == null || c.size() == 0 || func == null) {
            return Collections.emptyList();
        }
        int size = c.size();
        if (size == 1) {
            return filter(c, new Func1<E, Boolean>() {
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
                    Func1Inner<E, Boolean> f = new Func1Inner<>(func, index);
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

        return filter(Arrays.asList(arr), new Func1<E, Boolean>() {
            @Override
            public Boolean call(E item) {
                return item != null;
            }
        });
    }

    /**
     * Return a merges result that applies a specified function to each item.This is serial compute.
     *
     * @param c    collection with date item
     * @param func a function to apply to each item
     * @param <E>  collection item type
     * @return the result transformed by the specified function
     */
    public static <E> E reduce(Collection<E> c, Func2<E, E, E> func) {
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
     * Return a merges result that applies a specified function to each item.This is parallel compute.
     * The concurrent quantity see{@link HFunc#MAX_THREAD}
     *
     * @param c    collection with date item
     * @param func a function to apply to each item
     * @param <E>  collection item type
     * @return the result transformed by the specified function
     */
    public static <E> E reduceParallel(Collection<E> c, Func2<E, E, E> func) {
        return reduceParallel(null, c, func);

    }

    /**
     * Return a merges result that applies a specified function to each item.This is parallel compute.
     * The concurrent quantity see{@link HFunc#MAX_THREAD}
     *
     * @param executor {@link ExecutorService}
     * @param c        collection with date item
     * @param func     a function to apply to each item
     * @param <E>      collection item type
     * @return the result transformed by the specified function
     */
    public static <E> E reduceParallel(ExecutorService executor, Collection<E> c, Func2<E, E, E> func) {
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
                    finals.add(reduce(split, new Func2<E, E, E>() {
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
        return reduce(finals, new Func2<E, E, E>() {
            @Override
            public E call(E merge, E next) {
                return func.call(merge, next);
            }
        });
    }

    //Create ExecutorService
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
     * the function with index
     */
    private static class Func1Inner<E, R> {
        private Func1<E, R> func1;
        private int index;

        Func1Inner(Func1<E, R> func, int index) {
            this.func1 = func;
            this.index = index;
        }

        int getIndex() {
            return index;
        }

        R call(E item) {
            return func1.call(item);
        }
    }

    /**
     * Represents a function with one arguments.
     *
     * @param <E> the first argument type
     * @param <R> the result type
     */
    public interface Func1<E, R> {
        public R call(E item);
    }

    /**
     * Represents a function with two arguments.
     *
     * @param <E1> the first argument type
     * @param <E2> the second argument type
     * @param <R>  the result type
     */
    public interface Func2<E1, E2, R> {
        public R call(E1 merge, E2 next);
    }
}