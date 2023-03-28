JAVA_HOME=/opt/android-studio/jbr
export JAVA_HOME
# ./gradlew app:packageFullReleaseUniversalApk app:packageMiniReleaseUniversalApk
ls -l /home/gabriel/AndroidStudioProjects/connectbot/app/google/release/app-google-release.apk
apksigner verify --verbose /home/gabriel/AndroidStudioProjects/connectbot/app/google/release/app-google-release.apk
#jarsigner -keystore madeta.jks /home/gabriel/AndroidStudioProjects/connectbot/app/google/release/app-google-release.apk madeta
