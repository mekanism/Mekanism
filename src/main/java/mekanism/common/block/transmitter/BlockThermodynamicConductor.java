package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockThermodynamicConductor extends BlockSmallTransmitter {

    private final ConductorTier tier;

    public BlockThermodynamicConductor(ConductorTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_thermodynamic_conductor");
        this.tier = tier;
    }

    public ConductorTier getTier() {
        return tier;
    }

    @Override
    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new TileEntityThermodynamicConductor();
    }
}