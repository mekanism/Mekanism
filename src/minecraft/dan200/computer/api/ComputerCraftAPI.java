/**
 * This file is part of the public ComputerCraft API - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2013. This API may be redistributed unmodified and in full only.
 * For help using the API, and posting your mods, visit the forums at computercraft.info.
 */

package dan200.computer.api;
import java.lang.reflect.Method;

/**
 * The static entry point to the ComputerCraft API.
 * Members in this class must be called after mod_ComputerCraft has been initialised,
 * but may be called before it is fully loaded.
 */
public class ComputerCraftAPI 
{
	/**
	 * Get the creative mode tab that ComputerCraft items can be found on.
	 * Use this to add your peripherals to ComputerCraft's tab.
	 */
	public static net.minecraft.creativetab.CreativeTabs getCreativeTab()
	{
		findCC();
		if (computerCraft_getCreativeTab != null)
		{
			try {
				return (net.minecraft.creativetab.CreativeTabs)( computerCraft_getCreativeTab.invoke(null) );
			} catch (Exception e){
				// It failed
			}
		}
		return null;	
	}
	
	/**
	 * Registers a peripheral handler for a TileEntity that you do not have access to. Only
	 * use this if you want to expose IPeripheral on a TileEntity from another mod. For your own
	 * mod, just implement IPeripheral on the TileEntity directly.
	 * @see IPeripheral
	 * @see IPeripheralHandler
	 */
	public static void registerExternalPeripheral( Class <? extends net.minecraft.tileentity.TileEntity> clazz, IPeripheralHandler handler )
	{
		findCC();
		if (computerCraft_registerExternalPeripheral != null)
		{
			try {
				computerCraft_registerExternalPeripheral.invoke(null, clazz, handler);
			} catch (Exception e){
				// It failed
			}
		}
	}

	// The functions below here are private, and are used to interface with the non-API ComputerCraft classes.
	// Reflection is used here so you can develop your mod in MCP without decompiling ComputerCraft and including
	// it in your solution.
	
	private static void findCC()
	{
		if( !ccSearched ) {
			try {
				computerCraft = Class.forName( "dan200.ComputerCraft" );
				computerCraft_getCreativeTab = findCCMethod( "getCreativeTab", new Class[] { } );
				computerCraft_registerExternalPeripheral = findCCMethod( "registerExternalPeripheral", new Class[] { 
					Class.class, IPeripheralHandler.class 
				} );
			} catch( Exception e ) {
				System.out.println("ComputerCraftAPI: ComputerCraft not found.");
			} finally {
				ccSearched = true;
			}
		}
	}

	private static Method findCCMethod( String name, Class[] args )
	{
		try {
			return computerCraft.getMethod( name, args );
			
		} catch( NoSuchMethodException e ) {
			System.out.println("ComputerCraftAPI: ComputerCraft method " + name + " not found.");
			return null;
		}
	}	
	
	private static boolean ccSearched = false;	
	private static Class computerCraft = null;
	private static Method computerCraft_registerExternalPeripheral = null;
	private static Method computerCraft_getCreativeTab = null;
}
