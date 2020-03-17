package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.block.IBlockElectric;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.Block;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LaserEnergyContainer extends BasicEnergyContainer {

    public static LaserEnergyContainer create(Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert, TileEntityMekanism tile) {
        Block block = tile.getBlockType().getBlock();
        if (!(block instanceof IBlockElectric)) {
            throw new IllegalArgumentException("Block provider must be an electric block");
        }
        Objects.requireNonNull(tile, "Tile cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        IBlockElectric electricBlock = (IBlockElectric) block;
        return new LaserEnergyContainer(electricBlock.getStorage(), canExtract, canInsert, tile);
    }

    private LaserEnergyContainer(double maxEnergy, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          @Nullable IMekanismStrictEnergyHandler energyHandler) {
        super(maxEnergy, canExtract, canInsert, energyHandler);
    }
}