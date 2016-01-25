package mcp.mobius.waila.api;

/**
 * Main registration interface. An instance will be provided to a method specified in an IMC msg formatted as follow<br>
 * FMLInterModComms.sendMessage("Waila", "register", "fully.qualified.path.to.registration.method");<br>
 * The registration method need to follow this signature<br>
 * public static void callbackRegister({@link IWailaRegistrar} registrar)<p>
 * If not specified otherwise, all the registration methods taking a class can take classes as well as interfaces.
 * Waila will do a lookup using instanceof on the registered classes, meaning that if all your targets inherit one interface, you only need
 * to specify it to cover the whole hierarchy.<br>
 * For the registration of blocks, both Blocks and TileEntities are accepted.<p>
 * For the configuration keys :<br>
 * modname refers to a String used for display in Waila's config panel.<br>
 * keyname refers to an unique key used internally for config query (cf {@link IWailaConfigHandler}). Those keys are shared across Waila, keep them unique !<br>
 * 
 * 
 * @author ProfMobius
 *
 */
public interface IWailaRegistrar {
	/* Add a config option in the section modname with displayed text configtext and access key keyname */
	public void addConfig(String modname, String keyname, String configtext);
	public void addConfig(String modname, String keyname, String configtext, boolean defvalue);
	public void addConfigRemote(String modname, String keyname, String configtext);	
	public void addConfigRemote(String modname, String keyname, String configtext, boolean defvalue);
	public void addConfig(String modname, String keyname);
	public void addConfig(String modname, String keyname, boolean defvalue);	
	public void addConfigRemote(String modname, String keyname);	
	public void addConfigRemote(String modname, String keyname, boolean defvalue);
	
	/* Register a stack overrider for the given blockID */
	public void registerStackProvider(IWailaDataProvider dataProvider, Class block);	
	
	/* Same thing, but works on a class hierarchy instead */
	public void registerHeadProvider (IWailaDataProvider dataProvider, Class block);
	public void registerBodyProvider (IWailaDataProvider dataProvider, Class block);
	public void registerTailProvider (IWailaDataProvider dataProvider, Class block);	

	/* Registering an NBT Provider provides a way to override the default "writeToNBT" way of doing things. */
	public void registerNBTProvider(IWailaDataProvider dataProvider, Class block);	
	
	/* Entity text registration methods */
	public void registerHeadProvider     (IWailaEntityProvider dataProvider, Class entity);
	public void registerBodyProvider     (IWailaEntityProvider dataProvider, Class entity);
	public void registerTailProvider     (IWailaEntityProvider dataProvider, Class entity);
	public void registerOverrideEntityProvider (IWailaEntityProvider dataProvider, Class entity);

	/* Registering an NBT Provider provides a way to override the default "writeToNBT" way of doing things. */
	public void registerNBTProvider(IWailaEntityProvider dataProvider, Class entity);	
	
	/* FMP Providers */
	public void registerHeadProvider(IWailaFMPProvider dataProvider, String name);
	public void registerBodyProvider(IWailaFMPProvider dataProvider, String name);
	public void registerTailProvider(IWailaFMPProvider dataProvider, String name);
	
	/* The block decorators */
	public void registerDecorator (IWailaBlockDecorator decorator, Class block);
	public void registerDecorator (IWailaFMPDecorator decorator,   String name);
	
	public void registerTooltipRenderer(String name, IWailaTooltipRenderer renderer);
	
	/* UNUSED FOR NOW (Will be used for the ingame wiki */
	//public void registerDocTextFile  (String filename);
	//public void registerShortDataProvider (IWailaSummaryProvider dataProvider, Class item);
}
