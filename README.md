# HFunc
[ ![Download](https://api.bintray.com/packages/tangxiaolv/maven/hfunc/images/download.svg) ](https://bintray.com/tangxiaolv/maven/hfunc/_latestVersion)

English | [中文](https://github.com/TangXiaoLv/HFunc/blob/master/README_CN.md) 

A fast and simple Java Higher-order function lib. Support serial compute and parallel compute.
Applies to Java and Android.

Support
---
+ map
+ filter
+ reduce

Gradle
---
    dependencies {
        compile 'com.library.tangxiaolv:hfunc:1.0.1'
    }

Guide
---
	Example:
	//data collection
    List<Integer> c = new ArrayList<>();
    for (int i = 1; i < 101; i++) {
        c.add(i);
    }

[**map:**](https://research.google.com/archive/mapreduce.html)

<img src="img/1.png" height= "228" width="220">

```
serial compute: 1078ms
HFunc.map(c, new HFunc.Func1<Integer, String>() {
    @Override
    public String call(Integer item) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {}
        return Integer.toString(item * 2);
    }
});

parallel compute: 150ms
HFunc.mapParallel(c, new HFunc.Func1<Integer, String>() {
    @Override
    public String call(Integer item) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {}
        return Integer.toString(item * 2);
    }
});
```

**filter:**

<img src="img/3.png" height= "228" width="220">

```
serial compute: 1037ms
HFunc.filter(c, new HFunc.Func1<Integer, Boolean>() {
    @Override
    public Boolean call(Integer item) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {}
        return item % 2 != 0;
    }
});
        
parallel compute: 159ms
HFunc.filterParallel(c, new HFunc.Func1<Integer, Boolean>() {
    @Override
    public Boolean call(Integer item) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {}
        return item % 2 != 0;
    }
});
```

[**reduce:**](https://research.google.com/archive/mapreduce.html)

<img src="img/2.png" height= "128" width="480">

```
serial compute: 1061ms
HFunc.reduce(c, new HFunc.Func2<Integer, Integer, Integer>() {
    @Override
    public Integer call(Integer merge, Integer next) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {}
        return merge + next;
    }
});

parallel compute: 239ms
HFunc.reduceParallel(c, new HFunc.Func2<Integer, Integer, Integer>() {
    @Override
    public Integer call(Integer merge, Integer next) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {}
        return merge + next;
    }
});
```

LICENSE
---

    Copyright 2016 XiaoLv Tang

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
