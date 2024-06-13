package mekanism.common.block;

import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.resource.ore.OreType;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class BlockOre extends Block implements IHasDescription {

    private final OreType ore;
    private String descriptionTranslationKey;

    public BlockOre(OreType ore) {
        this(ore, BlockStateHelper.applyLightLevelAdjustments(BlockBehaviour.Properties.of().strength(3, 3).requiresCorrectToolForDrops()
              .mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)));
    }

    public BlockOre(OreType ore, BlockBehaviour.Properties properties) {
        super(properties);
        this.ore = ore;
    }

    @NotNull
    public String getDescriptionTranslationKey() {
        if (descriptionTranslationKey == null) {
            descriptionTranslationKey = Util.makeDescriptionId("description", Mekanism.rl(ore.getResource().getRegistrySuffix() + "_ore"));
        }
        return descriptionTranslationKey;
    }

    @NotNull
    @Override
    public ILangEntry getDescription() {
        return this::getDescriptionTranslationKey;
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader reader, RandomSource random, BlockPos pos) {
        //Note: If min exp = max exp = 0, then this will just return zero, similar to what super does
        return Mth.nextInt(random, ore.getMinExp(), ore.getMaxExp());
    }
}