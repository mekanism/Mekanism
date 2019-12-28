package mekanism.common.block.transmitter;

import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.tileentity.TileEntityType;

public class BlockThermodynamicConductor extends BlockSmallTransmitter implements ITieredBlock<ConductorTier>, IHasTileEntity<TileEntityThermodynamicConductor> {

    private final ConductorTier tier;

    public BlockThermodynamicConductor(ConductorTier tier) {
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
                return MekanismTileEntityTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR.getTileEntityType();
            case ELITE:
                return MekanismTileEntityTypes.ELITE_THERMODYNAMIC_CONDUCTOR.getTileEntityType();
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR.getTileEntityType();
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_THERMODYNAMIC_CONDUCTOR.getTileEntityType();
        }
    }
}