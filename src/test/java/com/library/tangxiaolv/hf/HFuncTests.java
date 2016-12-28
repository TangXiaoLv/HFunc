/**
 * Copyright 2014 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.library.tangxiaolv.hf;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HFuncTests {

    @Test
    public void map() throws Exception {
        List<Integer> c = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            c.add(i);
        }
        long start = System.currentTimeMillis();
        List<String> result = HFunc.map(c, new HFunc.Func1<Integer, String>() {
            @Override
            public String call(Integer item) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Integer.toString(item * 2);
            }
        });
        long time = System.currentTimeMillis() - start;
        System.out.println("Before : " + c);
        System.out.println("===============");
        System.out.println("After : " + result);
        System.out.println("===============");
        System.out.println("Cost time : " + time + "ms");
    }

    @Test
    public void mapParallel() throws Exception {
        List<Integer> c = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            c.add(i);
        }
        long start = System.currentTimeMillis();
        List<String> result = HFunc.mapParallel(c, new HFunc.Func1<Integer, String>() {
            @Override
            public String call(Integer item) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Integer.toString(item * 2);
            }
        });
        long time = System.currentTimeMillis() - start;
        System.out.println("Before : " + c);
        System.out.println("===============");
        System.out.println("After : " + result);
        System.out.println("===============");
        System.out.println("Cost time : " + time + "ms");
    }

    @Test
    public void filter() throws Exception {
        List<Integer> c = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            c.add(i);
        }
        long start = System.currentTimeMillis();
        List<Integer> result = HFunc.filter(c, new HFunc.Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer item) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return item % 2 != 0;
            }
        });
        long time = System.currentTimeMillis() - start;
        System.out.println("Before : " + c);
        System.out.println("===============");
        System.out.println("After : " + result);
        System.out.println("===============");
        System.out.println("Cost time : " + time + "ms");
    }

    @Test
    public void filterParallel() throws Exception {
        List<Integer> c = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            c.add(i);
        }
        long start = System.currentTimeMillis();
        List<Integer> result = HFunc.filterParallel(c, new HFunc.Func1<Integer, Boolean>() {
            @Override
            public Boolean call(Integer item) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return item % 2 != 0;
            }
        });
        long time = System.currentTimeMillis() - start;
        System.out.println("Before : " + c);
        System.out.println("===============");
        System.out.println("After : " + result);
        System.out.println("===============");
        System.out.println("Cost time : " + time + "ms");
    }

    @Test
    public void reduce() throws Exception {
        List<Integer> c = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            c.add(i);
        }
        long start = System.currentTimeMillis();
        Integer result = HFunc.reduce(c, new HFunc.Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer merge, Integer next) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return merge + next;
            }
        });
        long time = System.currentTimeMillis() - start;
        System.out.println("Before : " + c);
        System.out.println("===============");
        System.out.println("After : " + result);
        System.out.println("===============");
        System.out.println("Cost time : " + time + "ms");
    }

    @Test
    public void reduceParallel() throws Exception {
        List<Integer> c = new ArrayList<>();
        for (int i = 1; i < 101; i++) {
            c.add(i);
        }
        long start = System.currentTimeMillis();
        Integer result = HFunc.reduceParallel(c, new HFunc.Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer merge, Integer next) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return merge + next;
            }
        });
        long time = System.currentTimeMillis() - start;
        System.out.println("Before : " + c);
        System.out.println("===============");
        System.out.println("After : " + result);
        System.out.println("===============");
        System.out.println("Cost time : " + time + "ms");
    }
}
