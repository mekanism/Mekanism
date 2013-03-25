package mekanism.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;

/**
 * A simple way of managing all machines -- MachineryManager! Contains an ArrayList of
 * basic machines that all machines are added to on placement.
 * @author AidanBrady
 *
 */
public class MachineryManager 
{
	/** The list of machines used */
	public List<TileEntityBasicMachine> machines = new ArrayList<TileEntityBasicMachine>();
	
	/**
	 * MachineryManager -- the easiest way of managing machines.
	 */
	public MachineryManager()
	{
		reset();
		System.out.println("[Mekanism] Successfully initialized Machinery Manager.");
	}
	
	/**
	 * Register a machine with the manager.
	 * @param machine - to be added
	 */
	public void register(TileEntityBasicMachine machine)
	{
		if(!machines.contains(machine))
		{
			machines.add(machine);
		}
		else {
			System.out.println("[Mekanism] Attempted to add machine to manager that already exists.");
		}
	}
	
	/**
	 * Remove a machine from the manager.
	 * @param machine - to be removed
	 */
	public void remove(TileEntityBasicMachine machine)
	{
		if(machines.contains(machine))
		{
			machines.remove(machine);
		}
		else {
			System.out.println("[Mekanism] Attempted to remove machine from manager that doesn't exist.");
		}
	}
	
	/**
	 * Grabs a machine from the manager.
	 * @param world - to be grabbed
	 * @param x - block coord
	 * @param y - block coord
	 * @param z - block coord
	 * @return machine grabbed from the manager
	 */
	public TileEntityBasicMachine getMachine(World world, int x, int y, int z)
	{
		if(machines.contains((TileEntityBasicMachine)world.getBlockTileEntity(x, y, z)))
		{
			return (TileEntityBasicMachine)world.getBlockTileEntity(x, y, z);
		}
		else {
			System.out.println("[Mekanism] Attempted to grab machine from manager that doesn't exist.");
			return null;
		}
	}
	
	/**
	 * Destroys all machines registered, as well as removing them from the manager's ArrayList.
	 * @param explode - whether or not to show fake explosion
	 */
	public void destroyAll(boolean explode)
	{
		for(TileEntityBasicMachine machine : machines)
		{
			if(explode)
			{
				MekanismUtils.doFakeBlockExplosion(machine.worldObj, machine.xCoord, machine.yCoord, machine.zCoord);
			}
			machine.worldObj.setBlockToAir(machine.xCoord, machine.yCoord, machine.zCoord);
			machine.worldObj.removeBlockTileEntity(machine.xCoord, machine.yCoord, machine.zCoord);
			remove(machine);
		}
	}
	
	public int size()
	{
		return machines.size();
	}
	
	/**
	 * Resets the manager -- removing all machines from the ArrayList
	 */
	public void reset()
	{
		machines.clear();
	}
}
