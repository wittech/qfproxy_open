package com.qunar.qfproxy.utils.imgtype;


import java.io.IOException;
import java.io.InputStream;


public class InputStreamWrapper extends InputStream {
//    private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamWrapper.class);
//    private static final int MAX_SKIP_BUFFER_SIZE = 2048;

    private InputStream inputStream;
    private byte[] buffer;
    private int bufferSize = 16;
    private int loc = 0;
    private int bufferActualLength = -1;


    public InputStreamWrapper(InputStream is) {
        this.inputStream = is;
    }

    public InputStreamWrapper(InputStream inputStream, int bufferSize) {
        this.inputStream = inputStream;
        this.bufferSize = bufferSize;
    }

    @Override
    public int read() throws IOException {
        if (loc < bufferActualLength) {
            return buffer[loc++] & 0xff;
        } else {
            return inputStream.read();
        }
    }

    public int available() throws IOException {
        return bufferActualLength > -1 ? bufferActualLength - loc + inputStream.available() : inputStream.available();
    }

    public void close() throws IOException {
        inputStream.close();
    }

    public synchronized void mark(int readLimit) {
        if (!markSupported()) {
            return;
        }
        if (bufferActualLength > -1 && readLimit <= bufferActualLength) {
            loc = readLimit;
        } else {
            inputStream.mark(readLimit - bufferActualLength);
        }
    }

    public synchronized void reset() throws IOException {
        if (!markSupported()) {
            throw new IOException("mark/reset not supported");
        }

        if (bufferActualLength > -1) {
            loc = 0;
        }
        inputStream.reset();
    }

    public boolean markSupported() {
        return inputStream.markSupported();
    }


    public int readToBuffer() throws IOException {
        if (bufferActualLength == -1) {
            buffer = new byte[bufferSize];
            bufferActualLength = inputStream.read(buffer);
        }

        return bufferActualLength;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public static InputStreamWrapper createBufferWrapper(InputStream inputStream) throws IOException {
        InputStreamWrapper isw;
        if (inputStream instanceof InputStreamWrapper) {
            isw = (InputStreamWrapper) inputStream;
            isw.readToBuffer();
        } else {
            isw = new InputStreamWrapper(inputStream);
            isw.readToBuffer();
        }
        return isw;
    }
}
