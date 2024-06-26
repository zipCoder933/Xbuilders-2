package com.xbuilders.engine.world.chunk;

import java.io.*;

public class XBFilterOutputStream extends FilterOutputStream {

    final static byte FILTERED_BYTE = ChunkSavingLoading.NEWLINE_BYTE;

    public XBFilterOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        if (b == FILTERED_BYTE) {
            throw new IllegalArgumentException("ChunkSavingLoading: The byte [-128] is forbidden for use.");
        } else {
            super.write(b);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for (int i = 0; i < b.length; i++) {
            if (b[i] != FILTERED_BYTE) {
                super.write(b[i]);
            }
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            if (b[i] != FILTERED_BYTE) {
                super.write(b[i]);
            }
        }
    }
}