package com.iflytek.seopt;

public class VoiceByte {
    public byte[] localBytes;

    public VoiceByte(byte[] bytes) {
        if (bytes == null) {
            return;
        }
        localBytes = new byte[bytes.length];
        System.arraycopy(bytes, 0, localBytes, 0, localBytes.length);
    }

    public byte[] getLocalBytes() {
        return localBytes;
    }
}
