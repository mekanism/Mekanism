package mekanism.common.block.attribute;

import com.mojang.datafixers.util.Either;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

public record AttributeCustomShape(Either<VoxelShape, Map<Direction, VoxelShape>> bounds) implements Attribute {
}
