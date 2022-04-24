package mekanism.common.block.attribute;

import net.minecraft.world.phys.shapes.VoxelShape;

public class AttributeCustomShape implements Attribute {

    private final VoxelShape[] bounds;

    public AttributeCustomShape(VoxelShape[] bounds) {
        this.bounds = bounds;
    }

    public VoxelShape[] getBounds() {
        return bounds;
    }
}
