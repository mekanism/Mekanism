package mekanism.common.block.transmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IBlockOreDict;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.CableTier;
import mekanism.common.tile.transmitter.universal_cable.TileEntityAdvancedUniversalCable;
import mekanism.common.tile.transmitter.universal_cable.TileEntityBasicUniversalCable;
import mekanism.common.tile.transmitter.universal_cable.TileEntityEliteUniversalCable;
import mekanism.common.tile.transmitter.universal_cable.TileEntityUltimateUniversalCable;
import mekanism.common.tile.transmitter.universal_cable.TileEntityUniversalCable;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockReader;

public class BlockUniversalCable extends BlockSmallTransmitter implements ITieredBlock<CableTier>, IBlockOreDict, IHasTileEntity<TileEntityUniversalCable> {

    private final CableTier tier;

    public BlockUniversalCable(CableTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_universal_cable");
        this.tier = tier;
    }

    @Override
    public CableTier getTier() {
        return tier;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        switch (tier) {
            case BASIC:
                return new TileEntityBasicUniversalCable();
            case ADVANCED:
                return new TileEntityAdvancedUniversalCable();
            case ELITE:
                return new TileEntityEliteUniversalCable();
            case ULTIMATE:
                return new TileEntityUltimateUniversalCable();
        }
        return null;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }

    @Override
    public List<String> getOredictEntries() {
        List<String> entries = new ArrayList<>();
        if (tier == CableTier.BASIC) {
            entries.add("universalCable");
        }
        return entries;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityUniversalCable> getTileClass() {
        switch (tier) {
            case BASIC:
                return TileEntityBasicUniversalCable.class;
            case ADVANCED:
                return TileEntityAdvancedUniversalCable.class;
            case ELITE:
                return TileEntityEliteUniversalCable.class;
            case ULTIMATE:
                return TileEntityUltimateUniversalCable.class;
        }
        return null;
    }
}