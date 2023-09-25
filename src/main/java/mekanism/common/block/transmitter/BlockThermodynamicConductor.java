package mekanism.common.block.transmitter;

import mekanism.api.tier.BaseTier;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;

public class BlockThermodynamicConductor extends BlockSmallTransmitter implements ITypeBlock, IHasTileEntity<TileEntityThermodynamicConductor> {

    private final ConductorTier tier;

    public BlockThermodynamicConductor(ConductorTier tier) {
        super(properties -> properties.mapColor(tier.getBaseTier().getMapColor()));
        this.tier = tier;
    }

    @Override
    protected BaseTier getBaseTier() {
        return this.tier.getBaseTier();
    }

    @Override
    public BlockType getType() {
        return AttributeTier.getPassthroughType(tier);
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityThermodynamicConductor> getTileType() {
        return switch (tier) {
            case ADVANCED -> MekanismTileEntityTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR;
            case ELITE -> MekanismTileEntityTypes.ELITE_THERMODYNAMIC_CONDUCTOR;
            case ULTIMATE -> MekanismTileEntityTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR;
            case BASIC -> MekanismTileEntityTypes.BASIC_THERMODYNAMIC_CONDUCTOR;
        };
    }
}