# HFunc
中文 | [English](https://github.com/TangXiaoLv/HFunc/blob/master/README.md) 

一个快速简单轻量级高阶函数库，支持串行，并行计算。适用于Java，Android。
Support
---
+ map
+ filter
+ reduce

Guide
---
	示例数据集：
    List<Integer> c = new ArrayList<>();
    for (int i = 1; i < 101; i++) {
        c.add(i);
    }

[**map:**](https://research.google.com/archive/mapreduce.html)

<img src="img/1.png" height= "228" width="220">

```
假设: 每个应用方法消耗10ms
串行计算: 1078ms
并行计算: 150ms
```

**filter:**

<img src="img/3.png" height= "228" width="220">

```
假设: 每个应用方法消耗10ms
串行计算: 1037ms
并行计算: 159ms
```

[**reduce:**](https://research.google.com/archive/mapreduce.html)

<img src="img/2.png" height= "128" width="480">

```
假设: 每个应用方法消耗10ms
串行计算: 1061ms
并行计算: 239ms
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
