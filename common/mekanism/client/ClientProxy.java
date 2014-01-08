package mekanism.client;


import java.io.File;
import java.util.HashMap;

import mekanism.client.gui.GuiChemicalFormulator;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiConfiguration;
import mekanism.client.gui.GuiCredits;
import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiDictionary;
import mekanism.client.gui.GuiDigitalMiner;
import mekanism.client.gui.GuiDynamicTank;
import mekanism.client.gui.GuiElectricChest;
import mekanism.client.gui.GuiElectricPump;
import mekanism.client.gui.GuiEnergizedSmelter;
import mekanism.client.gui.GuiEnergyCube;
import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.client.gui.GuiFactory;
import mekanism.client.gui.GuiGasTank;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.client.gui.GuiPasswordEnter;
import mekanism.client.gui.GuiPasswordModify;
import mekanism.client.gui.GuiPortableTeleporter;
import mekanism.client.gui.GuiPurificationChamber;
import mekanism.client.gui.GuiRobitCrafting;
import mekanism.client.gui.GuiRobitInventory;
import mekanism.client.gui.GuiRobitMain;
import mekanism.client.gui.GuiRobitRepair;
import mekanism.client.gui.GuiRobitSmelting;
import mekanism.client.gui.GuiRotaryCondensentrator;
import mekanism.client.gui.GuiTeleporter;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.block.BasicRenderingHandler;
import mekanism.client.render.block.MachineRenderingHandler;
import mekanism.client.render.block.TransmitterRenderingHandler;
import mekanism.client.render.entity.RenderObsidianTNTPrimed;
import mekanism.client.render.entity.RenderRobit;
import mekanism.client.render.item.ItemRenderingHandler;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderChargepad;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderElectricChest;
import mekanism.client.render.tileentity.RenderElectricPump;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderGasTank;
import mekanism.client.render.tileentity.RenderLogisticalSorter;
import mekanism.client.render.tileentity.RenderLogisticalTransporter;
import mekanism.client.render.tileentity.RenderMechanicalPipe;
import mekanism.client.render.tileentity.RenderMetallurgicInfuser;
import mekanism.client.render.tileentity.RenderObsidianTNT;
import mekanism.client.render.tileentity.RenderPressurizedTube;
import mekanism.client.render.tileentity.RenderRotaryCondensentrator;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.render.tileentity.RenderUniversalCable;
import mekanism.client.sound.Sound;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonProxy;
import mekanism.common.EntityObsidianTNT;
import mekanism.common.EntityRobit;
import mekanism.common.IElectricChest;
import mekanism.common.IInvConfiguration;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.InventoryElectricChest;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.tileentity.TileEntityAdvancedElectricMachine;
import mekanism.common.tileentity.TileEntityAdvancedFactory;
import mekanism.common.tileentity.TileEntityBin;
import mekanism.common.tileentity.TileEntityChargepad;
import mekanism.common.tileentity.TileEntityChemicalFormulator;
import mekanism.common.tileentity.TileEntityCombiner;
import mekanism.common.tileentity.TileEntityCrusher;
import mekanism.common.tileentity.TileEntityDigitalMiner;
import mekanism.common.tileentity.TileEntityDiversionTransporter;
import mekanism.common.tileentity.TileEntityDynamicTank;
import mekanism.common.tileentity.TileEntityDynamicValve;
import mekanism.common.tileentity.TileEntityElectricChest;
import mekanism.common.tileentity.TileEntityElectricMachine;
import mekanism.common.tileentity.TileEntityElectricPump;
import mekanism.common.tileentity.TileEntityEliteFactory;
import mekanism.common.tileentity.TileEntityEnergizedSmelter;
import mekanism.common.tileentity.TileEntityEnergyCube;
import mekanism.common.tileentity.TileEntityEnrichmentChamber;
import mekanism.common.tileentity.TileEntityFactory;
import mekanism.common.tileentity.TileEntityGasTank;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import mekanism.common.tileentity.TileEntityMechanicalPipe;
import mekanism.common.tileentity.TileEntityMetallurgicInfuser;
import mekanism.common.tileentity.TileEntityObsidianTNT;
import mekanism.common.tileentity.TileEntityOsmiumCompressor;
import mekanism.common.tileentity.TileEntityPressurizedTube;
import mekanism.common.tileentity.TileEntityPurificationChamber;
import mekanism.common.tileentity.TileEntityRotaryCondensentrator;
import mekanism.common.tileentity.TileEntityTeleporter;
import mekanism.common.tileentity.TileEntityUniversalCable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client proxy for the Mekanism mod.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	public static int MACHINE_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	public static int TRANSMITTER_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	public static int BASIC_RENDER_ID = RenderingRegistry.getNextAvailableRenderId();
	
	@Override
	public void loadConfiguration()
	{
		super.loadConfiguration();
		
		Mekanism.configuration.load();
		MekanismClient.enableSounds = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EnableSounds", true).getBoolean(true);
		MekanismClient.fancyUniversalCableRender = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "FancyUniversalCableRender", true).getBoolean(true);
		MekanismClient.holidays = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "Holidays", true).getBoolean(true);
		MekanismClient.baseSoundVolume = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "SoundVolume", 1).getDouble(1);
		Mekanism.configuration.save();
	}
	
	@Override
	public int getArmorIndex(String string)
	{
		return RenderingRegistry.addNewArmourRendererPrefix(string);
	}
	
	@Override
	public void registerSound(Object obj) 
	{
		if(MekanismClient.enableSounds && Minecraft.getMinecraft().sndManager.sndSystem != null)
		{
			synchronized(MekanismClient.audioHandler.sounds)
			{
				MekanismClient.audioHandler.register(obj);
			}
		}
	}
	
	@Override
	public void unregisterSound(TileEntity tileEntity) 
	{
		if(MekanismClient.enableSounds && Minecraft.getMinecraft().sndManager.sndSystem != null)
		{
			synchronized(MekanismClient.audioHandler.sounds)
			{
				if(MekanismClient.audioHandler.getFrom(tileEntity) != null)
				{
					MekanismClient.audioHandler.getFrom(tileEntity).remove();
				}
			}
		}
	}
	
	@Override
	public void openElectricChest(EntityPlayer entityplayer, int id, int windowId, boolean isBlock, int x, int y, int z) 
	{
		TileEntityElectricChest tileEntity = (TileEntityElectricChest)entityplayer.worldObj.getBlockTileEntity(x, y, z);
		
		if(id == 0)
		{
			if(isBlock)
			{
	    		FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiElectricChest(entityplayer.inventory, tileEntity));
	    		entityplayer.openContainer.windowId = windowId;
			}
			else {
				FMLClientHandler.instance().getClient().sndManager.playSoundFX("random.chestopen", 1.0F, 1.0F);
				ItemStack stack = entityplayer.getCurrentEquippedItem();
				
				if(stack != null && stack.getItem() instanceof IElectricChest && MachineType.get(stack) == MachineType.ELECTRIC_CHEST)
				{
    				InventoryElectricChest inventory = new InventoryElectricChest(entityplayer);
		    		FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiElectricChest(entityplayer.inventory, inventory));
		    		entityplayer.openContainer.windowId = windowId;
				}
			}
		}
		else if(id == 1)
		{
			if(isBlock)
			{
				FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiPasswordEnter(tileEntity));
			}
			else {
				ItemStack stack = entityplayer.getCurrentEquippedItem();
				
				if(stack != null && stack.getItem() instanceof IElectricChest && MachineType.get(stack) == MachineType.ELECTRIC_CHEST)
				{
					FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiPasswordEnter(stack));
				}
			}
		}
		else if(id == 2)
		{
			if(isBlock)
			{
				FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiPasswordModify(tileEntity));
			}
			else {
				ItemStack stack = entityplayer.getCurrentEquippedItem();
				
				if(stack != null && stack.getItem() instanceof IElectricChest && MachineType.get(stack) == MachineType.ELECTRIC_CHEST)
				{
					FMLClientHandler.instance().displayGuiScreen(entityplayer, new GuiPasswordModify(stack));
				}
			}
		}
	}
	
	@Override
	public void registerSpecialTileEntities() 
	{
		ClientRegistry.registerTileEntity(TileEntityEnrichmentChamber.class, "EnrichmentChamber", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityOsmiumCompressor.class, "OsmiumCompressor", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityCombiner.class, "Combiner", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityCrusher.class, "Crusher", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityFactory.class, "SmeltingFactory", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityAdvancedFactory.class, "AdvancedSmeltingFactory", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityEliteFactory.class, "UltimateSmeltingFactory", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityPurificationChamber.class, "PurificationChamber", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityEnergizedSmelter.class, "EnergizedSmelter", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityMetallurgicInfuser.class, "MetallurgicInfuser", new RenderMetallurgicInfuser());
		ClientRegistry.registerTileEntity(TileEntityObsidianTNT.class, "ObsidianTNT", new RenderObsidianTNT());
		ClientRegistry.registerTileEntity(TileEntityGasTank.class, "GasTank", new RenderGasTank());
		ClientRegistry.registerTileEntity(TileEntityEnergyCube.class, "EnergyCube", new RenderEnergyCube());
		ClientRegistry.registerTileEntity(TileEntityPressurizedTube.class, "PressurizedTube", new RenderPressurizedTube());
		ClientRegistry.registerTileEntity(TileEntityUniversalCable.class, "UniversalCable", new RenderUniversalCable());
		ClientRegistry.registerTileEntity(TileEntityElectricPump.class, "ElectricPump", new RenderElectricPump());
		ClientRegistry.registerTileEntity(TileEntityElectricChest.class, "ElectricChest", new RenderElectricChest());
		ClientRegistry.registerTileEntity(TileEntityMechanicalPipe.class, "MechanicalPipe", new RenderMechanicalPipe());
		ClientRegistry.registerTileEntity(TileEntityDynamicTank.class, "DynamicTank", new RenderDynamicTank());
		ClientRegistry.registerTileEntity(TileEntityDynamicValve.class, "DynamicValve", new RenderDynamicTank());
		ClientRegistry.registerTileEntity(TileEntityChargepad.class, "Chargepad", new RenderChargepad());
		ClientRegistry.registerTileEntity(TileEntityLogisticalTransporter.class, "LogisticalTransporter", new RenderLogisticalTransporter());
		ClientRegistry.registerTileEntity(TileEntityDiversionTransporter.class, "DiversionTransporter", new RenderLogisticalTransporter());
		ClientRegistry.registerTileEntity(TileEntityLogisticalSorter.class, "LogisticalSorter", new RenderLogisticalSorter());
		ClientRegistry.registerTileEntity(TileEntityBin.class, "Bin", new RenderBin());
		ClientRegistry.registerTileEntity(TileEntityDigitalMiner.class, "DigitalMiner", new RenderDigitalMiner());
		ClientRegistry.registerTileEntity(TileEntityRotaryCondensentrator.class, "RotaryCondensentrator", new RenderRotaryCondensentrator());
		ClientRegistry.registerTileEntity(TileEntityTeleporter.class, "MekanismTeleporter", new RenderTeleporter());
	}
	
	@Override
	public void registerRenderInformation()
	{
		//Register entity rendering handlers
		RenderingRegistry.registerEntityRenderingHandler(EntityObsidianTNT.class, new RenderObsidianTNTPrimed());
		RenderingRegistry.registerEntityRenderingHandler(EntityRobit.class, new RenderRobit());
		
		//Register item handler
		ItemRenderingHandler handler = new ItemRenderingHandler();
		
		MinecraftForgeClient.registerItemRenderer(Mekanism.energyCubeID, handler);
		MinecraftForgeClient.registerItemRenderer(Mekanism.machineBlockID, handler);
		MinecraftForgeClient.registerItemRenderer(Mekanism.machineBlock2ID, handler);
		MinecraftForgeClient.registerItemRenderer(Mekanism.Robit.itemID, handler);
		MinecraftForgeClient.registerItemRenderer(Mekanism.WalkieTalkie.itemID, handler);
		MinecraftForgeClient.registerItemRenderer(Mekanism.gasTankID, handler);
		MinecraftForgeClient.registerItemRenderer(Mekanism.obsidianTNTID, handler);
		MinecraftForgeClient.registerItemRenderer(Mekanism.basicBlockID, handler);
		MinecraftForgeClient.registerItemRenderer(Mekanism.Jetpack.itemID, handler);
		
		//Register block handlers
		RenderingRegistry.registerBlockHandler(new MachineRenderingHandler());
		RenderingRegistry.registerBlockHandler(new TransmitterRenderingHandler());
		RenderingRegistry.registerBlockHandler(new BasicRenderingHandler());
		
		System.out.println("[Mekanism] Render registrations complete.");
	}
	
	@Override
	public GuiScreen getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		if (tileEntity == null)
			return null;
			
		switch(ID)
		{
			case 0:
				return new GuiDictionary(player.inventory);
			case 1:
				return new GuiCredits();
			case 2:
				return new GuiDigitalMiner(player.inventory, (TileEntityDigitalMiner)tileEntity);
			case 3:
				return new GuiEnrichmentChamber(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 4:
				return new GuiOsmiumCompressor(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 5:
				return new GuiCombiner(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 6:
				return new GuiCrusher(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 7:
				return new GuiRotaryCondensentrator(player.inventory, (TileEntityRotaryCondensentrator)tileEntity);
			case 8:
				return new GuiEnergyCube(player.inventory, (TileEntityEnergyCube)tileEntity);
			case 9:
				return new GuiConfiguration(player, (IInvConfiguration)tileEntity);
			case 10:
				return new GuiGasTank(player.inventory, (TileEntityGasTank)tileEntity);
			case 11:
				return new GuiFactory(player.inventory, (TileEntityFactory)tileEntity);
			case 12:
				return new GuiMetallurgicInfuser(player.inventory, (TileEntityMetallurgicInfuser)tileEntity);
			case 13:
				return new GuiTeleporter(player.inventory, (TileEntityTeleporter)tileEntity);
			case 14:
				ItemStack itemStack = player.getCurrentEquippedItem();
				
				if(itemStack != null && itemStack.getItem() instanceof ItemPortableTeleporter)
				{
					return new GuiPortableTeleporter(player, itemStack);
				}
			case 15:
				return new GuiPurificationChamber(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 16:
				return new GuiEnergizedSmelter(player.inventory, (TileEntityElectricMachine)tileEntity);
			case 17:
				return new GuiElectricPump(player.inventory, (TileEntityElectricPump)tileEntity);
			case 18:
				return new GuiDynamicTank(player.inventory, (TileEntityDynamicTank)tileEntity);
			case 19:
				return new GuiPasswordEnter((TileEntityElectricChest)tileEntity);
			case 20:
				return new GuiPasswordModify((TileEntityElectricChest)tileEntity);
			case 21:
				EntityRobit robit = (EntityRobit)world.getEntityByID(x);
				
				if(robit != null)
				{
					return new GuiRobitMain(player.inventory, robit);
				}
			case 22:
				return new GuiRobitCrafting(player.inventory, world, x);
			case 23:
				EntityRobit robit1 = (EntityRobit)world.getEntityByID(x);
				
				if(robit1 != null)
				{
					return new GuiRobitInventory(player.inventory, robit1);
				}
			case 24:
				EntityRobit robit2 = (EntityRobit)world.getEntityByID(x);
				
				if(robit2 != null)
				{
					return new GuiRobitSmelting(player.inventory, robit2);
				}
			case 25:
				return new GuiRobitRepair(player.inventory, world, x);
			case 29:
				return new GuiChemicalFormulator(player.inventory, (TileEntityChemicalFormulator)tileEntity);
		}
		
		return null;
	}
	
	@Override
	public void doTankAnimation(TileEntityDynamicTank tileEntity)
	{
		new ThreadTankSparkle(tileEntity).start();
	}
	
	@Override
	public void loadUtilities()
	{
		super.loadUtilities();
		
		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
		TickRegistry.registerTickHandler(new RenderTickHandler(), Side.CLIENT);
		
		NetworkRegistry.instance().registerConnectionHandler(new ClientConnectionHandler());
		
		KeyBindingRegistry.registerKeyBinding(new MekanismKeyHandler());
		
		GameRegistry.registerPlayerTracker(new ClientPlayerTracker());
		
		HolidayManager.init();
	}
	
	@Override
	public void loadSoundHandler()
	{
		if(MekanismClient.enableSounds)
		{
			MekanismClient.audioHandler = new SoundHandler();
		}
	}
	
	@Override
	public void unloadSoundHandler()
	{
		if(MekanismClient.enableSounds)
		{
			if(MekanismClient.audioHandler != null)
			{
				synchronized(MekanismClient.audioHandler.sounds)
				{
					HashMap<Object, Sound> sounds = new HashMap<Object, Sound>();
					sounds.putAll(MekanismClient.audioHandler.sounds);
					
					for(Sound sound : sounds.values())
					{
						sound.remove();
					}
					
					MekanismClient.audioHandler.sounds.clear();
				}
			}
		}
	}
	
	@Override
	public void preInit()
	{
		MekanismRenderer.init();
	}
	
	@Override
	public double getReach(EntityPlayer player)
	{
		return Minecraft.getMinecraft().playerController.getBlockReachDistance();
	}
	
	@Override
	public boolean isPaused()
	{
		if(FMLClientHandler.instance().getClient().isSingleplayer() && !FMLClientHandler.instance().getClient().getIntegratedServer().getPublic())
		{
			GuiScreen screen = FMLClientHandler.instance().getClient().currentScreen;
			
			if(screen != null && screen.doesGuiPauseGame())
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public File getMinecraftDir()
	{
		return Minecraft.getMinecraft().mcDataDir;
	}
}
