package mekanism.common.base;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tile.IEnergyStorage;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.integration.MekanismHooks;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

@InterfaceList({
	@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = MekanismHooks.IC2_MOD_ID),
	@Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = MekanismHooks.IC2_MOD_ID),
	@Interface(iface = "ic2.api.energy.tile.IEnergyEmitter", modid = MekanismHooks.IC2_MOD_ID),
	@Interface(iface = "ic2.api.tile.IEnergyStorage", modid = MekanismHooks.IC2_MOD_ID)
})
public interface IEnergyWrapper extends IStrictEnergyStorage, IEnergyReceiver, IEnergyProvider, IEnergySink, IEnergySource, IEnergyStorage, IStrictEnergyAcceptor, IStrictEnergyOutputter, IInventory
{
	public boolean sideIsOutput(EnumFacing side);

	public boolean sideIsConsumer(EnumFacing side);

	public double getMaxOutput();
}
