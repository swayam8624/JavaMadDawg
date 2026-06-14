package com.singal.maddog.net;

/**
 * Defines the UDP network message packet format and structure.
 */
public class Packet {
    public enum PacketType {
        INVALID((byte) -1),
        LOGIN((byte) 0x01),
        DISCONNECT((byte) 0x02),
        MOVE((byte) 0x03);

        private final byte id;

        PacketType(byte id) {
            this.id = id;
        }

        public byte getId() {
            return id;
        }

        public static PacketType lookup(byte id) {
            for (PacketType t : values()) {
                if (t.id == id) return t;
            }
            return INVALID;
        }
    }

    private final PacketType type;
    private final byte[] data;

    public Packet(PacketType type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public PacketType getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    /**
     * Serializes this packet into a raw byte array.
     * Format: [1 byte Type ID] [n bytes Payload Data]
     */
    public byte[] serialize() {
        byte[] buffer = new byte[data.length + 1];
        buffer[0] = type.getId();
        System.arraycopy(data, 0, buffer, 1, data.length);
        return buffer;
    }

    public static Packet deserialize(byte[] buffer, int length) {
        if (length <= 0) return new Packet(PacketType.INVALID, new byte[0]);
        
        PacketType type = PacketType.lookup(buffer[0]);
        byte[] data = new byte[length - 1];
        System.arraycopy(buffer, 1, data, 0, length - 1);
        return new Packet(type, data);
    }
}
