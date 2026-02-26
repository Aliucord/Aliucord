# `org/webrtc`
This code is lifted from https://github.com/webrtc-sdk/webrtc/tree/13e377b804f68aa9c20ea5e449666ea5248e3286:
- [`sdk/android/api/org/webrtc`](https://github.com/webrtc-sdk/webrtc/commit/13e377b804f68aa9c20ea5e449666ea5248e3286/sdk/android/api/org/webrtc)
- [`sdk/android/src/java/org/webrtc`](https://github.com/webrtc-sdk/webrtc/commit/13e377b804f68aa9c20ea5e449666ea5248e3286/sdk/android/src/java/org/webrtc)
- [`rtc_base/java/src/org/webrtc`](https://github.com/webrtc-sdk/webrtc/commit/13e377b804f68aa9c20ea5e449666ea5248e3286/rtc_base/java/src/org/webrtc)
- [`api/video/video_frame_buffer.h`](https://github.com/webrtc-sdk/webrtc/commit/13e377b804f68aa9c20ea5e449666ea5248e3286/api/video/video_frame_buffer.h) (VideoFrameBuffer.Type) -> `org/webrtc/VideoFrameBufferType.java`

`org/webrtc/EglBaseFactory.kt` is written so that `h0.c.n0` can access the package-private `EglBase14Impl` class.

To find the correct commit when updating the voice lib, search for `WebRTC source stamp` in `libdiscord.so` strings,
then look for history of `call/version.cc` in WebRTC's source code to find the commit setting this source stamp.

WebRTC is licensed under [BSD 3-Clause](https://github.com/webrtc-sdk/webrtc/blob/13e377b804f68aa9c20ea5e449666ea5248e3286/LICENSE)
