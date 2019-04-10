package mekanism.common.block.states;

import mekanism.common.block.BlockEnergyCube;
import mekanism.common.tier.EnergyCubeTier;
import net.minecraft.block.properties.PropertyEnum;

/**
 * Created by ben on 23/12/14.
 */
public class BlockStateEnergyCube extends BlockStateFacing {

    public static final PropertyEnum<EnergyCubeTier> typeProperty = PropertyEnum
          .create("tier", EnergyCubeTier.class);

    public BlockStateEnergyCube(BlockEnergyCube block) {
        super(block, typeProperty);
    }
}
