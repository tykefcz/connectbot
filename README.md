[![Build Status](https://travis-ci.com/connectbot/connectbot.svg?branch=master)](
https://travis-ci.com/connectbot/connectbot)

# ConnectBot

ConnectBot is a [Secure Shell](https://en.wikipedia.org/wiki/Secure_Shell)
client for Android that lets you connect to remote servers over a
cryptographically secure link.

## Extensions

For Honeywell CK65 Android 
 - Hardware keyboard F1..F12 support
 - Setting + Host management : Available only in Provisioning Mode
 - Export/Import settings : Deploy exported file from one device to others
   (in Downloads/droidssh.xml) - ConnectBot/DroidSSH automaticaly import newer file than last imported on startup.
 - CornerMode - visible only upper left ?? cols/rows from "virtual size"
 - Change emulation per host
 - Ask for username (use question ? as username)
 - Can save plain text password
 - Username / Password from barcode scan
 - Enable/Disable AIM Barcode prefixes (for all barcodes)
 - Set EAN128 (Code128 SSCC barcode) custom prefix
 - Replace FN1 in Code128 by user defined sequence (default \x1d )
 - Set barcode suffix (\x0d = enter, \x09 = Tab)
 - AutoConnect on statrup (Not yet implemented - only switch in Host Settings)
 - Device info on ESC[8n sequence (Madeta extension) try # echo -e '\e[8n' ; cat -
 - Device Serial + System Build ID answerback (\005) try # echo -e '\005'; cat -

## Compiling

### Android Studio

ConnectBot is most easily developed in [Android Studio](
https://developer.android.com/studio/). You can import this project
directly from its project creation screen by importing from the GitHub URL.

### Command line

To compile ConnectBot using `gradlew`, you must first specify where your
Android SDK is via the `ANDROID_SDK_HOME` environment variable. Then
you can invoke the Gradle wrapper to build:

```sh
./gradlew build
```

### Reproducing Continuous Integration (CI) builds locally

To run the Jenkins CI pipeline locally, you can use
`jenkinsfile-runner` via a Docker installation which can be invoked like
this:

```sh
docker run -it -v $(pwd):/workspace \
    -v jenkinsfile-runner-cache:/var/jenkinsfile-runner-cache \
    -v jenkinsfile-runner:/var/jenkinsfile-runner \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v $(which docker):$(which docker) \
    -e ANDROID_ADB_SERVER_ADDRESS=host.docker.internal \
    jenkins/jenkinsfile-runner
```


## Translations

If you'd like to correct or contribute new translations to ConnectBot,
then head on over to [ConnectBot's translations project](
https://translations.launchpad.net/connectbot/trunk/+pots/fortune)
