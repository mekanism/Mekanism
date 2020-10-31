package mekanism.common.integration.lookingat.hwyla;

import mcp.mobius.waila.api.IServerDataProvider;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.LookingAtHelper;
import mekanism.common.integration.lookingat.LookingAtUtils;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.util.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class HwylaDataProvider implements IServerDataProvider<TileEntity> {

    static final HwylaDataProvider INSTANCE = new HwylaDataProvider();

    @Override
    public void appendServerData(CompoundNBT data, ServerPlayerEntity player, World world, TileEntity tile) {
        if (tile instanceof TileEntityBoundingBlock) {
            //If we are a bounding block that has a position set, redirect the check to the main location
            TileEntityBoundingBlock boundingBlock = (TileEntityBoundingBlock) tile;
            if (!boundingBlock.receivedCoords || tile.getPos().equals(boundingBlock.getMainPos())) {
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

    private static class HwylaLookingAtHelper implements LookingAtHelper {

        private final ListNBT data = new ListNBT();

        @Override
        public void addText(ITextComponent text) {
            CompoundNBT textData = new CompoundNBT();
            textData.putString(MekanismHwylaPlugin.TEXT, ITextComponent.Serializer.toJson(text));
            data.add(textData);
        }

        @Override
        public void addEnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
            CompoundNBT energyData = new CompoundNBT();
            energyData.putString(NBTConstants.ENERGY_STORED, energy.toString());
            energyData.putString(NBTConstants.MAX, maxEnergy.toString());
            data.add(energyData);
        }

        @Override
        public void addFluidElement(FluidStack stored, int capacity) {
            CompoundNBT fluidData = new CompoundNBT();
            fluidData.put(NBTConstants.FLUID_STORED, stored.writeToNBT(new CompoundNBT()));
            fluidData.putInt(NBTConstants.MAX, capacity);
            data.add(fluidData);
        }

        @Override
        public void addChemicalElement(ChemicalStack<?> stored, long capacity) {
            CompoundNBT chemicalData = new CompoundNBT();
            chemicalData.put(MekanismHwylaPlugin.CHEMICAL_STACK, stored.write(new CompoundNBT()));
            chemicalData.putLong(NBTConstants.MAX, capacity);
            data.add(chemicalData);
        }

        private void finalizeData(CompoundNBT data) {
            if (!this.data.isEmpty()) {
                data.put(NBTConstants.MEK_DATA, this.data);
            }
        }
    }
}