package mekanism.common.lib.mesh;

import net.minecraft.util.math.BlockPos;

public interface IMeshNode {

    public Structure getStructure();

    public void setStructure(Structure structure);

    public default void resetStructure() {
        setStructure(new Structure(this));
    }

    public BlockPos getPos();
}
