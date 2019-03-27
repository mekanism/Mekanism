package mekanism.common.block.states;

import mekanism.common.block.BlockGlowPanel;
import mekanism.common.block.property.PropertyColor;
import net.minecraft.block.properties.IProperty;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockStateGlowPanel extends ExtendedBlockState {

    public BlockStateGlowPanel(BlockGlowPanel block) {
        super(block, new IProperty[]{BlockStateFacing.facingProperty}, new IUnlistedProperty[]{PropertyColor.INSTANCE});
    }
}
