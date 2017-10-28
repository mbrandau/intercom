package de.maximilianbrandau.intercom.encoding.net;

public enum PacketType {

    AUTH((byte) 0),
    AUTH_RESPONSE((byte) 1),
    PING((byte) 10),
    REQUEST((byte) 100),
    RESPONSE((byte) 101),
    PUSH((byte) 120);

    private final byte id;

    PacketType(byte id) {
        this.id = id;
    }

    public static PacketType getById(byte id) {
        PacketType[] types = values();
        for (PacketType type : types) if (type.getId() == id) return type;
        return null;
    }

    public byte getId() {
        return id;
    }

}
