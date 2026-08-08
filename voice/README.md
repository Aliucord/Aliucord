# voice

Codenamed **Sunflower**.

This module houses the translation layer from DiscordKt's `libdiscord.so` to RNA's `libdiscord.so`.

`libdiscord.so` is the native C++ library responsible for sending and receiving data over the voice connection.
It is also responsible for encrypting and decrypting this data and sending it straight over to the device's audio IO.

DiscordKt's library is old and does not support, amongst other things:
- New transport encryption algorithms; the old ones are almost entirely phased out
- End-to-end encryption, also known as DAVE.

For the DAVE implementation, see the [coreplugin](../Aliucord/src/main/java/com/aliucord/coreplugins/voice).

## Structure

This module translates the old native calls to the new ones:
- `co.discord.media_engine.Connection` translates to `com.discord.native.engine.NativeConnection`
- `com.hammerandchisel.libdiscord.Discord` translates to `com.discord.native.engine.NativeEngine`

For organisation, the original methods of each class are stored in `IConnection` and `IDiscord` respectively, to
ensure that method signatures aren't accidentally changed. Callback interfaces however must still live in `Connection` and
`Discord`, these also must not be changed.

## Development
- These docs demonstrate black box reverse engineering. It is possible to decompile and inspect RNA's code,
  but after Hermes this is considerably more difficult to understand. If you are comfortable doing so, you
  may inspect it for some deeper insight.
- The voice module version is pinned to each `libdiscord.so` version. This version can be found in RNA's
  `AndroidManifest.xml`.

### Requirements
- A rooted device. It is possible to do this on an unrooted device but it would be quite a hassle
  to do so. If you don't have one, use an emulator.
- Frida: used to intercept and inspect native calls
- Some form of https mitm with websocket support. mitmproxy is recommended.
- The latest Discord app from your favourite app store.

### Finding JNI signatures
Use [jadx](https://github.com/skylot/jadx) to decompile RNA's Java code and find the JNI interfaces in `com.discord.native.engine`.

### Dumping all native JNI methods
Use `nm -D` to dump the symbol table of `libdiscord.so`. Grep this for `Java_` to find all
JNI methods. This shouldn't be necessary if you already used jadx, but here it is anyway.

### Frida
- Install the cli tools: https://frida.re/docs/installation/
- Follow the official installation steps for Android: https://frida.re/docs/android/
- Use `frida-trace` to intercept native jni calls. A good command is:
  ```sh
  frida-trace -U -N com.discord \
  -j 'com.discord.native.engine.NativeEngine!*' \
  -j 'com.discord.native.engine.NativeConnection!*' \
  -j '*com.discord.media.engine.*!on*' \
  -J '*MediaEngine*!*' \
  -J '*!onStats' \
  -J '*EglRenderer!*' \
  -J '*VideoStreamViewManager!*' \
  -J '*VideoStreamTextureView!*' \
  -J '*VideoSink!*' \
  -J '*Connection!getFilteredStats'
  ```
- Notes:
    - `-N` will launch and instrument the app. Use `-f` instead to instrument an already-running Discord app.
    - Sometimes some classes are not loaded and the above command will miss them. Most notably these are the
      `com.discord.media.engine.*!on*` event handlers. You might have to join a voice call and mess around a bit
      to get these event handlers to load. Then you can run the command and instrument them.
    - For transport options, there is no strongly typed interface anywhere apart from reading the native code that
      parses the JSON. You can instrument setTransportOptions function to find all the options set.

### mitmproxy
- Install mitmproxy: https://docs.mitmproxy.org/stable/overview/installation/
- Configure the proxy: https://docs.mitmproxy.org/stable/concepts/modes/
    - You may use either Regular or WireGuard proxy mode. WireGuard is more reliable but a bit more
      difficult to set up.
- Install the certificate: https://docs.mitmproxy.org/stable/concepts/certificates/
- Install the [AlwaysTrustUserCerts](https://github.com/NVISOsecurity/AlwaysTrustUserCerts) module
    - Discord does not trust user certs (the one we installed above), so this is required to intercept all HTTPS traffic.
- Use the mitm proxy to peek at the voice gateway websocket connection. This is usually `wss://[region].discord.media:[port]`.
