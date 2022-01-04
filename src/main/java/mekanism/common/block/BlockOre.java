package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.resource.OreType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;

public class BlockOre extends Block implements IHasDescription {

    private final OreType ore;
    private String descriptionTranslationKey;

    public BlockOre(OreType ore) {
        super(BlockStateHelper.applyLightLevelAdjustments(BlockBehaviour.Properties.of(Material.STONE).strength(3, 3)
              .requiresCorrectToolForDrops()));
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
    public int getExpDrop(BlockState state, LevelReader reader, BlockPos pos, int fortune, int silkTouch) {
        if (ore.getMaxExp() > 0 && silkTouch == 0) {
            return Mth.nextInt(RANDOM, ore.getMinExp(), ore.getMaxExp());
        }
        return super.getExpDrop(state, reader, pos, fortune, silkTouch);
    }
}