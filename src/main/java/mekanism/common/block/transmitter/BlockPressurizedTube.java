package mekanism.common.block.transmitter;

import java.util.Locale;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.tileentity.TileEntityType;

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
    public TileEntityType<TileEntityPressurizedTube> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE;
            case ELITE:
                return MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE;
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE;
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE;
        }
    }
}