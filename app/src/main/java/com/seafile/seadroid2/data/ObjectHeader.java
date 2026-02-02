package com.seafile.seadroid2.data;

import java.nio.ByteBuffer;

public class ObjectHeader {
    private byte[] objId; // Equivalent to "!40s"
    private int objSize;  // Equivalent to "I"

    public ObjectHeader(byte[] objId, int objSize) {
        this.objId = objId;
        this.objSize = objSize;
    }

    public static ObjectHeader fromBytes(ByteBuffer buffer) {
        byte[] objId = new byte[40];
        buffer.get(objId); // Read 40 bytes for obj_id
        int objSize = buffer.getInt(); // Read 4 bytes for obj_size

        return new ObjectHeader(objId, objSize);
    }

    public byte[] getObjId() {
        return objId;
    }

    public int getObjSize() {
        return objSize;
    }
}
