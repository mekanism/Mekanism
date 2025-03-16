package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderLogisticalTransporter extends RenderTransmitterBase<TileEntityLogisticalTransporterBase> {

    private static final Map<Direction, Model3D> cachedOverlays = new EnumMap<>(Direction.class);
    private static final int DIVERSION_OVERLAY_ARGB = MekanismRenderer.getColorARGB(0xFFFFFF, 0.8F);
    @Nullable
    private static TextureAtlasSprite gunpowderIcon;
    @Nullable
    private static TextureAtlasSprite torchOffIcon;
    @Nullable
    private static TextureAtlasSprite torchOnIcon;
    private final ModelTransporterBox modelBox;
    private final LazyItemRenderer itemRenderer = new LazyItemRenderer();

    public RenderLogisticalTransporter(BlockEntityRendererProvider.Context context) {
        super(context);
        modelBox = new ModelTransporterBox(context.getModelSet());
    }

    public static void onStitch(TextureAtlas map) {
        cachedOverlays.clear();
        gunpowderIcon = map.getSprite(ResourceLocation.withDefaultNamespace("item/gunpowder"));
        torchOffIcon = map.getSprite(ResourceLocation.withDefaultNamespace("block/redstone_torch_off"));
        torchOnIcon = map.getSprite(ResourceLocation.withDefaultNamespace("block/redstone_torch"));
    }

    @Override
    protected void render(TileEntityLogisticalTransporterBase tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        LogisticalTransporterBase transporter = tile.getTransmitter();
        BlockPos pos = tile.getBlockPos();
        if (!MekanismConfig.client.opaqueTransmitters.get()) {
            Collection<TransporterStack> inTransit = transporter.getTransit();
            if (!inTransit.isEmpty()) {
                matrix.pushPose();
                itemRenderer.init(tile.getLevel(), pos);

                float partial = partialTick * transporter.tier.getSpeed();
                Collection<TransporterStack> reducedTransit = getReducedTransit(inTransit);
                for (TransporterStack stack : reducedTransit) {
                    float[] stackPos = TransporterUtils.getStackPosition(transporter, stack, partial);
                    matrix.pushPose();
                    matrix.translate(stackPos[0], stackPos[1], stackPos[2]);
                    matrix.scale(0.75F, 0.75F, 0.75F);
                    itemRenderer.renderAsStack(matrix, renderer, stack.itemStack, light);
                    matrix.popPose();
                    if (stack.color != null) {
                        modelBox.render(matrix, renderer, overlayLight, stackPos[0], stackPos[1], stackPos[2], stack.color);
                    }
                }
                matrix.popPose();
            }
        }
        if (transporter instanceof DiversionTransporter diversionTransporter) {
            Player player = Minecraft.getInstance().player;
            //Player shouldn't be null here, but validate it
            ItemStack itemStack = player == null ? ItemStack.EMPTY : player.getMainHandItem();
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemConfigurator) {
                BlockHitResult rayTraceResult = MekanismUtils.rayTrace(player);
                if (rayTraceResult.getType() != Type.MISS && rayTraceResult.getBlockPos().equals(pos)) {
                    Direction side = tile.getSideLookingAt(player, rayTraceResult.getDirection());
                    matrix.pushPose();
                    matrix.scale(0.5F, 0.5F, 0.5F);
                    matrix.translate(0.5, 0.5, 0.5);
                    RenderResizableCuboid.renderCube(getOverlayModel(diversionTransporter, side), matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()), DIVERSION_OVERLAY_ARGB, LightTexture.FULL_BRIGHT, overlayLight, FaceDisplay.FRONT, getCamera(), null);
                    matrix.popPose();
                }
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.LOGISTICAL_TRANSPORTER;
    }

    @Override
    protected boolean shouldRenderTransmitter(TileEntityLogisticalTransporterBase tile, Vec3 camera) {
        //Render the transmitter if we normally should (opaque status) or we are a diversion transporter as we will need to render the overlay
        return super.shouldRenderTransmitter(tile, camera) || tile instanceof TileEntityDiversionTransporter;
    }

    /**
     * Shrink the in transit list as much as possible. Don't try to render things of the same type that are in the same spot with the same color, ignoring stack size
     */
    private Collection<TransporterStack> getReducedTransit(Collection<TransporterStack> inTransit) {
        Collection<TransporterStack> reducedTransit = new ArrayList<>();
        Set<TransportInformation> information = new ObjectOpenHashSet<>();
        for (TransporterStack stack : inTransit) {
            if (stack != null && !stack.itemStack.isEmpty() && information.add(new TransportInformation(stack))) {
                //Ensure the stack is valid AND we did not already have information matching the stack
                //We use add to check if it already contained the value, so that we only have to query the set once
                reducedTransit.add(stack);
            }
        }
        return reducedTransit;
    }

    private Model3D getOverlayModel(DiversionTransporter transporter, Direction side) {
        //Get the model or set it up if needed
        Model3D model = cachedOverlays.computeIfAbsent(side, face -> {
            Model3D model3D = new Model3D().prepSingleFaceModelSize(face);
            for (Direction direction : EnumUtils.DIRECTIONS) {
                model3D.setSideRender(direction, direction == face);
            }
            return model3D;
        });
        //set the proper side to the texture we need to use
        return model.setTexture(side, switch (transporter.modes[side.ordinal()]) {
            case DISABLED -> gunpowderIcon;
            case HIGH -> torchOnIcon;
            case LOW -> torchOffIcon;
        });
    }

    private static class TransportInformation {

        @Nullable
        private final EnumColor color;
        private final HashedItem item;
        private final int progress;

        private TransportInformation(TransporterStack transporterStack) {
            this.progress = transporterStack.progress;
            this.color = transporterStack.color;
            this.item = HashedItem.create(transporterStack.itemStack);
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + progress;
            code = 31 * code + item.hashCode();
            if (color != null) {
                code = 31 * code + color.hashCode();
            }
            return code;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj instanceof TransportInformation other && progress == other.progress && color == other.color && item.equals(other.item);
        }
    }

    private static class LazyItemRenderer {

        @Nullable
        private ItemEntity entityItem;
        @Nullable
        private EntityRenderer<? super ItemEntity> renderer;

        public void init(Level world, BlockPos pos) {
            if (entityItem == null) {
                entityItem = new ItemEntity(EntityType.ITEM, world);
            } else {
                entityItem.setLevel(world);
            }
            entityItem.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            //Reset entity age to fix issues with mods like ItemPhysic
            entityItem.age = 0;
        }

        private void renderAsStack(PoseStack matrix, MultiBufferSource buffer, ItemStack stack, int light) {
            if (entityItem != null) {
                if (renderer == null) {
                    renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entityItem);
                }
                entityItem.setItem(stack);
                renderer.render(entityItem, 0, 0, matrix, buffer, light);
            }
        }
    }
}