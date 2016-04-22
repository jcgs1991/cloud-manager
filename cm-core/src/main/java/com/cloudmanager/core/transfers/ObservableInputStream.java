package com.cloudmanager.core.transfers;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

/**
 * InputStream that notifies listeners of its progress.
 */
public class ObservableInputStream<T> extends InputStream {
    private static final int percentStep = 1;

    private final InputStream contentStream;
    private final long size;
    private final T object;
    private final long countStep;

    private long counter = 0;
    private long lastNotifyCounter = 0;

    private BiConsumer<T, Double> listener;

    public ObservableInputStream(InputStream in, long size, T object) {
        contentStream = in;
        this.size = size;
        this.object = object;

        this.countStep = (size * percentStep) / 100;
    }

    public void addListener(BiConsumer<T, Double> otherListener) {
        if (listener == null)
            listener = otherListener;
        else
            listener = listener.andThen(otherListener);
    }

    @Override
    public int read() throws IOException {
        counter += 1;
        check();
        return contentStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = contentStream.read(b);
        counter += read;
        check();
        return read;
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        int read = contentStream.read(b, offset, length);
        counter += read;
        check();
        return read;
    }

    private void check() {
        double percent;

        // If we are over the limit, just return 100
        if (counter >= size) {
            percent = 100;

        } else { // If we havent finished, check if we have to notify
            if (counter - lastNotifyCounter < countStep)
                return; // We don't have to notify yet

            percent = (counter * 100d) / size;
        }

        // Notify the listener
        listener.accept(object, percent);
        lastNotifyCounter = counter;
    }

    @Override
    public void close() throws IOException {
        contentStream.close();
    }

    @Override
    public int available() throws IOException {
        return contentStream.available();
    }

    @Override
    public void mark(int readlimit) {
        contentStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        contentStream.reset();
    }

    @Override
    public boolean markSupported() {
        return contentStream.markSupported();
    }

    @Override
    public long skip(long n) throws IOException {
        return contentStream.skip(n);
    }
}
