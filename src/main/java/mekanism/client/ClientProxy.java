package mekanism.client;

import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.Pos3D;
import mekanism.api.text.EnumColor;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.client.entity.ParticleLaser;
import mekanism.client.gui.GuiDictionary;
import mekanism.client.gui.GuiGasTank;
import mekanism.client.gui.GuiPortableTeleporter;
import mekanism.client.gui.GuiTeleporter;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.entity.RenderBalloon;
import mekanism.client.render.entity.RenderFlame;
import mekanism.client.render.entity.RenderObsidianTNTPrimed;
import mekanism.client.render.entity.RenderRobit;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.block.RenderChemicalCrystallizerItem;
import mekanism.client.render.item.block.RenderChemicalDissolutionChamberItem;
import mekanism.client.render.item.block.RenderDigitalMinerItem;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.item.block.RenderPersonalChestItem;
import mekanism.client.render.item.block.RenderQuantumEntangloporterItem;
import mekanism.client.render.item.block.RenderResistiveHeaterItem;
import mekanism.client.render.item.block.RenderSecurityDeskItem;
import mekanism.client.render.item.block.RenderSeismicVibratorItem;
import mekanism.client.render.item.block.RenderSolarNeutronActivatorItem;
import mekanism.client.render.item.gear.RenderArmoredJetpack;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.client.render.item.gear.RenderGasMask;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.client.render.item.gear.RenderScubaTank;
import mekanism.client.render.obj.MekanismOBJLoader;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderChemicalCrystallizer;
import mekanism.client.render.tileentity.RenderChemicalDissolutionChamber;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderGasTank;
import mekanism.client.render.tileentity.RenderPersonalChest;
import mekanism.client.render.tileentity.RenderQuantumEntangloporter;
import mekanism.client.render.tileentity.RenderResistiveHeater;
import mekanism.client.render.tileentity.RenderSecurityDesk;
import mekanism.client.render.tileentity.RenderSeismicVibrator;
import mekanism.client.render.tileentity.RenderSolarNeutronActivator;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.render.tileentity.RenderThermalEvaporationController;
import mekanism.client.render.tileentity.RenderThermoelectricBoiler;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.client.render.transmitter.RenderPressurizedTube;
import mekanism.client.render.transmitter.RenderThermodynamicConductor;
import mekanism.client.render.transmitter.RenderUniversalCable;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonProxy;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismItem;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.config.MekanismConfig;
import mekanism.common.entity.EntityBabySkeleton;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityObsidianTNT;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainerTypes;
import mekanism.common.item.ItemBalloon;
import mekanism.common.network.PacketPortableTeleporter;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityBoilerValve;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityResistiveHeater;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.tile.bin.TileEntityBin;
import mekanism.common.tile.energy_cube.TileEntityEnergyCube;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.fluid_tank.TileEntityFluidTank;
import mekanism.common.tile.gas_tank.TileEntityGasTank;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityLogisticalTransporter;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityRestrictiveTransporter;
import mekanism.common.tile.transmitter.mechanical_pipe.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.pressurized_tube.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.thermodynamic_conductor.TileEntityThermodynamicConductor;
import mekanism.common.tile.transmitter.universal_cable.TileEntityUniversalCable;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Client proxy for the Mekanism mod.
 *
 * @author AidanBrady
 */
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();
        /*MekanismConfig.current().client.load(Mekanism.configuration);
        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }*/
    }

    @Override
    public void registerTESRs() {
        //TODO?
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBin.class, new RenderBin());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoilerCasing.class, new RenderThermoelectricBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBoilerValve.class, new RenderThermoelectricBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalCrystallizer.class, new RenderChemicalCrystallizer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalDissolutionChamber.class, new RenderChemicalDissolutionChamber());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChemicalInjectionChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCombiner.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrusher.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDigitalMiner.class, new RenderDigitalMiner());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDiversionTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicTank.class, new RenderDynamicTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDynamicValve.class, new RenderDynamicTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergizedSmelter.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyCube.class, new RenderEnergyCube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnrichmentChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFactory.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFluidTank.class, RenderFluidTank.INSTANCE);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFormulaicAssemblicator.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGasTank.class, new RenderGasTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLogisticalTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMechanicalPipe.class, new RenderMechanicalPipe());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMetallurgicInfuser.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOsmiumCompressor.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPRC.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPersonalChest.class, new RenderPersonalChest());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPrecisionSawmill.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPressurizedTube.class, new RenderPressurizedTube());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPurificationChamber.class, new RenderConfigurableMachine<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityQuantumEntangloporter.class, new RenderQuantumEntangloporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityResistiveHeater.class, new RenderResistiveHeater());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRestrictiveTransporter.class, new RenderLogisticalTransporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySecurityDesk.class, new RenderSecurityDesk());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySeismicVibrator.class, new RenderSeismicVibrator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolarNeutronActivator.class, new RenderSolarNeutronActivator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTeleporter.class, new RenderTeleporter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermalEvaporationController.class, new RenderThermalEvaporationController());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityThermodynamicConductor.class, new RenderThermodynamicConductor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUniversalCable.class, new RenderUniversalCable());
    }

    @Override
    public void registerItemRenders() {
        //TODO: Redo this stuff via json
        /*setCustomModelResourceLocation(getInventoryMRL("balloon"), MekanismItem.BLACK_BALLOON, MekanismItem.RED_BALLOON, MekanismItem.GREEN_BALLOON,
              MekanismItem.BROWN_BALLOON, MekanismItem.BLUE_BALLOON, MekanismItem.PURPLE_BALLOON, MekanismItem.CYAN_BALLOON, MekanismItem.LIGHT_GRAY_BALLOON,
              MekanismItem.GRAY_BALLOON, MekanismItem.PINK_BALLOON, MekanismItem.LIME_BALLOON, MekanismItem.YELLOW_BALLOON, MekanismItem.LIGHT_BLUE_BALLOON,
              MekanismItem.MAGENTA_BALLOON, MekanismItem.ORANGE_BALLOON, MekanismItem.WHITE_BALLOON);

        ModelBakery.registerItemVariants(MekanismItem.WALKIE_TALKIE.getItem(), ItemWalkieTalkie.OFF_MODEL);

        for (int i = 1; i <= 9; i++) {
            ModelBakery.registerItemVariants(MekanismItem.WALKIE_TALKIE.getItem(), ItemWalkieTalkie.getModel(i));
        }

        ModelBakery.registerItemVariants(MekanismItem.CRAFTING_FORMULA.getItem(), ItemCraftingFormula.MODEL, ItemCraftingFormula.INVALID_MODEL, ItemCraftingFormula.ENCODED_MODEL);

        //Register the item inventory model locations for the various blocks
        for (MekanismBlock mekanismBlock : MekanismBlock.values()) {
            BlockItem item = mekanismBlock.getItem();
            if (item instanceof IItemRedirectedModel) {
                ModelLoader.setCustomModelResourceLocation(item, 0, getInventoryMRL(((IItemRedirectedModel) item).getRedirectLocation()));
            } else {
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        }
        //TODO: Maybe have it be two different items/blocks one for full one for not given metadata is going away?
        ModelLoader.setCustomModelResourceLocation(MekanismBlock.CARDBOARD_BOX.getItem(), 0, new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "cardboard_box"), "storage=false"));
        ModelLoader.setCustomModelResourceLocation(MekanismBlock.CARDBOARD_BOX.getItem(), 1, new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, "cardboard_box"), "storage=true"));*/
    }

    /*private void setCustomModelResourceLocation(ModelResourceLocation model, MekanismItem... items) {
        for (MekanismItem mekanismItem : items) {
            ModelLoader.setCustomModelResourceLocation(mekanismItem.getItem(), 0, model);
        }
    }*/

    @Override
    public void registerBlockRenders() {
        //TODO: Redo all of these. Lots can probably just be done with json now. It is probably a good idea to do the ones, that can be done
        // in json with it, EVEN if it requires more skeleton json files.

        /*setCustomTransmitterMeshDefinition(MekanismBlock.BASIC_UNIVERSAL_CABLE, MekanismBlock.ADVANCED_UNIVERSAL_CABLE, MekanismBlock.ELITE_UNIVERSAL_CABLE,
              MekanismBlock.ULTIMATE_UNIVERSAL_CABLE, MekanismBlock.BASIC_MECHANICAL_PIPE, MekanismBlock.ADVANCED_MECHANICAL_PIPE, MekanismBlock.ELITE_MECHANICAL_PIPE,
              MekanismBlock.ULTIMATE_MECHANICAL_PIPE, MekanismBlock.BASIC_PRESSURIZED_TUBE, MekanismBlock.ADVANCED_PRESSURIZED_TUBE, MekanismBlock.ELITE_PRESSURIZED_TUBE,
              MekanismBlock.ULTIMATE_PRESSURIZED_TUBE, MekanismBlock.BASIC_LOGISTICAL_TRANSPORTER, MekanismBlock.ADVANCED_LOGISTICAL_TRANSPORTER,
              MekanismBlock.ELITE_LOGISTICAL_TRANSPORTER, MekanismBlock.ULTIMATE_LOGISTICAL_TRANSPORTER, MekanismBlock.RESTRICTIVE_TRANSPORTER,
              MekanismBlock.DIVERSION_TRANSPORTER, MekanismBlock.BASIC_THERMODYNAMIC_CONDUCTOR, MekanismBlock.ADVANCED_THERMODYNAMIC_CONDUCTOR,
              MekanismBlock.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismBlock.ULTIMATE_THERMODYNAMIC_CONDUCTOR);

        //Walkie Talkie dynamic texture
        ModelLoader.setCustomMeshDefinition(MekanismItem.WALKIE_TALKIE.getItem(), stack -> {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemWalkieTalkie) {
                ItemWalkieTalkie item = (ItemWalkieTalkie) stack.getItem();
                if (item.getOn(stack)) {
                    return ItemWalkieTalkie.CHANNEL_MODELS.get(item.getChannel(stack));
                }
            }
            return ItemWalkieTalkie.OFF_MODEL;
        });

        //Crafting Formula dynamic texture
        ModelLoader.setCustomMeshDefinition(MekanismItem.CRAFTING_FORMULA.getItem(), stack -> {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemCraftingFormula) {
                ItemCraftingFormula item = (ItemCraftingFormula) stack.getItem();
                if (item.getInventory(stack) == null) {
                    return ItemCraftingFormula.MODEL;
                }
                return item.isInvalid(stack) ? ItemCraftingFormula.INVALID_MODEL : ItemCraftingFormula.ENCODED_MODEL;
            }
            return ItemCraftingFormula.MODEL;
        });*/

        OBJLoader.INSTANCE.addDomain(Mekanism.MODID);
    }

    /*private void setCustomTransmitterMeshDefinition(MekanismBlock... transmitters) {
        for (MekanismBlock transmitter : transmitters) {
            ModelLoader.setCustomMeshDefinition(transmitter.getItem(), stack -> new ModelResourceLocation(transmitter.getBlock().getRegistryName(), "inventory"));
        }
    }

    public void registerItemRender(MekanismItem item) {
        ModelLoader.setCustomModelResourceLocation(item.getItem(), 0, new ModelResourceLocation(item.getItem().getRegistryName(), "inventory"));
    }*/

    @Override
    public void registerScreenHandlers() {
        //TODO: Make sure all are here
        ScreenManager.registerFactory(MekanismContainerTypes.DICTIONARY, GuiDictionary::new);
        ScreenManager.registerFactory(MekanismContainerTypes.GAS_TANK, GuiGasTank::new);
    }

    @Override
    public void handleTeleporterUpdate(PacketPortableTeleporter message) {
        Screen screen = Minecraft.getInstance().currentScreen;

        if (screen instanceof GuiPortableTeleporter) {
            GuiPortableTeleporter teleporter = (GuiPortableTeleporter) screen;
            teleporter.setStatus(message.getStatus());
            teleporter.setFrequency(message.getFrequency());
            teleporter.setPublicCache(message.getPublicCache());
            teleporter.setPrivateCache(message.getPrivateCache());
            teleporter.updateButtons();
        }
    }

    @Override
    public void addHitEffects(Coord4D coord, BlockRayTraceResult mop) {
        if (Minecraft.getInstance().world != null) {
            Minecraft.getInstance().particles.addBlockHitEffects(coord.getPos(), mop);
        }
    }

    private void doSparkle(TileEntity tileEntity, SparkleAnimation anim) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        // If player is within 16 blocks (256 = 16^2), show the status message/sparkles
        if (tileEntity.getPos().distanceSq(player.getPosition()) <= 256) {
            if (MekanismConfig.client.enableMultiblockFormationParticles.get()) {
                anim.run();
            } else {
                player.sendStatusMessage(TextComponentUtil.build(EnumColor.INDIGO, Translation.of("chat.mek.multiblockformed")), true);
            }
        }
    }

    @Override
    public void doMultiblockSparkle(TileEntity tileEntity, BlockPos renderLoc, int length, int width, int height, INodeChecker checker) {
        doSparkle(tileEntity, new SparkleAnimation(tileEntity, renderLoc, length, width, height, checker));
    }

    @Override
    public void doMultiblockSparkle(TileEntity tileEntity, BlockPos corner1, BlockPos corner2, INodeChecker checker) {
        doSparkle(tileEntity, new SparkleAnimation(tileEntity, corner1, corner2, checker));
    }

    private void registerBlockColorHandler(IBlockColor blockColor, IItemColor itemColor, MekanismBlock... blocks) {
        for (MekanismBlock mekanismBlock : blocks) {
            Minecraft.getInstance().getBlockColors().register(blockColor, mekanismBlock.getBlock());
            Minecraft.getInstance().getItemColors().register(itemColor, mekanismBlock.getItem());
        }
    }

    private void registerItemColorHandler(IItemColor itemColor, MekanismItem... items) {
        for (MekanismItem mekanismItem : items) {
            Minecraft.getInstance().getItemColors().register(itemColor, mekanismItem.getItem());
        }
    }

    @Override
    public void init() {
        super.init();
        registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
                  Block block = state.getBlock();
                  if (block instanceof IColoredBlock) {
                      return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
                  }
                  return -1;
              }, (stack, tintIndex) -> {
                  Item item = stack.getItem();
                  if (item instanceof BlockItem) {
                      //TODO: Fix Glow panel item coloring
                      Block block = ((BlockItem) item).getBlock();
                      if (block instanceof IColoredBlock) {
                          return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
                      }
                  }
                  return -1;
              },
              //Fluid Tank
              MekanismBlock.BASIC_FLUID_TANK, MekanismBlock.ADVANCED_FLUID_TANK, MekanismBlock.ELITE_FLUID_TANK, MekanismBlock.ULTIMATE_FLUID_TANK, MekanismBlock.CREATIVE_FLUID_TANK,
              //Plastic Blocks
              MekanismBlock.BLACK_PLASTIC_BLOCK, MekanismBlock.RED_PLASTIC_BLOCK, MekanismBlock.GREEN_PLASTIC_BLOCK, MekanismBlock.BROWN_PLASTIC_BLOCK,
              MekanismBlock.BLUE_PLASTIC_BLOCK, MekanismBlock.PURPLE_PLASTIC_BLOCK, MekanismBlock.CYAN_PLASTIC_BLOCK, MekanismBlock.LIGHT_GRAY_PLASTIC_BLOCK,
              MekanismBlock.GRAY_PLASTIC_BLOCK, MekanismBlock.PINK_PLASTIC_BLOCK, MekanismBlock.LIME_PLASTIC_BLOCK, MekanismBlock.YELLOW_PLASTIC_BLOCK,
              MekanismBlock.LIGHT_BLUE_PLASTIC_BLOCK, MekanismBlock.MAGENTA_PLASTIC_BLOCK, MekanismBlock.ORANGE_PLASTIC_BLOCK, MekanismBlock.WHITE_PLASTIC_BLOCK,
              //Slick Plastic Blocks
              MekanismBlock.BLACK_SLICK_PLASTIC_BLOCK, MekanismBlock.RED_SLICK_PLASTIC_BLOCK, MekanismBlock.GREEN_SLICK_PLASTIC_BLOCK,
              MekanismBlock.BROWN_SLICK_PLASTIC_BLOCK, MekanismBlock.BLUE_SLICK_PLASTIC_BLOCK, MekanismBlock.PURPLE_SLICK_PLASTIC_BLOCK, MekanismBlock.CYAN_SLICK_PLASTIC_BLOCK,
              MekanismBlock.LIGHT_GRAY_SLICK_PLASTIC_BLOCK, MekanismBlock.GRAY_SLICK_PLASTIC_BLOCK, MekanismBlock.PINK_SLICK_PLASTIC_BLOCK,
              MekanismBlock.LIME_SLICK_PLASTIC_BLOCK, MekanismBlock.YELLOW_SLICK_PLASTIC_BLOCK, MekanismBlock.LIGHT_BLUE_SLICK_PLASTIC_BLOCK,
              MekanismBlock.MAGENTA_SLICK_PLASTIC_BLOCK, MekanismBlock.ORANGE_SLICK_PLASTIC_BLOCK, MekanismBlock.WHITE_SLICK_PLASTIC_BLOCK,
              //Plastic Glow Blocks
              MekanismBlock.BLACK_PLASTIC_GLOW_BLOCK, MekanismBlock.RED_PLASTIC_GLOW_BLOCK, MekanismBlock.GREEN_PLASTIC_GLOW_BLOCK, MekanismBlock.BROWN_PLASTIC_GLOW_BLOCK,
              MekanismBlock.BLUE_PLASTIC_GLOW_BLOCK, MekanismBlock.PURPLE_PLASTIC_GLOW_BLOCK, MekanismBlock.CYAN_PLASTIC_GLOW_BLOCK, MekanismBlock.LIGHT_GRAY_PLASTIC_GLOW_BLOCK,
              MekanismBlock.GRAY_PLASTIC_GLOW_BLOCK, MekanismBlock.PINK_PLASTIC_GLOW_BLOCK, MekanismBlock.LIME_PLASTIC_GLOW_BLOCK, MekanismBlock.YELLOW_PLASTIC_GLOW_BLOCK,
              MekanismBlock.LIGHT_BLUE_PLASTIC_GLOW_BLOCK, MekanismBlock.MAGENTA_PLASTIC_GLOW_BLOCK, MekanismBlock.ORANGE_PLASTIC_GLOW_BLOCK, MekanismBlock.WHITE_PLASTIC_GLOW_BLOCK,
              //Reinforced Plastic Blocks
              MekanismBlock.BLACK_REINFORCED_PLASTIC_BLOCK, MekanismBlock.RED_REINFORCED_PLASTIC_BLOCK, MekanismBlock.GREEN_REINFORCED_PLASTIC_BLOCK,
              MekanismBlock.BROWN_REINFORCED_PLASTIC_BLOCK, MekanismBlock.BLUE_REINFORCED_PLASTIC_BLOCK, MekanismBlock.PURPLE_REINFORCED_PLASTIC_BLOCK,
              MekanismBlock.CYAN_REINFORCED_PLASTIC_BLOCK, MekanismBlock.LIGHT_GRAY_REINFORCED_PLASTIC_BLOCK, MekanismBlock.GRAY_REINFORCED_PLASTIC_BLOCK,
              MekanismBlock.PINK_REINFORCED_PLASTIC_BLOCK, MekanismBlock.LIME_REINFORCED_PLASTIC_BLOCK, MekanismBlock.YELLOW_REINFORCED_PLASTIC_BLOCK,
              MekanismBlock.LIGHT_BLUE_REINFORCED_PLASTIC_BLOCK, MekanismBlock.MAGENTA_REINFORCED_PLASTIC_BLOCK, MekanismBlock.ORANGE_REINFORCED_PLASTIC_BLOCK,
              MekanismBlock.WHITE_REINFORCED_PLASTIC_BLOCK,
              //Plastic Road
              MekanismBlock.BLACK_PLASTIC_ROAD, MekanismBlock.RED_PLASTIC_ROAD, MekanismBlock.GREEN_PLASTIC_ROAD, MekanismBlock.BROWN_PLASTIC_ROAD,
              MekanismBlock.BLUE_PLASTIC_ROAD, MekanismBlock.PURPLE_PLASTIC_ROAD, MekanismBlock.CYAN_PLASTIC_ROAD, MekanismBlock.LIGHT_GRAY_PLASTIC_ROAD,
              MekanismBlock.GRAY_PLASTIC_ROAD, MekanismBlock.PINK_PLASTIC_ROAD, MekanismBlock.LIME_PLASTIC_ROAD, MekanismBlock.YELLOW_PLASTIC_ROAD,
              MekanismBlock.LIGHT_BLUE_PLASTIC_ROAD, MekanismBlock.MAGENTA_PLASTIC_ROAD, MekanismBlock.ORANGE_PLASTIC_ROAD, MekanismBlock.WHITE_PLASTIC_ROAD,
              //Plastic Fences
              MekanismBlock.BLACK_PLASTIC_FENCE, MekanismBlock.RED_PLASTIC_FENCE, MekanismBlock.GREEN_PLASTIC_FENCE, MekanismBlock.BROWN_PLASTIC_FENCE,
              MekanismBlock.BLUE_PLASTIC_FENCE, MekanismBlock.PURPLE_PLASTIC_FENCE, MekanismBlock.CYAN_PLASTIC_FENCE, MekanismBlock.LIGHT_GRAY_PLASTIC_FENCE,
              MekanismBlock.GRAY_PLASTIC_FENCE, MekanismBlock.PINK_PLASTIC_FENCE, MekanismBlock.LIME_PLASTIC_FENCE, MekanismBlock.YELLOW_PLASTIC_FENCE,
              MekanismBlock.LIGHT_BLUE_PLASTIC_FENCE, MekanismBlock.MAGENTA_PLASTIC_FENCE, MekanismBlock.ORANGE_PLASTIC_FENCE, MekanismBlock.WHITE_PLASTIC_FENCE,
              //Glow Panels
              MekanismBlock.BLACK_GLOW_PANEL, MekanismBlock.RED_GLOW_PANEL, MekanismBlock.GREEN_GLOW_PANEL, MekanismBlock.BROWN_GLOW_PANEL,
              MekanismBlock.BLUE_GLOW_PANEL, MekanismBlock.PURPLE_GLOW_PANEL, MekanismBlock.CYAN_GLOW_PANEL, MekanismBlock.LIGHT_GRAY_GLOW_PANEL,
              MekanismBlock.GRAY_GLOW_PANEL, MekanismBlock.PINK_GLOW_PANEL, MekanismBlock.LIME_GLOW_PANEL, MekanismBlock.YELLOW_GLOW_PANEL,
              MekanismBlock.LIGHT_BLUE_GLOW_PANEL, MekanismBlock.MAGENTA_GLOW_PANEL, MekanismBlock.ORANGE_GLOW_PANEL, MekanismBlock.WHITE_GLOW_PANEL);
        registerItemColorHandler((stack, tintIndex) -> {
                  Item item = stack.getItem();
                  if (item instanceof ItemBalloon) {
                      ItemBalloon balloon = (ItemBalloon) item;
                      return MekanismRenderer.getColorARGB(balloon.getColor(), 1);
                  }
                  return -1;
              }, MekanismItem.BLACK_BALLOON, MekanismItem.RED_BALLOON, MekanismItem.GREEN_BALLOON, MekanismItem.BROWN_BALLOON, MekanismItem.BLUE_BALLOON,
              MekanismItem.PURPLE_BALLOON, MekanismItem.CYAN_BALLOON, MekanismItem.LIGHT_GRAY_BALLOON, MekanismItem.GRAY_BALLOON, MekanismItem.PINK_BALLOON,
              MekanismItem.LIME_BALLOON, MekanismItem.YELLOW_BALLOON, MekanismItem.LIGHT_BLUE_BALLOON, MekanismItem.MAGENTA_BALLOON, MekanismItem.ORANGE_BALLOON,
              MekanismItem.WHITE_BALLOON);

        //MinecraftForge.EVENT_BUS.register(new ClientConnectionHandler());
        MinecraftForge.EVENT_BUS.register(new ClientPlayerTracker());
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
        MinecraftForge.EVENT_BUS.register(new RenderTickHandler());
        MinecraftForge.EVENT_BUS.register(SoundHandler.class);

        new MekanismKeyHandler();

        HolidayManager.init();
    }

    @Override
    public void onConfigSync(boolean fromPacket) {
        super.onConfigSync(fromPacket);
        if (fromPacket && MekanismConfig.general.voiceServerEnabled.get() && MekanismClient.voiceClient != null) {
            MekanismClient.voiceClient.start();
        }
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        registerItemStackModel(modelRegistry, "jetpack", model -> RenderJetpack.model = model);
        registerItemStackModel(modelRegistry, "jetpack_armored", model -> RenderArmoredJetpack.model = model);
        registerItemStackModel(modelRegistry, "gas_mask", model -> RenderGasMask.model = model);
        registerItemStackModel(modelRegistry, "scuba_tank", model -> RenderScubaTank.model = model);
        registerItemStackModel(modelRegistry, "free_runners", model -> RenderFreeRunners.model = model);
        registerItemStackModel(modelRegistry, "atomic_disassembler", model -> RenderAtomicDisassembler.model = model);
        registerItemStackModel(modelRegistry, "flamethrower", model -> RenderFlameThrower.model = model);
        registerItemStackModel(modelRegistry, "digital_miner", model -> RenderDigitalMinerItem.model = model);
        registerItemStackModel(modelRegistry, "solar_neutron_activator", model -> RenderSolarNeutronActivatorItem.model = model);
        registerItemStackModel(modelRegistry, "chemical_dissolution_chamber", model -> RenderChemicalDissolutionChamberItem.model = model);
        registerItemStackModel(modelRegistry, "chemical_crystallizer", model -> RenderChemicalCrystallizerItem.model = model);
        registerItemStackModel(modelRegistry, "seismic_vibrator", model -> RenderSeismicVibratorItem.model = model);
        registerItemStackModel(modelRegistry, "quantum_entangloporter", model -> RenderQuantumEntangloporterItem.model = model);
        registerItemStackModel(modelRegistry, "resistive_heater", model -> RenderResistiveHeaterItem.model = model);
        registerItemStackModel(modelRegistry, "personal_chest", model -> RenderPersonalChestItem.model = model);
        registerItemStackModel(modelRegistry, "security_desk", model -> RenderSecurityDeskItem.model = model);

        //TODO: Does tier matter
        registerItemStackModel(modelRegistry, "energy_cube", model -> RenderEnergyCubeItem.model = model);
        registerItemStackModel(modelRegistry, "fluid_tank", model -> RenderFluidTankItem.model = model);
    }

    private ModelResourceLocation getInventoryMRL(String type) {
        return new ModelResourceLocation(new ResourceLocation(Mekanism.MODID, type), "inventory");
    }

    private void registerItemStackModel(Map<ResourceLocation, IBakedModel> modelRegistry, String type, Function<ItemLayerWrapper, IBakedModel> setModel) {
        ModelResourceLocation resourceLocation = getInventoryMRL(type);
        modelRegistry.put(resourceLocation, setModel.apply(new ItemLayerWrapper(modelRegistry.get(resourceLocation))));
    }

    @Override
    public void preInit() {
        MekanismRenderer.init();

        ModelLoaderRegistry.registerLoader(MekanismOBJLoader.INSTANCE);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(MekanismOBJLoader.INSTANCE);

        //Register entity rendering handlers
        RenderingRegistry.registerEntityRenderingHandler(EntityObsidianTNT.class, RenderObsidianTNTPrimed::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityRobit.class, RenderRobit::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBalloon.class, RenderBalloon::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBabySkeleton.class, SkeletonRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFlame.class, RenderFlame::new);
    }

    @Override
    public double getReach(PlayerEntity player) {
        return Minecraft.getInstance().playerController.getBlockReachDistance();
    }

    @Override
    public boolean isPaused() {
        if (Minecraft.getInstance().isSingleplayer() && !Minecraft.getInstance().getIntegratedServer().getPublic()) {
            //TODO: Make sure that gui's that pause game react to this properly
            return Minecraft.getInstance().isGamePaused();
        }
        return false;
    }

    @Override
    public File getMinecraftDir() {
        return Minecraft.getInstance().gameDir;
    }

    @Override
    public PlayerEntity getPlayer(Supplier<Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER || context.get().getDirection() == NetworkDirection.LOGIN_TO_SERVER) {
            return context.get().getSender();
        }
        return Minecraft.getInstance().player;
    }

    @Override
    public void handlePacket(Runnable runnable, PlayerEntity player) {
        if (player == null || player.world.isRemote) {
            Minecraft.getInstance().addScheduledTask(runnable);
        } else {
            //Single player
            if (player.world instanceof ServerWorld) {
                ((ServerWorld) player.world).addScheduledTask(runnable);
            } else {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    server.addScheduledTask(runnable);
                } else {
                    Mekanism.logger.error("Packet handler wanted to set a scheduled task, but we couldn't find a way to set one.");
                    Mekanism.logger.error("Player = {}, World = {}", player, player.world);
                }
            }
        }
    }

    @Override
    public void renderLaser(World world, Pos3D from, Pos3D to, Direction direction, double energy) {
        Minecraft.getInstance().particles.addEffect(new ParticleLaser(world, from, to, direction, energy));
    }

    @Override
    public FontRenderer getFontRenderer() {
        return Minecraft.getInstance().fontRenderer;
    }

    @Override
    public void throwApiPresentException() {
        throw new ApiJarPresentException(API_PRESENT_MESSAGE);
    }
}