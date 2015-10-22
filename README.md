# 1Sheeld Android App [![Build Status](https://travis-ci.org/Integreight/1Sheeld-Android-App.svg?branch=master)](https://travis-ci.org/Integreight/1Sheeld-Android-App)#

## Overview ##

1Sheeld app is used along with our hardware shield to either control Arduino, read your smartphone's sensors in your Arduino sketch, post on social media, or even control your Android device. It enables you to scan, connect, and interact with nearby 1Sheelds using our custom protocol

## Installation ##

You can download the latest version of our app from [Google Play Store](https://play.google.com/store/apps/details?id=com.integreight.onesheeld&hl=en), or pick your preferred version directly from our repo's [releases page](https://github.com/Integreight/1Sheeld-Android-App/releases).

## Screenshots ##

![App Screenshots](http://i.imgur.com/9KqOKKu.png)

## Building ##

The repo is a generic Gradle project, it was built and tested using the latest stable version of Android Studio.

To build the project and generate the release and debug apk(s), run this command on the root of the repo:

```
.\gradlew assemble
```

## Access Tokens ##

In case you want to use any of the web services we use, you should update its access tokens in [meta_data.json](https://github.com/Integreight/1Sheeld-Android-App/blob/master/oneSheeld/src/main/assets/meta_data.json) file.

## Compatibility ##

The app should work with Android devices running version 2.3 and above.

## Contribution ##

Contributions are welcomed, please follow this pattern:
- Fork the repo.
- Open an issue with your proposed feature or bug fix.
- Commit and push code to a new branch in your forked repo.
- Submit a pull request to our *development* branch.

Don't forget to drop us an email, post on our forum, or mention us on Twitter or Facebook about what you have built using 1Sheeld, we would love to hear about it.

## Learn More ##

- [Getting started tutorial](http://www.1sheeld.com/tutorials/getting-started)
- [Arduino library documentation](http://1sheeld.com/docs/).
- Check our [1Sheeld Forums](http://www.1sheeld.com/forum) where you can post your questions and get answers.
- Explore what people have built at our [Hackster.io page](https://www.hackster.io/1sheeld/projects)
- Built custom apps using our [Android SDK](https://github.com/Integreight/1Sheeld-Android-SDK).

## Changelog ##

To see what has changed in recent versions of 1Sheeld Android App, see the [Change Log](CHANGELOG.md).

## License and Copyright ##

```
This code is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License version 3 only, as
published by the Free Software Foundation.

This code is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
version 3 for more details (a copy is included in the LICENSE file that
accompanied this code).

Please contact Integreight, Inc. at info@integreight.com or post on our
support forums www.1sheeld.com/forum if you need additional information
or have any questions.
```
