package mekanism.common.integration.lookingat.hwyla;

import mcp.mobius.waila.api.IServerDataProvider;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.LookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;

public class HwylaDataProvider implements IServerDataProvider<BlockEntity> {

    static final HwylaDataProvider INSTANCE = new HwylaDataProvider();

    @Override
    public void appendServerData(CompoundTag data, ServerPlayer player, Level world, BlockEntity tile, boolean showDetails) {
        if (tile instanceof TileEntityBoundingBlock boundingBlock) {
            //If we are a bounding block that has a position set, redirect the check to the main location
            if (!boundingBlock.hasReceivedCoords() || tile.getBlockPos().equals(boundingBlock.getMainPos())) {
                //If the coords haven't been received, exit
                return;
            }
            tile = WorldUtils.getTileEntity(world, boundingBlock.getMainPos());
            if (tile == null) {
                //If there is no tile where the bounding block thinks the main tile is, exit
                return;
            }
        }
        HwylaLookingAtHelper helper = new HwylaLookingAtHelper();
        LookingAtUtils.addInfo(helper, tile, true, true);
        //Add our data if we have any
        helper.finalizeData(data);
    }

    static class HwylaLookingAtHelper implements LookingAtHelper {

        private final ListTag data = new ListTag();

        @Override
        public void addText(Component text) {
            CompoundTag textData = new CompoundTag();
            textData.putString(MekanismHwylaPlugin.TEXT, Component.Serializer.toJson(text));
            data.add(textData);
        }

        @Override
        public void addEnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
            CompoundTag energyData = new CompoundTag();
            energyData.putString(NBTConstants.ENERGY_STORED, energy.toString());
            energyData.putString(NBTConstants.MAX, maxEnergy.toString());
            data.add(energyData);
        }

        @Override
        public void addFluidElement(FluidStack stored, int capacity) {
            CompoundTag fluidData = new CompoundTag();
            fluidData.put(NBTConstants.FLUID_STORED, stored.writeToNBT(new CompoundTag()));
            fluidData.putInt(NBTConstants.MAX, capacity);
            data.add(fluidData);
        }

        @Override
        public void addChemicalElement(ChemicalStack<?> stored, long capacity) {
            CompoundTag chemicalData = new CompoundTag();
            chemicalData.put(MekanismHwylaPlugin.CHEMICAL_STACK, stored.write(new CompoundTag()));
            chemicalData.putLong(NBTConstants.MAX, capacity);
            data.add(chemicalData);
        }

        void finalizeData(CompoundTag data) {
            if (!this.data.isEmpty()) {
                data.put(NBTConstants.MEK_DATA, this.data);
            }
        }
    }
}