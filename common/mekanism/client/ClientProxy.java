package mekanism.client;

import java.io.File;
import java.util.HashMap;

import mekanism.client.gui.GuiChemicalInfuser;
import mekanism.client.gui.GuiChemicalInjectionChamber;
import mekanism.client.gui.GuiChemicalOxidizer;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiConfiguration;
import mekanism.client.gui.GuiCredits;
import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiDictionary;
import mekanism.client.gui.GuiDigitalMiner;
import mekanism.client.gui.GuiDynamicTank;
import mekanism.client.gui.GuiElectricChest;
import mekanism.client.gui.GuiElectricPump;
import mekanism.client.gui.GuiElectrolyticSeparator;
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
import mekanism.client.gui.GuiSalinationController;
import mekanism.client.gui.GuiTeleporter;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.block.BasicRenderingHandler;
import mekanism.client.render.block.MachineRenderingHandler;
import mekanism.client.render.entity.RenderBalloon;
import mekanism.client.render.entity.RenderObsidianTNTPrimed;
import mekanism.client.render.entity.RenderRobit;
import mekanism.client.render.item.ItemRenderingHandler;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderChargepad;
import mekanism.client.render.tileentity.RenderChemicalInfuser;
import mekanism.client.render.tileentity.RenderChemicalOxidizer;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderElectricChest;
import mekanism.client.render.tileentity.RenderElectricPump;
import mekanism.client.render.tileentity.RenderElectrolyticSeparator;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderGasTank;
import mekanism.client.render.tileentity.RenderLogisticalSorter;
import mekanism.client.render.tileentity.RenderMetallurgicInfuser;
import mekanism.client.render.tileentity.RenderObsidianTNT;
import mekanism.client.render.tileentity.RenderRotaryCondensentrator;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.sound.Sound;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonProxy;
import mekanism.common.IElectricChest;
import mekanism.common.IInvConfiguration;
import mekanism.common.Mekanism;
import mekanism.common.EnergyDisplay.EnergyType;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.InventoryElectricChest;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.TileEntityAdvancedFactory;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.tile.TileEntityElectricMachine;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEliteFactory;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityObsidianTNT;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySalinationController;
import mekanism.common.tile.TileEntitySalinationValve;
import mekanism.common.tile.TileEntityTeleporter;
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
		MekanismClient.baseSoundVolume = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "SoundVolume", 1D).getDouble(1D);
		
		String s = Mekanism.configuration.get(Configuration.CATEGORY_GENERAL, "EnergyType", "J").getString();
		
		if(s != null)
		{
			if(s.trim().equalsIgnoreCase("j") || s.trim().equalsIgnoreCase("joules"))
			{
				Mekanism.activeType = EnergyType.J;
			}
			else if(s.trim().equalsIgnoreCase("rf") || s.trim().equalsIgnoreCase("te") || s.trim().equalsIgnoreCase("thermal expansion"))
			{
				Mekanism.activeType = EnergyType.RF;
			}
			else if(s.trim().equalsIgnoreCase("eu") || s.trim().equalsIgnoreCase("ic2"))
			{
				Mekanism.activeType = EnergyType.EU;
			}
			else if(s.trim().equalsIgnoreCase("mj") || s.trim().equalsIgnoreCase("bc") || s.trim().equalsIgnoreCase("buildcraft"))
			{
				Mekanism.activeType = EnergyType.MJ;
			}
		}
		
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
		ClientRegistry.registerTileEntity(TileEntityElectricPump.class, "ElectricPump", new RenderElectricPump());
		ClientRegistry.registerTileEntity(TileEntityElectricChest.class, "ElectricChest", new RenderElectricChest());
		ClientRegistry.registerTileEntity(TileEntityDynamicTank.class, "DynamicTank", new RenderDynamicTank());
		ClientRegistry.registerTileEntity(TileEntityDynamicValve.class, "DynamicValve", new RenderDynamicTank());
		ClientRegistry.registerTileEntity(TileEntityChargepad.class, "Chargepad", new RenderChargepad());
		ClientRegistry.registerTileEntity(TileEntityLogisticalSorter.class, "LogisticalSorter", new RenderLogisticalSorter());
		ClientRegistry.registerTileEntity(TileEntityBin.class, "Bin", new RenderBin());
		ClientRegistry.registerTileEntity(TileEntityDigitalMiner.class, "DigitalMiner", new RenderDigitalMiner());
		ClientRegistry.registerTileEntity(TileEntityRotaryCondensentrator.class, "RotaryCondensentrator", new RenderRotaryCondensentrator());
		ClientRegistry.registerTileEntity(TileEntityTeleporter.class, "MekanismTeleporter", new RenderTeleporter());
		ClientRegistry.registerTileEntity(TileEntityChemicalOxidizer.class, "ChemicalOxidizer", new RenderChemicalOxidizer());
		ClientRegistry.registerTileEntity(TileEntityChemicalInfuser.class, "ChemicalInfuser", new RenderChemicalInfuser());
		ClientRegistry.registerTileEntity(TileEntityChemicalInjectionChamber.class, "ChemicalInjectionChamber", new RenderConfigurableMachine());
		ClientRegistry.registerTileEntity(TileEntityElectrolyticSeparator.class, "ElectrolyticSeparator", new RenderElectrolyticSeparator());
		GameRegistry.registerTileEntity(TileEntitySalinationController.class, "SalinationController");
		GameRegistry.registerTileEntity(TileEntitySalinationValve.class, "SalinationValve");
	}
	
	@Override
	public void registerRenderInformation()
	{
	    RenderPartTransmitter.init();
	    
		//Register entity rendering handlers
		RenderingRegistry.registerEntityRenderingHandler(EntityObsidianTNT.class, new RenderObsidianTNTPrimed());
		RenderingRegistry.registerEntityRenderingHandler(EntityRobit.class, new RenderRobit());
		RenderingRegistry.registerEntityRenderingHandler(EntityBalloon.class, new RenderBalloon());
		
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
        MinecraftForgeClient.registerItemRenderer(Mekanism.PartTransmitter.itemID, handler);
        MinecraftForgeClient.registerItemRenderer(Mekanism.GasMask.itemID, handler);
        MinecraftForgeClient.registerItemRenderer(Mekanism.ScubaTank.itemID, handler);
        MinecraftForgeClient.registerItemRenderer(Mekanism.Balloon.itemID, handler);
		
		//Register block handlers
		RenderingRegistry.registerBlockHandler(new MachineRenderingHandler());
		RenderingRegistry.registerBlockHandler(new BasicRenderingHandler());
		
		System.out.println("[Mekanism] Render registrations complete.");
	}
	
	@Override
	public GuiScreen getClientGui(int ID, EntityPlayer player, World world, int x, int y, int z) 
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
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
				return new GuiChemicalOxidizer(player.inventory, (TileEntityChemicalOxidizer)tileEntity);
			case 30:
				return new GuiChemicalInfuser(player.inventory, (TileEntityChemicalInfuser)tileEntity);
			case 31:
				return new GuiChemicalInjectionChamber(player.inventory, (TileEntityAdvancedElectricMachine)tileEntity);
			case 32:
				return new GuiElectrolyticSeparator(player.inventory, (TileEntityElectrolyticSeparator)tileEntity);
			case 33:
				return new GuiSalinationController(player.inventory, (TileEntitySalinationController)tileEntity);

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
	
	@Override
	public void onConfigSync()
	{
		super.onConfigSync();

		if(Mekanism.voiceServerEnabled)
		{
			MekanismClient.voiceClient.start();
		}
	}
}
