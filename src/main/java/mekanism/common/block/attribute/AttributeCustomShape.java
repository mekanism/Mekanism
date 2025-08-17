package mekanism.common.block.attribute;

import java.util.Arrays;
import net.minecraft.world.phys.shapes.VoxelShape;

public record AttributeCustomShape(VoxelShape[] bounds) implements Attribute {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof AttributeCustomShape(VoxelShape[] otherBounds) && Arrays.equals(bounds, otherBounds);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bounds);
    }
}
