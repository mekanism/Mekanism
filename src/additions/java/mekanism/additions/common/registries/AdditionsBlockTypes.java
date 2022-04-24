package mekanism.additions.common.registries;

import mekanism.additions.common.AdditionsLang;
import mekanism.additions.common.content.blocktype.BlockShapes;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeStateFacing.FacePlacementType;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockType.BlockTypeBuilder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AdditionsBlockTypes {

    private AdditionsBlockTypes() {
    }

    // Glow Panel
    public static final BlockType GLOW_PANEL = BlockTypeBuilder
          .createBlock(AdditionsLang.DESCRIPTION_GLOW_PANEL)
          .withCustomShape(BlockShapes.GLOW_PANEL)
          .with(new AttributeStateFacing(BlockStateProperties.FACING, FacePlacementType.SELECTED_FACE))
          .withLight(15)
          .build();
}
