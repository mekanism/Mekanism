package mekanism.common.block;

import javax.annotation.Nonnull;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.resource.ore.OreType;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class BlockOre extends Block implements IHasDescription {

    private final OreType ore;
    private String descriptionTranslationKey;

    public BlockOre(OreType ore) {
        this(ore, BlockBehaviour.Properties.of(Material.STONE).strength(3, 3).requiresCorrectToolForDrops());
    }

    public BlockOre(OreType ore, BlockBehaviour.Properties properties) {
        super(BlockStateHelper.applyLightLevelAdjustments(properties));
        this.ore = ore;
    }

    public String getDescriptionTranslationKey() {
        if (descriptionTranslationKey == null) {
            descriptionTranslationKey = Util.makeDescriptionId("description", Mekanism.rl(ore.getResource().getRegistrySuffix() + "_ore"));
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