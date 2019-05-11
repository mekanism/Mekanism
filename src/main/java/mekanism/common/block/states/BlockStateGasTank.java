package mekanism.common.block.states;

import mekanism.common.block.BlockGasTank;
import mekanism.common.tier.GasTankTier;
import net.minecraft.block.properties.PropertyEnum;

/**
 * Created by ben on 23/12/14.
 */
public class BlockStateGasTank extends BlockStateFacing {

    public static final PropertyEnum<GasTankTier> typeProperty = PropertyEnum.create("tier", GasTankTier.class);

    public BlockStateGasTank(BlockGasTank block) {
        super(block, typeProperty);
    }
}