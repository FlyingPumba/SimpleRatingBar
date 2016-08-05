SimpleRatingBar
====

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-%20SimpleRatingBar-green.svg?style=true)](https://android-arsenal.com/details/1/4027)
[![Download](https://api.bintray.com/packages/flyingpumba/maven/simpleratingbar/images/download.svg) ](https://bintray.com/flyingpumba/maven/simpleratingbar/_latestVersion)
[![License](https://img.shields.io/badge/License-Apache%202-blue.svg)](https://github.com/FlyingPumba/SimpleRatingBar/blob/master/LICENSE.txt)

A simple RatingBar for Android.

![](images/sample2.gif)

Features
----
* Fully working `android:layout_width`: it can be set to `wrap_content`, `match_parent` or abritary dp.
* Arbitrary number of stars.
* Arbitrary step size.
* Size of stars can be controlled exactly or by setting a maximum size.
* Customizable colors (border, fill and background of stars).
* Customizable size separation between stars.
* Customizable border width of stars.
* Customizable stars corner radius.
* Allows to set OnRatingBarChangeListener
* Stars fill can be set to start from left to right or from right to left (RTL language support).
* AnimationBuilder integrated in the view to set rating programatically with animation.

Usage
----

In your layout file:

```xml
<com.iarcuschin.simpleratingbar.SimpleRatingBar
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:starSize="40dp"
        app:numberOfStars="5"
        app:rating="3"
        app:stepSize="0.5"
        app:borderColor="@color/blue"
        app:fillColor="@color/light_blue"
        />
```

More examples in the [sample app layouts](https://github.com/FlyingPumba/SimpleRatingBar/blob/master/simpleratingbar-sample/src/main/res/layout).

Example of setting rating with animation:

```java
SimpleRatingBar.AnimationBuilder builder = myRatingBar.getAnimationBuilder()
        .setRatingTarget(3)
        .setDuration(2000)
        .setInterpolator(new BounceInterpolator());
    builder.start();
```


Configuration
----
The view can be configured as follows:

* Set the number of stars with `app:numberOfStars` / `setNumberOfStars(int)`
* Set the rating with `app:rating` / `setRating(float)`
* Set step size with `app:stepSize` / `setStepSize(float)`
* Set star size with `app:starSize` / `setStarSize(float)`
* Set max star size with `app:maxStarSize` / `setMaxStarSize(float)`
* Set separation between stars with `app:starsSeparation` / `setStarsSeparation(float)`
* Set border width with `app:starBorderWidth` / `setStarBorderWidth(float)`
* Set stars corner radius with `app:starCornerRadius` / `setStarCornerRadius(float)`
* Set stars border color with `app:borderColor` / `setBorderColor(@ColorInt int)`
* Set stars fill color with `app:fillColor` / `setFillColor(@ColorInt int)`
* Set background color with `app:backgroundColor` / `setBackgroundColor(@ColorInt int)`
* Enabled/disable user interaction with `app:isIndicator` / `setIsIndicator(boolean)`
* Set gravity of fill (left or right) with `app:gravity` / `setGravity(Gravity)`

Download
----

Using `Gradle` from `jcenter`:

```groovy
repositories {
    jcenter()
}
dependencies {
    compile 'com.iarcuschin:simpleratingbar:0.0.5'
}
```

Using `Gradle` from `Maven Central`:

```groovy
repositories {
    mavenCentral()
}
dependencies {
    compile 'com.iarcuschin:simpleratingbar:0.0.5'
}
```

Using `Maven`:
```xml
<dependency>
  <groupId>com.iarcuschin</groupId>
  <artifactId>simpleratingbar</artifactId>
  <version>0.0.5</version>
  <type>apklib</type>
</dependency>
```

License
----
    Copyright 2016 Iván Arcuschin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
