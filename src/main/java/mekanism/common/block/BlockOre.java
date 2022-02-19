package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.resource.OreType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.ToolType;

public class BlockOre extends Block implements IHasDescription {

    private final OreType ore;
    private String descriptionTranslationKey;

    public BlockOre(OreType ore) {
        super(BlockStateHelper.applyLightLevelAdjustments(AbstractBlock.Properties.of(Material.STONE).strength(3, 3)
              .requiresCorrectToolForDrops().harvestTool(ToolType.PICKAXE).harvestLevel(1)));
        this.ore = ore;
    }

    public String getDescriptionTranslationKey() {
        if (descriptionTranslationKey == null) {
            descriptionTranslationKey = Util.makeDescriptionId("description", getRegistryName());
        }
        return descriptionTranslationKey;
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return this::getDescriptionTranslationKey;
    }

    @Override
    public int getExpDrop(BlockState state, IWorldReader reader, BlockPos pos, int fortune, int silkTouch) {
        if (ore.getMaxExp() > 0 && silkTouch == 0) {
            return MathHelper.nextInt(RANDOM, ore.getMinExp(), ore.getMaxExp());
        }
        return super.getExpDrop(state, reader, pos, fortune, silkTouch);
    }
}