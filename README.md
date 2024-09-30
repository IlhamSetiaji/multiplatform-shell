This is a Kotlin Multiplatform project targeting Android, iOS.



* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* `/shared` is for the code that will be shared between all targets in the project.
  The most important subfolder is `commonMain`. If preferred, you can add code to the platform-specific folders here too.

* `/sharedModule` is similar with `/shared` but this folder will be share all the modules for different platform.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
## Tech Stack

**Kotlin:** any version of Kotlin, but currently I use version 2

**Library:** I use jetpack compose to help my development

**Gradle:** currently, I update the gradle to latest version for further implementation

This pattern was heavily inspired from https://github.com/KevinnZou/compose-webview-multiplatform for his awesome pattern.


## Features

- This project mainly focused on building web view for multiplatform. Why? Because currently, I've develop some mobile app using React Native. So, I need the shell for my app.

- This project also focused on building the multiplatform for Android and iOS. I've tried to build the webview for Android and iOS using Kotlin Multiplatform.