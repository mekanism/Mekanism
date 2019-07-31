package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPressurizedTube extends BlockSmallTransmitter {

    private final TubeTier tier;

    public BlockPressurizedTube(TubeTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_pressurized_tube");
        this.tier = tier;
    }

    public TubeTier getTier() {
        return tier;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityPressurizedTube();
    }
}