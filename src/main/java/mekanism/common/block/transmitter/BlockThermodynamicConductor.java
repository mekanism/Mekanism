package mekanism.common.block.transmitter;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.transmitter.thermodynamic_conductor.TileEntityAdvancedThermodynamicConductor;
import mekanism.common.tile.transmitter.thermodynamic_conductor.TileEntityBasicThermodynamicConductor;
import mekanism.common.tile.transmitter.thermodynamic_conductor.TileEntityEliteThermodynamicConductor;
import mekanism.common.tile.transmitter.thermodynamic_conductor.TileEntityThermodynamicConductor;
import mekanism.common.tile.transmitter.thermodynamic_conductor.TileEntityUltimateThermodynamicConductor;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        switch (tier) {
            case BASIC:
                return new TileEntityBasicThermodynamicConductor();
            case ADVANCED:
                return new TileEntityAdvancedThermodynamicConductor();
            case ELITE:
                return new TileEntityEliteThermodynamicConductor();
            case ULTIMATE:
                return new TileEntityUltimateThermodynamicConductor();
        }
        return null;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityThermodynamicConductor> getTileClass() {
        switch (tier) {
            case BASIC:
                return TileEntityBasicThermodynamicConductor.class;
            case ADVANCED:
                return TileEntityAdvancedThermodynamicConductor.class;
            case ELITE:
                return TileEntityEliteThermodynamicConductor.class;
            case ULTIMATE:
                return TileEntityUltimateThermodynamicConductor.class;
        }
        return null;
    }
}