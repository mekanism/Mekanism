package mekanism.common.integration.computercraft;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.integration.computercraft.peripherals.*;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class MekanismPeripheralProvider implements IPeripheralProvider {
    @Override
    public @NotNull LazyOptional<IPeripheral> getPeripheral(@NotNull World world, @NotNull BlockPos blockPos, @NotNull Direction direction) {
        TileEntity t = world.getTileEntity(blockPos);

        if(t instanceof TileEntityMultiblock){
            TileEntityMultiblock<?> multiblock = (TileEntityMultiblock<?>) t;
            MultiblockData data = multiblock.getMultiblock();

            if(data instanceof MatrixMultiblockData){
                return LazyOptional.of(() -> new InductionMatrixPeripheral((MatrixMultiblockData) data));
            } else if(data instanceof EvaporationMultiblockData){
                return LazyOptional.of(() -> new ThermalEvaporationPeripheral((EvaporationMultiblockData) data));
            } else if(data instanceof TankMultiblockData){
                return LazyOptional.of(() -> new DynamicTankPeripheral((TankMultiblockData) data));
            } else if(data instanceof BoilerMultiblockData){
                return LazyOptional.of(() -> new BoilerPeripheral((BoilerMultiblockData) data));
            } else if(data instanceof SPSMultiblockData){
                return LazyOptional.of(() -> new SPSPeripheral((SPSMultiblockData) data));
            }
        }

        return LazyOptional.empty();
    }
}
