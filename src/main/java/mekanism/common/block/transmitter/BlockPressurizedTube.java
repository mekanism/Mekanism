package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPressurizedTube extends BlockSmallTransmitter {

    private final TubeTier tier;

    public BlockPressurizedTube(TubeTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_pressurized_tube");
        this.tier = tier;
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileEntityPressurizedTube();
    }
}