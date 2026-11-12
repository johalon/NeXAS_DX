package com.giga.nexas.dto.bhe.spm.hitbox;

import com.giga.nexas.dto.bhe.spm.Spm;
import com.giga.nexas.io.BinaryReader;
import lombok.Data;

import java.io.IOException;

@Data
public class CRotatableBox extends Spm.SPMHitArea {

    // 中心点坐标。
    private Integer centerX;
    private Integer centerY;
    private Integer centerZ;
    // 尺寸（宽、高、深）。
    private Integer sizeX;
    private Integer sizeY;
    private Integer sizeZ;
    // 预留字段，目前样本中恒为 0。
    private Integer reserved0;
    private Integer reserved1;
    // 低 16 位的属性标志。
    private Integer propertyFlags;

    @Override
    public void readInfo(BinaryReader reader) throws IOException {
        centerX = reader.readInt();
        centerY = reader.readInt();
        centerZ = reader.readInt();
        sizeX = reader.readInt();
        sizeY = reader.readInt();
        sizeZ = reader.readInt();
        reserved0 = reader.readInt();
        reserved1 = reader.readInt();
        int raw = reader.readInt();
        propertyFlags = raw & 0xFFFF;
    }

    @Override
    public com.giga.nexas.dto.bsdx.spm.Spm.SPMHitArea transHitbox() {
        int w = valueOrZero(sizeX);
        int h = valueOrZero(sizeY);
        int d = valueOrZero(sizeZ);
        int cx = valueOrZero(centerX);
        int cy = valueOrZero(centerY);
        int cz = valueOrZero(centerZ);
        // PATCH(proj): centre +- size/2 with float half truncated toward zero (matches misaki2 sou.spm)
        int left = (int) (cx - w / 2.0);
        int top = (int) (cy - h / 2.0);
        int right = (int) (cx + w / 2.0);
        int bottom = (int) (cy + h / 2.0);
        int zMin = (int) (cz - d / 2.0);
        int zMax = (int) (cz + d / 2.0);
        return buildBsdxHitArea(left, top, right, bottom, zMin, zMax, null);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
