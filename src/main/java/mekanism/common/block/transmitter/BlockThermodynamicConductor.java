package mekanism.common.block.transmitter;

import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.tileentity.TileEntityType;

public class BlockThermodynamicConductor extends BlockSmallTransmitter implements ITypeBlock, IHasTileEntity<TileEntityThermodynamicConductor> {

    private final ConductorTier tier;

    public BlockThermodynamicConductor(ConductorTier tier) {
        this.tier = tier;
    }

    @Override
    public BlockType getType() {
        return AttributeTier.getPassthroughType(tier);
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