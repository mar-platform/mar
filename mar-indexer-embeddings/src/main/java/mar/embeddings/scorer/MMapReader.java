package mar.embeddings.scorer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.indeed.util.mmap.MMapBuffer;

import io.github.jbellis.jvector.disk.RandomAccessReader;

// Copied from: https://github.com/jbellis/jvector/blob/main/jvector-examples/src/main/java/io/github/jbellis/jvector/example/util/MMapReader.java
public class MMapReader implements RandomAccessReader {
    private final MMapBuffer buffer;
    private long position;
    private byte[] scratch = new byte[0];

    MMapReader(MMapBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void seek(long offset) {
        position = offset;
    }

    public int readInt() {
        try {
            return buffer.memory().getInt(position);
        } finally {
            position += Integer.BYTES;
        }
    }

    /*
    @Override
    public float readFloat() throws IOException {
        try {
            return buffer.memory().getFloat(position);
        } finally {
            position += Float.BYTES;
        }
    }
    */

    public void readFully(byte[] bytes) {
        read(bytes, 0, bytes.length);
    }

    public void readFully(ByteBuffer buffer) {
        var length = buffer.remaining();
        try {
            this.buffer.memory().getBytes(position, buffer);
        } finally {
            position += length;
        }
    }

    private void read(byte[] bytes, int offset, int count) {
        try {
            buffer.memory().getBytes(position, bytes, offset, count);
        } finally {
            position += count;
        }
    }

    @Override
    public void readFully(float[] floats) {
        int bytesToRead = floats.length * Float.BYTES;
        if (scratch.length < bytesToRead) {
            scratch = new byte[bytesToRead];
        }
        read(scratch, 0, bytesToRead);
        ByteBuffer byteBuffer = ByteBuffer.wrap(scratch).order(ByteOrder.BIG_ENDIAN);
        byteBuffer.asFloatBuffer().get(floats);
    }

    @Override
    public void read(int[] ints, int offset, int count) {
        int bytesToRead = count * Integer.BYTES;
        if (scratch.length < bytesToRead) {
            scratch = new byte[bytesToRead];
        }
        read(scratch, 0, bytesToRead);
        ByteBuffer byteBuffer = ByteBuffer.wrap(scratch).order(ByteOrder.BIG_ENDIAN);
        byteBuffer.asIntBuffer().get(ints, offset, count);
    }

    /*
    @Override
    public void read(float[] floats, int offset, int count) {
        int bytesToRead = count * Float.BYTES;
        if (scratch.length < bytesToRead) {
            scratch = new byte[bytesToRead];
        }
        read(scratch, 0, bytesToRead);
        ByteBuffer byteBuffer = ByteBuffer.wrap(scratch).order(ByteOrder.BIG_ENDIAN);
        byteBuffer.asFloatBuffer().get(floats, offset, count);
    }
    */

    @Override
    public void readFully(long[] vector) {
        int bytesToRead = vector.length * Long.BYTES;
        if (scratch.length < bytesToRead) {
            scratch = new byte[bytesToRead];
        }
        read(scratch, 0, bytesToRead);
        ByteBuffer byteBuffer = ByteBuffer.wrap(scratch).order(ByteOrder.BIG_ENDIAN);
        byteBuffer.asLongBuffer().get(vector);
    }

    @Override
    public void close() {
        // don't close buffer, let the Supplier handle that
    }
}