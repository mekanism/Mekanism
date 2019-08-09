package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.pressurized_tube.TileEntityAdvancedPressurizedTube;
import mekanism.common.tile.transmitter.pressurized_tube.TileEntityBasicPressurizedTube;
import mekanism.common.tile.transmitter.pressurized_tube.TileEntityElitePressurizedTube;
import mekanism.common.tile.transmitter.pressurized_tube.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.pressurized_tube.TileEntityUltimatePressurizedTube;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPressurizedTube extends BlockSmallTransmitter implements ITieredBlock<TubeTier>, IHasTileEntity<TileEntityPressurizedTube> {

    private final TubeTier tier;

    public BlockPressurizedTube(TubeTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_pressurized_tube");
        this.tier = tier;
    }

    @Override
    public TubeTier getTier() {
        return tier;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        switch (tier) {
            case BASIC:
                return new TileEntityBasicPressurizedTube();
            case ADVANCED:
                return new TileEntityAdvancedPressurizedTube();
            case ELITE:
                return new TileEntityElitePressurizedTube();
            case ULTIMATE:
                return new TileEntityUltimatePressurizedTube();
        }
        return null;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityPressurizedTube> getTileClass() {
        switch (tier) {
            case BASIC:
                return TileEntityBasicPressurizedTube.class;
            case ADVANCED:
                return TileEntityAdvancedPressurizedTube.class;
            case ELITE:
                return TileEntityElitePressurizedTube.class;
            case ULTIMATE:
                return TileEntityUltimatePressurizedTube.class;
        }
        return null;
    }
}