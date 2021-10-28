# WindX

WindX is an **free and open source** third-party Telegram client, based on Telegram-FOSS and NekoX with features added.

## WindX Changes

- Wait more :)

## Compilation Guide

**NOTE: Building on Windows is, unfortunately, not supported. Fuck Windows :)
Consider using a Linux VM or dual booting.**

**Important:**

1. Install Android SDK and NDK (default location is $HOME/Android/SDK, otherwise you need to specify $ANDROID_HOME for it)

It is recommended to use [AndroidStudio](https://developer.android.com/studio) to install.

2. Install golang ( 1.16 ).
```shell
# debian sid
apt install -y golang-1.16
```

3. Install Rust and its stdlib for Android ABIs, and add environment variables for it.

It is recommended to use the official script, otherwise you may not find rustup.

```shell
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- --default-toolchain none -y
echo "source \$HOME/.cargo/env" >> $HOME/.bashrc && source $HOME/.cargo/env

rustup install $(cat ss-rust/src/main/rust/shadowsocks-rust/rust-toolchain)
rustup default $(cat ss-rust/src/main/rust/shadowsocks-rust/rust-toolchain)
rustup target install armv7-linux-androideabi aarch64-linux-android i686-linux-android x86_64-linux-android
```

4. Build native dependencies: `./run init libs`
5. Build external libraries and native code: `./run libs update`
6. Fill out `TELEGRAM_APP_ID` and `TELEGRAM_APP_HASH` in `local.properties`
7. Replace TMessagesProj/google-services.json if you want fcm to work.
8. Replace release.keystore with yours and fill out `ALIAS_NAME`, `KEYSTORE_PASS` and `ALIAS_PASS` in `local.properties` if you want a custom sign key.

`./gradlew assemble<Full/Mini><Debug/Release/ReleaseNoGcm>`

## Localization

### Unofficial translations

Current built-in language packs:

* 简体中文: [moecn](https://translations.telegram.org/moecn)
* 正體中文: [taiwan](https://translations.telegram.org/taiwan)
* 日本語: [ja_raw](https://translations.telegram.org/ja_raw)

You can [open an issue to](https://github.com/NekoX-Dev/NekoX/issues/new?&template=language_request.md) request to amend these built-in translation.
