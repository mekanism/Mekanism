package mekanism.client;

import java.io.File;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.Pos3D;
import mekanism.api.block.IColoredBlock;
import mekanism.api.text.EnumColor;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.client.entity.ParticleLaser;
import mekanism.client.gui.GuiPortableTeleporter;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.sound.SoundHandler;
import mekanism.common.CommonProxy;
import mekanism.common.MekanismBlock;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketPortableTeleporter;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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

    //TODO: Move this to a utils class
    private void registerBlockColorHandler(IBlockColor blockColor, IItemColor itemColor, MekanismBlock... blocks) {
        for (MekanismBlock mekanismBlock : blocks) {
            Minecraft.getInstance().getBlockColors().register(blockColor, mekanismBlock.getBlock());
            Minecraft.getInstance().getItemColors().register(itemColor, mekanismBlock.getItem());
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
                      Block block = ((BlockItem) item).getBlock();
                      if (block instanceof IColoredBlock) {
                          return MekanismRenderer.getColorARGB(((IColoredBlock) block).getColor(), 1);
                      }
                  }
                  return -1;
              },
              //Fluid Tank
              MekanismBlock.BASIC_FLUID_TANK, MekanismBlock.ADVANCED_FLUID_TANK, MekanismBlock.ELITE_FLUID_TANK, MekanismBlock.ULTIMATE_FLUID_TANK,
              MekanismBlock.CREATIVE_FLUID_TANK);

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
        //TODO: Move this to additions and fix. Currently there is no config sync so there isn't a great spot to put this
        /*if (fromPacket && MekanismConfig.general.voiceServerEnabled.get() && MekanismClient.voiceClient != null) {
            MekanismClient.voiceClient.start();
        }*/
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
    public void renderLaser(World world, Pos3D from, Pos3D to, Direction direction, double energy) {
        Minecraft.getInstance().particles.addEffect(new ParticleLaser(world, from, to, direction, energy));
    }

    @Override
    public FontRenderer getFontRenderer() {
        return Minecraft.getInstance().fontRenderer;
    }

    //TODO
    /*@Override
    public void throwApiPresentException() {
        throw new ApiJarPresentException(API_PRESENT_MESSAGE);
    }*/
}