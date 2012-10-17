package obsidian.api;

import java.util.List;

import dan200.computer.api.IPeripheral;

import ic2.api.IWrenchable;
import net.minecraft.src.*;
import net.minecraftforge.common.ISidedInventory;

/**
 * A group of common methods used by all Obsidian Ingots machines.
 * @author AidanBrady
 *
 */
public interface IElectricMachine extends IInventory, ISidedInventory, IWrenchable, ITileNetwork, IPeripheral
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
	 * Gets the scaled charge level for the GUI.
	 * @param i - control
	 * @return scaled charge level
	 */
	public int getScaledChargeLevel(int i);
	
	/**
	 * Gets the scaled progress for the GUI.
	 * @param i - control
	 * @return scaled progress
	 */
	public int getScaledProgress(int i);
	
	/**
	 * Sets this machine's active state, sending a packet to the server if it differs from the previous update.
	 * @param active
	 */
	public void setActive(boolean active);
}
