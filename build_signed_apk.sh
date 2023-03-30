JAVA_HOME=/opt/android-studio/jbr
export JAVA_HOME
./gradlew app:packageGoogleReleaseUniversalApk

# from AndroidStudio:
# out=app/google/release/app-google-release.apk
# from gradlew:
out=app/build/outputs/universal_apk/googleRelease/app-google-release-universal.apk
ls -l $out
apksigner verify --verbose $out
