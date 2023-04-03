JAVA_HOME=/opt/android-studio/jbr
export JAVA_HOME
./gradlew app:packageGoogleReleaseUniversalApk

# from AndroidStudio:
# out=app/build/outputs/apk/google/release/app-google-release.apk
# from gradlew:
# AGP 4.2.2: out=app/build/outputs/universal_apk/googleRelease/app-google-release-universal.apk
# AGP 7+: (AGP=Android Gradle Plugin)
out=app/build/outputs/apk_from_bundle/googleRelease/app-google-release-universal.apk
ls -l $out
apksigner verify --print-certs --verbose $out
