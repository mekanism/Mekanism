package mekanism.common.block.attribute;

import net.minecraft.world.phys.shapes.VoxelShape;

public record AttributeCustomShape(VoxelShape[] bounds) implements Attribute {
}
