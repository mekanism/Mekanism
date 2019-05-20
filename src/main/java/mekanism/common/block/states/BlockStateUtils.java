package mekanism.common.block.states;

import java.util.function.Predicate;
import net.minecraft.util.EnumFacing;

/**
 * Created by Thiakil on 18/04/2019.
 */
public class BlockStateUtils {

    public static final Predicate<EnumFacing> ALL_FACINGS = enumFacing -> true;
    public static final Predicate<EnumFacing> NO_ROTATION = enumFacing -> false;
}