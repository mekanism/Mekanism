package mekanism.common.lib.multiblock;

import mekanism.common.lib.math.IShape;
import mekanism.common.lib.multiblock.UpdateProtocol.FormationResult;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public interface IStructureValidator {

    boolean isValid();

    IShape getShape();

    FormationResult validate(UpdateProtocol<?> protocol, UpdateProtocol<?>.ValidationContext ctx);

    Direction getSide(BlockPos pos);
}
