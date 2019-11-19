/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package dotnet4j.io.lzo;

import java.util.ArrayDeque;
import java.util.Deque;

import dotnet4j.io.SeekOrigin;
import dotnet4j.io.Stream;
import dotnet4j.io.compression.CompressionMode;


/**
 * SeekableLzoStream.
 *
 * @author <a href="mailto:umjammer@gmail.com Naohide Sano</a> (umjammer)
 * @version 0.00 2019/10/09 umjammer initial version <br>
 */
public class SeekableLzoStream extends LzoStream {

    class Snapshot {
        public final long OutputPosition;

        public final long InputPosition;

        public final int Instruction;

        public final LzoState State;

        private final RingBuffer _ringBuffer;

        public RingBuffer getRingBuffer() {
            return _ringBuffer.clone();
        }

        public Snapshot(long outputPosition, long inputPosition, RingBuffer ringBuffer, int instruction, LzoState state) {
            _ringBuffer = ringBuffer.clone();
            Instruction = instruction;
            State = state;
            InputPosition = inputPosition;
            OutputPosition = outputPosition;
        }
    }

    private final int _snapshotInterval;

    private final Deque<Snapshot> _snapshots;

    /**
     * creates a new seekable lzo stream for decompression
     *
     * @param stream the compressed stream
     * @param mode currently only decompression is supported
     */
    public SeekableLzoStream(Stream stream, CompressionMode mode) {
        this(stream, mode, false);
    }

    /**
     * creates a new seekable lzo stream for decompression
     *
     * @param stream the compressed stream
     * @param mode currently only decompression is supported
     * @param leaveOpen true to leave the stream open after disposing the
     *            LzoStream object; otherwise, false
     */
    public SeekableLzoStream(Stream stream, CompressionMode mode, boolean leaveOpen) {
        this(stream, mode, leaveOpen, 10 * MaxWindowSize);
    }

    /**
     * creates a new seekable lzo stream for decompression
     *
     * @param stream the compressed stream
     * @param mode currently only decompression is supported
     * @param leaveOpen true to leave the stream open after disposing the
     *            LzoStream object; otherwise, false
     * @param snapshotInterval specifies the interval for creating snapshots of
     *            internal state.
     */
    public SeekableLzoStream(Stream stream, CompressionMode mode, boolean leaveOpen, int snapshotInterval) {
        super(stream, mode, leaveOpen);

        _snapshotInterval = snapshotInterval;
        _snapshots = new ArrayDeque<>();
        takeSnapshot();
    }

    protected int decode(byte[] buffer, int offset, int count) {
        takeSnapshot();
        return super.decode(buffer, offset, count);
    }

    @Override
    public boolean canSeek() {
        return false;
    }

    public long seek(long offset, SeekOrigin origin) {
        long position = _outputPosition;
        long targetPosition = offset;
        switch (origin) {
        case Begin:
            break;
        case Current:
            targetPosition += position;
            break;
        case End:
            targetPosition = getLength() + targetPosition;
            break;
        default:
            throw new IllegalArgumentException("origin: " + origin);
        }
        if (targetPosition == position)
            return position;
        if (targetPosition < position) {
            Snapshot snapshot;
            while ((snapshot = _snapshots.peek()).OutputPosition > targetPosition) {
                _snapshots.pop();
            }

            _outputPosition = position = snapshot.OutputPosition;
            _ringBuffer = snapshot.getRingBuffer();
            _instruction = snapshot.Instruction;
            _state = snapshot.State;
            _decodedBuffer = null;
            _source.seek(snapshot.InputPosition, SeekOrigin.Begin);
        }
        if (targetPosition > position) {
            long total = targetPosition - position;
            byte[] buffer = new byte[1024];
            int count = 1024;
            do {
                if (total < count)
                    count = (int) total;
                total -= read(buffer, 0, count);
            } while (total > 0);
        }
        return getPosition();
    }

    private void takeSnapshot() {
        if (_snapshots.size() > 0 && (_snapshots.peek().InputPosition + _snapshotInterval) > _source.getPosition())
            return;
        _snapshots.push(new Snapshot(getPosition(), _source.getPosition(), _ringBuffer, _instruction, _state));
    }
}

/* */
