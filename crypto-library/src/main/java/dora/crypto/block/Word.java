package dora.crypto.block;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public sealed interface Word permits 
    Word.ShortBacked,
    Word.IntBacked,
    Word.LongBacked {

    Word add(Word other);

    Word sub(Word other);

    Word mul(Word other);

    Word xor(Word other);

    Word rotateLeft(Word distance);

    Word rotateRight(Word distance);

    byte[] toByteArray();

    //region Factories
    static Word fromByteArray(byte[] bytes) {
        return switch (bytes.length) {
            case Short.BYTES -> ShortBacked.fromByteArray(bytes);
            case Integer.BYTES -> IntBacked.fromByteArray(bytes);
            case Long.BYTES -> LongBacked.fromByteArray(bytes);
            default -> throw new IllegalArgumentException("Unsupported word size");
        };
    }

    static Word zero(int bits) {
        return of(0, bits);
    }

    static Word of(long value, int bits) {
        return switch (bits) {
            case Short.SIZE -> new ShortBacked((short) value);
            case Integer.SIZE -> new IntBacked((int) value);
            case Long.SIZE -> new LongBacked(value);
            default -> throw new IllegalArgumentException("Unsupported word size");
        };
    }
    //endregion

    //region Implementation
    record ShortBacked(short value) implements Word {

        public static ShortBacked fromByteArray(byte[] bytes) {
            short result = (short) ((bytes[1] & 0xff) << 8 | (bytes[0] & 0xff));
            return new ShortBacked(result);
        }

        private short unwrap(Word other) {
            if (!(other instanceof ShortBacked(short otherValue)))
                throw new IllegalArgumentException("Incompatible word types");
            return otherValue;
        }

        @Override
        public Word add(Word other) {
            short result = (short) (value + unwrap(other));
            return new ShortBacked(result);
        }

        @Override
        public Word sub(Word other) {
            short result = (short) (value - unwrap(other));
            return new ShortBacked(result);
        }

        @Override
        public Word mul(Word other) {
            short result = (short) (value * unwrap(other));
            return new ShortBacked(result);
        }

        @Override
        public Word xor(Word other) {
            short result = (short) (value ^ unwrap(other));
            return new ShortBacked(result);
        }

        @Override
        public Word rotateLeft(Word distance) {
            int distanceValue = unwrap(distance) & 0xf;
            int unsignedValue = Short.toUnsignedInt(value);
            int result = (unsignedValue << distanceValue)
                       | (unsignedValue >>> (Short.SIZE - distanceValue));
            return new ShortBacked((short) result);
        }

        @Override
        public Word rotateRight(Word distance) {
            int distanceValue = unwrap(distance) & 0xf;
            int unsignedValue = Short.toUnsignedInt(value);
            int result = (unsignedValue >>> distanceValue)
                       | (unsignedValue << (Short.SIZE - distanceValue));
            return new ShortBacked((short) result);
        }

        @Override
        public byte[] toByteArray() {
            return ByteBuffer.allocate(Short.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(value)
                .array();
        }
    }

    record IntBacked(int value) implements Word {

        public static IntBacked fromByteArray(byte[] bytes) {
            int result = (bytes[3] & 0xff) << 24
                       | (bytes[2] & 0xff) << 16
                       | (bytes[1] & 0xff) << 8
                       | (bytes[0] & 0xff);
            return new IntBacked(result);
        }

        private int unwrap(Word other) {
            if (!(other instanceof IntBacked(int otherValue)))
                throw new IllegalArgumentException("Incompatible word types");
            return otherValue;
        }

        @Override
        public Word add(Word other) {
            int result = value + unwrap(other);
            return new IntBacked(result);
        }

        @Override
        public Word sub(Word other) {
            int result = value - unwrap(other);
            return new IntBacked(result);
        }

        @Override
        public Word mul(Word other) {
            int result = value * unwrap(other);
            return new IntBacked(result);
        }

        @Override
        public Word xor(Word other) {
            int result = value ^ unwrap(other);
            return new IntBacked(result);
        }

        @Override
        public Word rotateLeft(Word distance) {
            int result = Integer.rotateLeft(value, unwrap(distance));
            return new IntBacked(result);
        }

        @Override
        public Word rotateRight(Word distance) {
            int result = Integer.rotateRight(value, unwrap(distance));
            return new IntBacked(result);
        }

        @Override
        public byte[] toByteArray() {
            return ByteBuffer.allocate(Integer.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();
        }
    }

    record LongBacked(long value) implements Word {

        public static LongBacked fromByteArray(byte[] bytes) {
            long result = (bytes[7] & 0xffL) << 56
                        | (bytes[6] & 0xffL) << 48
                        | (bytes[5] & 0xffL) << 40
                        | (bytes[4] & 0xffL) << 32
                        | (bytes[3] & 0xffL) << 24
                        | (bytes[2] & 0xffL) << 16
                        | (bytes[1] & 0xffL) << 8
                        | (bytes[0] & 0xffL);
            return new LongBacked(result);
        }

        private long unwrap(Word other) {
            if (!(other instanceof LongBacked(long otherValue)))
                throw new IllegalArgumentException("Incompatible word types");
            return otherValue;
        }

        @Override
        public Word add(Word other) {
            long result = value + unwrap(other);
            return new LongBacked(result);
        }

        @Override
        public Word sub(Word other) {
            long result = value - unwrap(other);
            return new LongBacked(result);
        }

        @Override
        public Word mul(Word other) {
            long result = value * unwrap(other);
            return new LongBacked(result);
        }

        @Override
        public Word xor(Word other) {
            long result = value ^ unwrap(other);
            return new LongBacked(result);
        }

        @Override
        public Word rotateLeft(Word distance) {
            long distanceValue = unwrap(distance);
            long result = value << distanceValue | (value >>> -distanceValue);
            return new LongBacked(result);
        }

        @Override
        public Word rotateRight(Word distance) {
            long distanceValue = unwrap(distance);
            long result = (value >>> distanceValue) | (value << -distanceValue);
            return new LongBacked(result);
        }

        @Override
        public byte[] toByteArray() {
            return ByteBuffer.allocate(Long.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(value)
                .array();
        }
    }
    //endregion
}
