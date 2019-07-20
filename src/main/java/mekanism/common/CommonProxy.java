package mekanism.common;

import java.io.File;
import java.lang.ref.WeakReference;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.Pos3D;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.common.base.IGuiProvider;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.InventoryPersonalChest;
import mekanism.common.inventory.container.ContainerAdvancedElectricMachine;
import mekanism.common.inventory.container.ContainerChanceMachine;
import mekanism.common.inventory.container.ContainerChemicalCrystallizer;
import mekanism.common.inventory.container.ContainerChemicalDissolutionChamber;
import mekanism.common.inventory.container.ContainerChemicalInfuser;
import mekanism.common.inventory.container.ContainerChemicalOxidizer;
import mekanism.common.inventory.container.ContainerChemicalWasher;
import mekanism.common.inventory.container.ContainerDictionary;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.inventory.container.ContainerDoubleElectricMachine;
import mekanism.common.inventory.container.ContainerDynamicTank;
import mekanism.common.inventory.container.ContainerElectricMachine;
import mekanism.common.inventory.container.ContainerElectricPump;
import mekanism.common.inventory.container.ContainerElectrolyticSeparator;
import mekanism.common.inventory.container.ContainerEnergyCube;
import mekanism.common.inventory.container.ContainerFactory;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerFluidTank;
import mekanism.common.inventory.container.ContainerFluidicPlenisher;
import mekanism.common.inventory.container.ContainerFormulaicAssemblicator;
import mekanism.common.inventory.container.ContainerFuelwoodHeater;
import mekanism.common.inventory.container.ContainerGasTank;
import mekanism.common.inventory.container.ContainerInductionMatrix;
import mekanism.common.inventory.container.ContainerLaserAmplifier;
import mekanism.common.inventory.container.ContainerLaserTractorBeam;
import mekanism.common.inventory.container.ContainerMetallurgicInfuser;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.inventory.container.ContainerOredictionificator;
import mekanism.common.inventory.container.ContainerPRC;
import mekanism.common.inventory.container.ContainerPersonalChest;
import mekanism.common.inventory.container.ContainerQuantumEntangloporter;
import mekanism.common.inventory.container.ContainerResistiveHeater;
import mekanism.common.inventory.container.ContainerRotaryCondensentrator;
import mekanism.common.inventory.container.ContainerSecurityDesk;
import mekanism.common.inventory.container.ContainerSeismicVibrator;
import mekanism.common.inventory.container.ContainerSolarNeutronActivator;
import mekanism.common.inventory.container.ContainerTeleporter;
import mekanism.common.inventory.container.ContainerThermalEvaporationController;
import mekanism.common.inventory.container.ContainerUpgradeManagement;
import mekanism.common.inventory.container.robit.ContainerRobitCrafting;
import mekanism.common.inventory.container.robit.ContainerRobitInventory;
import mekanism.common.inventory.container.robit.ContainerRobitMain;
import mekanism.common.inventory.container.robit.ContainerRobitRepair;
import mekanism.common.inventory.container.robit.ContainerRobitSmelting;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.tile.TileEntityChanceMachine;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.tile.prefab.TileEntityDoubleElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import mekanism.common.voice.VoiceServerManager;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Common proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
public class CommonProxy implements IGuiProvider {

    protected final String[] API_PRESENT_MESSAGE = {"Mekanism API jar detected (Mekanism-<version>-api.jar),",
                                                    "please delete it from your mods folder and restart the game."};

    /**
     * Register tile entities that have special models. Overwritten in client to register TESRs.
     */
    public void registerTESRs() {
    }

    public void handleTeleporterUpdate(PortableTeleporterMessage message) {
    }

    /**
     * Register and load client-only item render information.
     */
    public void registerItemRenders() {
    }

    /**
     * Register and load client-only block render information.
     */
    public void registerBlockRenders() {
    }

    /**
     * Set and load the mod's common configuration properties.
     */
    public void loadConfiguration() {
        MekanismConfig.local().general.load(Mekanism.configuration);
        MekanismConfig.local().usage.load(Mekanism.configuration);
        MekanismConfig.local().storage.load(Mekanism.configuration);
        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }
    }

    /**
     * Set up and load the utilities this mod uses.
     */
    public void init() {
        MinecraftForge.EVENT_BUS.register(Mekanism.worldTickHandler);
    }

    /**
     * Whether or not the game is paused.
     */
    public boolean isPaused() {
        return false;
    }

    /**
     * Adds block hit effects on the client side.
     */
    public void addHitEffects(Coord4D coord, RayTraceResult mop) {
    }

    /**
     * Does the multiblock creation animation, starting from the rendering block.
     */
    public void doMultiblockSparkle(TileEntity tileEntity, BlockPos corner1, BlockPos corner2, INodeChecker checker) {
    }

    /**
     * Does the multiblock creation animation, starting from the rendering block.
     */
    public void doMultiblockSparkle(TileEntity tileEntity, BlockPos renderLoc, int length, int width, int height, INodeChecker checker) {
    }

    @Override
    public Object getClientGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        return null;
    }

    public Container getServerItemGui(EntityPlayer player, BlockPos pos) {
        int currentItem = pos.getX();
        int handOrdinal = pos.getY();
        if (currentItem < 0 || currentItem >= player.inventory.mainInventory.size() || handOrdinal < 0 || handOrdinal >= EnumHand.values().length) {
            //If it is out of bounds don't do anything
            return null;
        }
        ItemStack stack = player.inventory.getStackInSlot(currentItem);
        if (stack.isEmpty()) {
            return null;
        }
        EnumHand hand = EnumHand.values()[handOrdinal];
        int guiID = pos.getZ();
        //TODO: Decide if this should be a switch statement
        if (guiID == MachineType.PERSONAL_CHEST.guiId) {
            if (MachineType.get(stack) == MachineType.PERSONAL_CHEST) {
                //Ensure the item didn't change. From testing even if it did things still seemed to work properly but better safe than sorry
                return new ContainerPersonalChest(player.inventory, new InventoryPersonalChest(stack, hand));
            }
        } else if (guiID == 14) {
            if (stack.getItem() instanceof ItemPortableTeleporter) {
                return new ContainerNull();
            }
        } else if (guiID == 38) {
            if (stack.getItem() instanceof ItemSeismicReader) {
                return new ContainerNull();
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Container getServerGui(int ID, EntityPlayer player, World world, BlockPos pos) {
        if (ID == 1) {
            //ID == 1 used to be credits, now it is being used for Item Gui's
            return getServerItemGui(player, pos);
        }
        TileEntity tileEntity = world.getTileEntity(pos);
        switch (ID) {
            case 0:
                return new ContainerDictionary(player.inventory);
            //1 USED BEFORE SWITCH
            case 2:
                return new ContainerDigitalMiner(player.inventory, (TileEntityDigitalMiner) tileEntity);
            case 3:
                return new ContainerElectricMachine<>(player.inventory, (TileEntityElectricMachine) tileEntity);
            case 4:
                return new ContainerAdvancedElectricMachine<>(player.inventory, (TileEntityAdvancedElectricMachine) tileEntity);
            case 5:
                return new ContainerDoubleElectricMachine<>(player.inventory, (TileEntityDoubleElectricMachine) tileEntity);
            case 6:
                return new ContainerElectricMachine<>(player.inventory, (TileEntityElectricMachine) tileEntity);
            case 7:
                return new ContainerRotaryCondensentrator(player.inventory, (TileEntityRotaryCondensentrator) tileEntity);
            case 8:
                return new ContainerEnergyCube(player.inventory, (TileEntityEnergyCube) tileEntity);
            case 9:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 10:
                return new ContainerGasTank(player.inventory, (TileEntityGasTank) tileEntity);
            case 11:
                return new ContainerFactory(player.inventory, (TileEntityFactory) tileEntity);
            case 12:
                return new ContainerMetallurgicInfuser(player.inventory, (TileEntityMetallurgicInfuser) tileEntity);
            case 13:
                return new ContainerTeleporter(player.inventory, (TileEntityTeleporter) tileEntity);
            //EMPTY 14
            case 15:
                return new ContainerAdvancedElectricMachine<>(player.inventory, (TileEntityAdvancedElectricMachine) tileEntity);
            case 16:
                return new ContainerElectricMachine<>(player.inventory, (TileEntityElectricMachine) tileEntity);
            case 17:
                return new ContainerElectricPump(player.inventory, (TileEntityElectricPump) tileEntity);
            case 18:
                return new ContainerDynamicTank(player.inventory, (TileEntityDynamicTank) tileEntity);
            case 19:
                return new ContainerPersonalChest(player.inventory, (TileEntityPersonalChest) tileEntity);
            //EMPTY 20
            case 21:
                EntityRobit robit = (EntityRobit) world.getEntityByID(pos.getX());
                if (robit != null) {
                    return new ContainerRobitMain(player.inventory, robit);
                }
                return null;
            case 22:
                robit = (EntityRobit) world.getEntityByID(pos.getX());
                if (robit != null) {
                    return new ContainerRobitCrafting(player.inventory, robit);
                }
                return null;
            case 23:
                robit = (EntityRobit) world.getEntityByID(pos.getX());
                if (robit != null) {
                    return new ContainerRobitInventory(player.inventory, robit);
                }
                return null;
            case 24:
                robit = (EntityRobit) world.getEntityByID(pos.getX());
                if (robit != null) {
                    return new ContainerRobitSmelting(player.inventory, robit);
                }
                return null;
            case 25:
                robit = (EntityRobit) world.getEntityByID(pos.getX());
                if (robit != null) {
                    return new ContainerRobitRepair(player.inventory, robit);
                }
                return null;
            case 26:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 27:
                return new ContainerFilter(player.inventory, (TileEntityContainerBlock) tileEntity);
            case 28:
                return new ContainerFilter(player.inventory, (TileEntityContainerBlock) tileEntity);
            case 29:
                return new ContainerChemicalOxidizer(player.inventory, (TileEntityChemicalOxidizer) tileEntity);
            case 30:
                return new ContainerChemicalInfuser(player.inventory, (TileEntityChemicalInfuser) tileEntity);
            case 31:
                return new ContainerAdvancedElectricMachine<>(player.inventory, (TileEntityAdvancedElectricMachine) tileEntity);
            case 32:
                return new ContainerElectrolyticSeparator(player.inventory, (TileEntityElectrolyticSeparator) tileEntity);
            case 33:
                return new ContainerThermalEvaporationController(player.inventory, (TileEntityThermalEvaporationController) tileEntity);
            case 34:
                return new ContainerChanceMachine<>(player.inventory, (TileEntityChanceMachine) tileEntity);
            case 35:
                return new ContainerChemicalDissolutionChamber(player.inventory, (TileEntityChemicalDissolutionChamber) tileEntity);
            case 36:
                return new ContainerChemicalWasher(player.inventory, (TileEntityChemicalWasher) tileEntity);
            case 37:
                return new ContainerChemicalCrystallizer(player.inventory, (TileEntityChemicalCrystallizer) tileEntity);
            //EMPTY 38
            case 39:
                return new ContainerSeismicVibrator(player.inventory, (TileEntitySeismicVibrator) tileEntity);
            case 40:
                return new ContainerPRC(player.inventory, (TileEntityPRC) tileEntity);
            case 41:
                return new ContainerFluidTank(player.inventory, (TileEntityFluidTank) tileEntity);
            case 42:
                return new ContainerFluidicPlenisher(player.inventory, (TileEntityFluidicPlenisher) tileEntity);
            case 43:
                return new ContainerUpgradeManagement(player.inventory, (IUpgradeTile) tileEntity);
            case 44:
                return new ContainerLaserAmplifier(player.inventory, (TileEntityLaserAmplifier) tileEntity);
            case 45:
                return new ContainerLaserTractorBeam(player.inventory, (TileEntityLaserTractorBeam) tileEntity);
            case 46:
                return new ContainerQuantumEntangloporter(player.inventory, (TileEntityQuantumEntangloporter) tileEntity);
            case 47:
                return new ContainerSolarNeutronActivator(player.inventory, (TileEntitySolarNeutronActivator) tileEntity);
            case 48:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 49:
                return new ContainerInductionMatrix(player.inventory, (TileEntityInductionCasing) tileEntity);
            case 50:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 51:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 52:
                return new ContainerOredictionificator(player.inventory, (TileEntityOredictionificator) tileEntity);
            case 53:
                return new ContainerResistiveHeater(player.inventory, (TileEntityResistiveHeater) tileEntity);
            case 54:
                return new ContainerFilter(player.inventory, (TileEntityContainerBlock) tileEntity);
            case 55:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
            case 56:
                return new ContainerFormulaicAssemblicator(player.inventory, (TileEntityFormulaicAssemblicator) tileEntity);
            case 57:
                return new ContainerSecurityDesk(player.inventory, (TileEntitySecurityDesk) tileEntity);
            case 58:
                return new ContainerFuelwoodHeater(player.inventory, (TileEntityFuelwoodHeater) tileEntity);
            case 59:
                return new ContainerNull(player, (TileEntityContainerBlock) tileEntity);
        }
        return null;
    }

    public void preInit() {
    }

    public double getReach(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            return player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        }
        return 0;
    }

    /**
     * Gets the Minecraft base directory.
     *
     * @return base directory
     */
    public File getMinecraftDir() {
        return (File) FMLInjectionData.data()[6];
    }

    public void onConfigSync(boolean fromPacket) {
        if (MekanismConfig.current().general.cardboardSpawners.val()) {
            MekanismAPI.removeBoxBlacklist(Blocks.MOB_SPAWNER, OreDictionary.WILDCARD_VALUE);
        } else {
            MekanismAPI.addBoxBlacklist(Blocks.MOB_SPAWNER, OreDictionary.WILDCARD_VALUE);
        }
        if (MekanismConfig.current().general.voiceServerEnabled.val() && Mekanism.voiceManager == null) {
            Mekanism.voiceManager = new VoiceServerManager();
        }
        if (fromPacket) {
            Mekanism.logger.info("Received config from server.");
        }
    }

    public final WeakReference<EntityPlayer> getDummyPlayer(WorldServer world) {
        return MekFakePlayer.getInstance(world);
    }

    public final WeakReference<EntityPlayer> getDummyPlayer(WorldServer world, double x, double y, double z) {
        return MekFakePlayer.getInstance(world, x, y, z);
    }

    public final WeakReference<EntityPlayer> getDummyPlayer(WorldServer world, BlockPos pos) {
        return getDummyPlayer(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public EntityPlayer getPlayer(MessageContext context) {
        return context.getServerHandler().player;
    }

    public void handlePacket(Runnable runnable, EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            ((WorldServer) player.world).addScheduledTask(runnable);
        }
    }

    public int getGuiId(Block block, int metadata) {
        if (MachineType.get(block, metadata) != null) {
            return MachineType.get(block, metadata).guiId;
        } else if (block == MekanismBlocks.GasTank) {
            return 10;
        } else if (block == MekanismBlocks.EnergyCube) {
            return 8;
        }
        return -1;
    }

    public void renderLaser(World world, Pos3D from, Pos3D to, EnumFacing direction, double energy) {
    }

    public Object getFontRenderer() {
        return null;
    }

    public void throwApiPresentException() {
        throw new RuntimeException(String.join(" ", API_PRESENT_MESSAGE));
    }
}