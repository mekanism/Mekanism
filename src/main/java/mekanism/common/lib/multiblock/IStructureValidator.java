package mekanism.common.lib.multiblock;

import mekanism.common.lib.math.IShape;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public interface IStructureValidator {

    boolean checkValid();

    IShape getShape();

    FormationResult validate(FormationProtocol<?> protocol, FormationProtocol<?>.ValidationContext ctx);

    Direction getSide(BlockPos pos);
}
