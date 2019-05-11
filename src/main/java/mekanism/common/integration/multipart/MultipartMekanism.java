package mekanism.common.integration.multipart;

import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_1;
import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_2;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcmultipart.api.addon.IMCMPAddon;
import mcmultipart.api.addon.MCMPAddon;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.event.DrawMultipartHighlightEvent;
import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartRegistry;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartCapabilityHelper;
import mcmultipart.api.multipart.MultipartOcclusionHelper;
import mcmultipart.api.ref.MCMPCapabilities;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.world.IMultipartBlockAccess;
import mcmultipart.multipart.MultipartRegistry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.BlockTransmitter;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType.Size;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.TileEntityGlowPanel;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@MCMPAddon
public class MultipartMekanism implements IMCMPAddon {

    public static MultipartTransmitter TRANSMITTER_MP;
    public static MultipartGlowPanel GLOWPANEL_MP;

    public static boolean hasConnectionWith(TileEntity tile, EnumFacing side) {
        if (tile != null && tile.hasCapability(MCMPCapabilities.MULTIPART_TILE, null)) {
            IMultipartTile multipartTile = tile.getCapability(MCMPCapabilities.MULTIPART_TILE, null);
            if (multipartTile instanceof MultipartTile && ((MultipartTile) multipartTile).getID().equals("transmitter")) {
                IPartInfo partInfo = ((MultipartTile) multipartTile).getInfo();
                if (partInfo != null) {
                    for (IPartInfo info : partInfo.getContainer().getParts().values()) {
                        IMultipart multipart = info.getPart();
                        Collection<AxisAlignedBB> origBounds = getTransmitterSideBounds(multipartTile, side);
                        if (MultipartOcclusionHelper.testBoxIntersection(origBounds, multipart.getOcclusionBoxes(info))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static Collection<AxisAlignedBB> getTransmitterSideBounds(IMultipartTile tile, EnumFacing side) {
        if (tile.getTileEntity() instanceof TileEntityTransmitter) {
            TileEntityTransmitter transmitter = (TileEntityTransmitter) tile.getTileEntity();
            boolean large = transmitter.getTransmitterType().getSize() == Size.LARGE;
            AxisAlignedBB ret = large ? BlockTransmitter.largeSides[side.ordinal()] : BlockTransmitter.smallSides[side.ordinal()];
            return Collections.singletonList(ret);
        }
        return Collections.emptyList();
    }

    static IMultipartContainer getContainer(IBlockAccess world, BlockPos pos) {
        IMultipartContainer container = null;
        if (world instanceof IMultipartBlockAccess) {
            container = ((IMultipartBlockAccess) world).getPartInfo().getContainer();
        } else {
            TileEntity possibleContainer = world.getTileEntity(pos);
            if (possibleContainer instanceof IMultipartContainer) {
                container = (IMultipartContainer) possibleContainer;
            }
        }
        return container;
    }

    public static boolean hasCenterSlot(IBlockAccess world, BlockPos pos) {
        boolean hasCenterSlot = false;
        IMultipartContainer container = getContainer(world, pos);
        if (container != null) {
            hasCenterSlot = container.getPart(EnumCenterSlot.CENTER).isPresent();
        }
        return hasCenterSlot;
    }

    public static TileEntity unwrapTileEntity(IBlockAccess world) {
        TileEntity tile = null;
        if (world instanceof IMultipartBlockAccess) {
            tile = ((IMultipartBlockAccess) world).getPartInfo().getTile().getTileEntity();
        }
        return tile;
    }

    public static boolean placeMultipartBlock(Block block, ItemStack is, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, IBlockState state) {
        return ItemBlockMultipart.placeAt(is, player, player.getActiveHand(), world, pos, side, hitX, hitY, hitZ,
              block::getStateForPlacement, is.getMetadata(), MultipartRegistry.INSTANCE.getPart(block),
              ((ItemBlock) is.getItem())::placeBlockAt, ItemBlockMultipart::placePartAt);
    }

    @SubscribeEvent
    public void onAttachTile(AttachCapabilitiesEvent<TileEntity> event) {
        TileEntity tile = event.getObject();
        if (tile instanceof TileEntityTransmitter) {
            register(event, "transmitter");
        } else if (tile instanceof TileEntityGlowPanel) {
            register(event, "glow_panel");
        }
    }

    @Override
    public void registerParts(IMultipartRegistry registry) {
        MinecraftForge.EVENT_BUS.register(this);
        registry.registerPartWrapper(MekanismBlocks.Transmitter, TRANSMITTER_MP = new MultipartTransmitter());
        //registry.registerStackWrapper(Item.getItemFromBlock(MekanismBlocks.Transmitter), s -> true, MekanismBlocks.Transmitter);
        registry.registerPartWrapper(MekanismBlocks.GlowPanel, GLOWPANEL_MP = new MultipartGlowPanel());
        //registry.registerStackWrapper(Item.getItemFromBlock(MekanismBlocks.GlowPanel), s -> true, MekanismBlocks.GlowPanel);
        MultipartCapabilityHelper.registerCapabilityJoiner(Capabilities.TILE_NETWORK_CAPABILITY, MultipartTileNetworkJoiner::new);
    }

    private void register(AttachCapabilitiesEvent<TileEntity> e, String id) {
        e.addCapability(new ResourceLocation(Mekanism.MODID, id), new ICapabilityProvider() {
            private MultipartTile tile;

            @Override
            public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == MCMPCapabilities.MULTIPART_TILE;
            }

            @Nullable
            @Override
            public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                if (capability == MCMPCapabilities.MULTIPART_TILE) {
                    if (tile == null) {
                        tile = new MultipartTile(e.getObject(), id);
                    }
                    return MCMPCapabilities.MULTIPART_TILE.cast(tile);
                }
                return null;
            }
        });
    }

    public void init() {
        registerMicroMaterials();
    }

    public void registerMicroMaterials() {
        for (int i = 0; i < 16; i++) {
            FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.BasicBlock, 1, i));

            if (!MachineType.get(MACHINE_BLOCK_1, i).hasModel) {
                FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.MachineBlock, 1, i));
            }

            if (!MachineType.get(MACHINE_BLOCK_2, i).hasModel) {
                FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.MachineBlock2, 1, i));
            }
        }

        FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.BasicBlock2, 1, 0));
        FMLInterModComms.sendMessage("ForgeMicroblock", "microMaterial", new ItemStack(MekanismBlocks.CardboardBox));
    }

    //No idea why mcmultipart doesnt do this itself...
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void drawBlockHighlightEvent(DrawMultipartHighlightEvent ev) {
        IBlockState state = ev.getPartInfo().getState();
        if (state.getBlock() == MekanismBlocks.GlowPanel || state.getBlock() == MekanismBlocks.Transmitter) {
            EntityPlayer player = ev.getPlayer();
            @SuppressWarnings("deprecation")
            AxisAlignedBB bb = state.getBlock().getSelectedBoundingBox(state, ev.getPartInfo().getPartWorld(), ev.getPartInfo().getPartPos());
            //NB rendering code copied from MCMultipart
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                  GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * ev.getPartialTicks();
            double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * ev.getPartialTicks();
            double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * ev.getPartialTicks();
            RenderGlobal.drawSelectionBoundingBox(bb.grow(0.002).offset(-x, -y, -z), 0.0F, 0.0F, 0.0F, 0.4F);
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            ev.setCanceled(true);
        }
    }
}