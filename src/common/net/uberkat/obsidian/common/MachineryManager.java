package net.uberkat.obsidian.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.*;

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
		System.out.println("[ObsidianIngots] Successfully initialized Machinery Manager.");
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
			System.out.println("[ObsidianIngots] Attempted to add machine to manager that already exists.");
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
			System.out.println("[ObsidianIngots] Attempted to remove machine from manager that doesn't exist.");
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
			System.out.println("[ObsidianIngots] Attempted to grab machine from manager that doesn't exist.");
			return null;
		}
	}
	
	/**
	 * Resets the manager -- removing all machines from the ArrayList
	 */
	public void reset()
	{
		machines.clear();
	}
}
