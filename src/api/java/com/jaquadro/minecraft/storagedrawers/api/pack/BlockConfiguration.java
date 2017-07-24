package com.jaquadro.minecraft.storagedrawers.api.pack;

import com.jaquadro.minecraft.storagedrawers.api.storage.EnumBasicDrawer;

public enum BlockConfiguration
{
    BasicFull1(BlockType.Drawers, EnumBasicDrawer.FULL1),
    BasicFull2(BlockType.Drawers, EnumBasicDrawer.FULL2),
    BasicFull4(BlockType.Drawers, EnumBasicDrawer.FULL4),
    BasicHalf2(BlockType.Drawers, EnumBasicDrawer.HALF2),
    BasicHalf4(BlockType.Drawers, EnumBasicDrawer.HALF4),
    Trim(BlockType.Trim, null);

    private final BlockType type;
    private final EnumBasicDrawer drawer;

    BlockConfiguration (BlockType type, EnumBasicDrawer drawer) {
        this.type = type;
        this.drawer = drawer;
    }

    public BlockType getBlockType () {
        return type;
    }

    public int getDrawerCount () {
        return (drawer != null) ? drawer.getDrawerCount() : 0;
    }

    public boolean isHalfDepth () {
        return (drawer != null) && drawer.isHalfDepth();
    }

    public static BlockConfiguration by (BlockType type, EnumBasicDrawer drawer) {
        for (BlockConfiguration config : values()) {
            if (config.type == type && config.drawer == drawer)
                return config;
        }

        return null;
    }
}
