package mekanism.common.base;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.tile.IEnergyStorage;

import java.util.EnumSet;

import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.api.energy.IStrictEnergyStorage;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;

@InterfaceList({
	@Interface(iface = "cofh.redstoneflux.api.IEnergyProvider", modid = "redstoneflux"),
	@Interface(iface = "cofh.redstoneflux.api.IEnergyReceiver", modid = "redstoneflux"),
	@Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2"),
	@Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "IC2"),
	@Interface(iface = "ic2.api.energy.tile.IEnergyEmitter", modid = "IC2"),
	@Interface(iface = "ic2.api.tile.IEnergyStorage", modid = "IC2")
})
public interface IEnergyWrapper extends IStrictEnergyStorage, IEnergyReceiver, IEnergyProvider, IEnergySink, IEnergySource, IEnergyStorage, IStrictEnergyAcceptor, IStrictEnergyOutputter, IInventory
{
	public EnumSet<EnumFacing> getOutputtingSides();

	public EnumSet<EnumFacing> getConsumingSides();

	public double getMaxOutput();
}
