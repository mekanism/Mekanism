package obsidian.api;

import java.util.List;

import buildcraft.api.power.IPowerReceptor;

import universalelectricity.implement.IElectricityReceiver;
import universalelectricity.implement.IJouleStorage;

import dan200.computer.api.IPeripheral;

import ic2.api.IEnergySink;
import ic2.api.IWrenchable;
import net.minecraft.src.*;
import net.minecraftforge.common.ISidedInventory;

/**
 * A group of common methods used by all Obsidian Ingots machines.
 * @author AidanBrady
 *
 */
public interface IElectricMachine extends IInventory, ISidedInventory, IWrenchable, ITileNetwork, IPowerReceptor, IEnergySink, IJouleStorage, IElectricityReceiver, IEnergyAcceptor, IPeripheral
{
    /**
     * Update call for machines. Use instead of updateEntity() - it's called every tick.
     */
	public void onUpdate();
	
    /**
     * Whether or not this machine can operate.
     * @return can operate
     */
	public boolean canOperate();
	
	/**
	 * Runs this machine's operation -- or smelts the item.
	 */
	public void operate();
	
	/**
	 * Sends a tile entity packet to the server.
	 */
	public void sendPacket();
	
	/**
	 * Sends a tile entity packet to the server with a defined range.
	 */
	public void sendPacketWithRange();

	/**
	 * Gets the recipe vector from the machine tile entity.
	 * @return recipes
	 */
	public List getRecipes();
	
	/**
	 * Sets this machine's active state, sending a packet to the server if it differs from the previous update.
	 * @param active
	 */
	public void setActive(boolean active);
}
