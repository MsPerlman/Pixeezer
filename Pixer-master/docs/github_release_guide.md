# GitHub Release Guide

This guide explains how to properly build a release APK and publish it to GitHub so that the automated update checker inside Pixer works correctly.

## 1. Version the App

Before building the release, ensure your app version is bumped.

Open `gradle.properties` and increment the version numbers:
```env
APP_VERSION_NAME=1.0.0 # Semantic versioning (e.g., 1.0.0, 1.1.0, 1.1.1)
APP_VERSION_CODE=2 # Increment this integer by 1 for each new release
```

## 2. Build the Release APK

To create a release APK, open a terminal in the root of the project and run:

```bash
./gradlew assembleRelease
```
Wait for the build to finish. The generated APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

## 3. Create a GitHub Release

1. Go to your GitHub repository: [Minuga-RC/Pixer](https://github.com/Minuga-RC/Pixer)
2. On the right sidebar, click **Releases**, then click **Draft a new release**.
3. **Choose a tag:** Click on the dropdown and type `v` followed by your `versionName`. 
   > **IMPORTANT:** The tag **MUST** match the `versionName` in your `build.gradle.kts` exactly, optionally prefixed with `v`. For example, if `versionName = "1.1.0"`, the tag should be `v1.1.0` or `1.1.0`. The update checker compares this tag with the local version.
4. **Target:** Keep it as `master` or `main`.
5. **Release title:** You can use the same tag name (e.g., `Pixer v1.1.0`).
6. **Description:** List the changes, bug fixes, and new features. This description will be shown to the user in the in-app update dialog!
7. **Attach Binaries:** Drag and drop the `app-release.apk` file you built earlier into the "Attach binaries" box.
8. Click **Publish release**.

## 4. Testing the Update Checker

Once published, any user running an older version of the app will automatically see an "Update Available" dialog on their next app launch, prompting them to download the new version.
