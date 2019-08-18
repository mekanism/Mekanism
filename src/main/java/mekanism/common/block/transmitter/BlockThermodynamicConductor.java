package mekanism.common.block.transmitter;

import java.util.Locale;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.tileentity.TileEntityType;

public class BlockThermodynamicConductor extends BlockSmallTransmitter implements ITieredBlock<ConductorTier>, IHasTileEntity<TileEntityThermodynamicConductor> {

    private final ConductorTier tier;

    public BlockThermodynamicConductor(ConductorTier tier) {
        super(tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_thermodynamic_conductor");
        this.tier = tier;
    }

    @Override
    public ConductorTier getTier() {
        return tier;
    }

    @Override
    public TileEntityType<TileEntityThermodynamicConductor> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR;
            case ELITE:
                return MekanismTileEntityTypes.ELITE_THERMODYNAMIC_CONDUCTOR;
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR;
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_THERMODYNAMIC_CONDUCTOR;
        }
    }
}