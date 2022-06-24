package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.MekanismRenderer.Model3D.SpriteInfo;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
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

@ParametersAreNonnullByDefault
public class RenderLogisticalTransporter extends RenderTransmitterBase<TileEntityLogisticalTransporterBase> {

    private static final Map<Direction, Model3D> cachedOverlays = new EnumMap<>(Direction.class);
    private static SpriteInfo gunpowderIcon;
    private static SpriteInfo torchOffIcon;
    private static SpriteInfo torchOnIcon;
    private final ModelTransporterBox modelBox;
    private final LazyItemRenderer itemRenderer = new LazyItemRenderer();

    public RenderLogisticalTransporter(BlockEntityRendererProvider.Context context) {
        super(context);
        modelBox = new ModelTransporterBox(context.getModelSet());
    }

    public static void onStitch(TextureAtlas map) {
        cachedOverlays.clear();
        gunpowderIcon = new SpriteInfo(map.getSprite(new ResourceLocation("minecraft", "item/gunpowder")), 16);
        torchOffIcon = new SpriteInfo(map.getSprite(new ResourceLocation("minecraft", "block/redstone_torch_off")), 16);
        torchOnIcon = new SpriteInfo(map.getSprite(new ResourceLocation("minecraft", "block/redstone_torch")), 16);
    }

    @Override
    protected void render(TileEntityLogisticalTransporterBase tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        LogisticalTransporterBase transporter = tile.getTransmitter();
        Collection<TransporterStack> inTransit = transporter.getTransit();
        BlockPos pos = tile.getBlockPos();
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
                itemRenderer.renderAsStack(matrix, renderer, stack.itemStack);
                matrix.popPose();
                if (stack.color != null) {
                    modelBox.render(matrix, renderer, MekanismRenderer.FULL_LIGHT, overlayLight, stackPos[0], stackPos[1], stackPos[2], stack.color);
                }
            }
            matrix.popPose();
        }
        if (transporter instanceof DiversionTransporter diversionTransporter) {
            Player player = Minecraft.getInstance().player;
            ItemStack itemStack = player.getInventory().getSelected();
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemConfigurator) {
                BlockHitResult rayTraceResult = MekanismUtils.rayTrace(player);
                if (rayTraceResult.getType() != Type.MISS && rayTraceResult.getBlockPos().equals(pos)) {
                    Direction side = tile.getSideLookingAt(player, rayTraceResult.getDirection());
                    matrix.pushPose();
                    matrix.scale(0.5F, 0.5F, 0.5F);
                    matrix.translate(0.5, 0.5, 0.5);
                    MekanismRenderer.renderObject(getOverlayModel(diversionTransporter, side), matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()),
                          MekanismRenderer.getColorARGB(255, 255, 255, 0.8F), MekanismRenderer.FULL_LIGHT, overlayLight, FaceDisplay.FRONT);
                    matrix.popPose();
                }
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.LOGISTICAL_TRANSPORTER;
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
            Model3D m = new Model3D();
            MekanismRenderer.prepSingleFaceModelSize(m, face);
            for (Direction direction : EnumUtils.DIRECTIONS) {
                m.setSideRender(direction, direction == face);
            }
            return m;
        });
        // and then figure out which texture we need to use
        SpriteInfo icon = switch (transporter.modes[side.ordinal()]) {
            case DISABLED -> gunpowderIcon;
            case HIGH -> torchOnIcon;
            case LOW -> torchOffIcon;
        };
        // and set that proper side to that texture
        model.setTexture(side, icon);
        return model;
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
                entityItem.level = world;
            }
            entityItem.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            //Reset entity age to fix issues with mods like ItemPhysic
            entityItem.age = 0;
        }

        private void renderAsStack(PoseStack matrix, MultiBufferSource buffer, ItemStack stack) {
            if (entityItem != null) {
                if (renderer == null) {
                    renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entityItem);
                }
                entityItem.setItem(stack);
                renderer.render(entityItem, 0, 0, matrix, buffer, MekanismRenderer.FULL_LIGHT);
            }
        }
    }
}