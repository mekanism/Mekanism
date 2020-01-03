package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class RenderLogisticalTransporter extends RenderTransmitterBase<TileEntityLogisticalTransporter> {

    private static Map<Direction, Map<Integer, DisplayInteger>> cachedOverlays = new EnumMap<>(Direction.class);
    private final static ResourceLocation transporterBox = MekanismUtils.getResource(ResourceType.RENDER, "transporter_box.png");
    private static TextureAtlasSprite gunpowderIcon;
    private static TextureAtlasSprite torchOffIcon;
    private static TextureAtlasSprite torchOnIcon;
    private ModelTransporterBox modelBox = new ModelTransporterBox();
    private ItemEntity entityItem = new ItemEntity(EntityType.ITEM, null);

    public RenderLogisticalTransporter(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }
    //TODO: 1.15
    //private EntityRenderer<ItemEntity> renderer = Minecraft.getInstance().getRenderManager().getRenderer(ItemEntity.class);

    public static void onStitch(AtlasTexture map) {
        cachedOverlays.clear();
        //TODO: 1.15, can we move overlaying this onto the diversion transporter into the baked model?
        gunpowderIcon = map.getSprite(new ResourceLocation("minecraft", "item/gunpowder"));
        torchOffIcon = map.getSprite(new ResourceLocation("minecraft", "block/redstone_torch_off"));
        torchOnIcon = map.getSprite(new ResourceLocation("minecraft", "block/redstone_torch_on"));
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityLogisticalTransporter transporter, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer,
          int light, int overlayLight) {
        //TODO: 1.15
        /*if (MekanismConfig.client.opaqueTransmitters.get()) {
            return;
        }
        //Keep track of if we had to push. Makes it so that we don't have to push and pop if we end up doing no rendering
        boolean pushed = false;
        Collection<TransporterStack> inTransit = transporter.getTransmitter().getTransit();
        if (!inTransit.isEmpty()) {
            RenderSystem.pushMatrix();
            pushed = true;

            //TODO: Do we have to make a new entity item each time we render
            entityItem.setNoDespawn();
            //entityItem.hoverStart = 0;
            entityItem.setPosition(transporter.getPos().getX() + 0.5, transporter.getPos().getY() + 0.5, transporter.getPos().getZ() + 0.5);
            entityItem.world = transporter.getWorld();

            float partial = partialTick * transporter.tier.getSpeed();
            Collection<TransporterStack> reducedTransit = getReducedTransit(inTransit);
            for (TransporterStack stack : reducedTransit) {
                entityItem.setItem(stack.itemStack);
                float[] pos = TransporterUtils.getStackPosition(transporter.getTransmitter(), stack, partial);
                float xShifted = (float) x + pos[0];
                float yShifted = (float) y + pos[1];
                float zShifted = (float) z + pos[2];

                RenderSystem.pushMatrix();
                RenderSystem.translatef(xShifted, yShifted, zShifted);
                RenderSystem.scalef(0.75F, 0.75F, 0.75F);
                renderer.doRender(entityItem, 0, 0, 0, 0, 0);
                RenderSystem.popMatrix();

                if (stack.color != null) {
                    bindTexture(transporterBox);
                    RenderSystem.pushMatrix();
                    GlowInfo glowInfo = MekanismRenderer.enableGlow();
                    RenderSystem.disableCull();
                    MekanismRenderer.color(stack.color);
                    RenderSystem.translatef(xShifted, yShifted, zShifted);
                    modelBox.render(0.0625F);
                    MekanismRenderer.resetColor();
                    RenderSystem.enableCull();
                    MekanismRenderer.disableGlow(glowInfo);
                    RenderSystem.popMatrix();
                }
            }
        }

        if (transporter instanceof TileEntityDiversionTransporter) {
            if (!pushed) {
                RenderSystem.pushMatrix();
                pushed = true;
            }
            ItemStack itemStack = minecraft.player.inventory.getCurrentItem();
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemConfigurator) {
                //TODO: Properly figure out which one the player is looking at
                BlockRayTraceResult pos = MekanismUtils.rayTrace(minecraft.player);
                if (!pos.getType().equals(Type.MISS) && pos.getFace() != null && pos.getPos().equals(transporter.getPos())) {
                    int mode = ((TileEntityDiversionTransporter) transporter).modes[pos.getFace().ordinal()];
                    RenderSystem.pushMatrix();
                    RenderSystem.enableCull();
                    RenderSystem.disableLighting();
                    GlowInfo glowInfo = MekanismRenderer.enableGlow();
                    RenderSystem.shadeModel(GL11.GL_SMOOTH);
                    RenderSystem.disableAlphaTest();
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                    RenderSystem.color4f(1, 1, 1, 0.8F);
                    field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
                    RenderSystem.translatef((float) x, (float) y, (float) z);
                    RenderSystem.scalef(0.5F, 0.5F, 0.5F);
                    RenderSystem.translatef(0.5F, 0.5F, 0.5F);

                    int display = getOverlayDisplay(pos.getFace(), mode).display;
                    GlStateManager.callList(display);

                    MekanismRenderer.resetColor();
                    RenderSystem.disableBlend();
                    RenderSystem.enableAlphaTest();
                    MekanismRenderer.disableGlow(glowInfo);
                    RenderSystem.enableLighting();
                    RenderSystem.disableCull();
                    RenderSystem.popMatrix();
                }
            }
        }
        if (pushed) {
            //If we did anything we need to pop the matrix we pushed
            RenderSystem.popMatrix();
        }*/
    }

    /**
     * Shrink the in transit list as much as possible. Don't try to render things of the same type that are in the same spot with the same color, ignoring stack size
     */
    private Collection<TransporterStack> getReducedTransit(Collection<TransporterStack> inTransit) {
        Collection<TransporterStack> reducedTransit = new ArrayList<>();
        Set<TransportInformation> information = new HashSet<>();
        for (TransporterStack stack : inTransit) {
            if (stack != null && !stack.itemStack.isEmpty() && information.add(new TransportInformation(stack))) {
                //Ensure the stack is valid AND we did not already have information matching the stack
                //We use add to check if it already contained the value, so that we only have to query the set once
                reducedTransit.add(stack);
            }
        }
        return reducedTransit;
    }

    //TODO: 1.15
    /*private DisplayInteger getOverlayDisplay(Direction side, int mode) {
        if (cachedOverlays.containsKey(side) && cachedOverlays.get(side).containsKey(mode)) {
            return cachedOverlays.get(side).get(mode);
        }

        TextureAtlasSprite icon = null;
        switch (mode) {
            case 0:
                icon = gunpowderIcon;
                break;
            case 1:
                icon = torchOnIcon;
                break;
            case 2:
                icon = torchOffIcon;
                break;
        }

        Model3D toReturn = new Model3D();
        toReturn.baseBlock = Blocks.STONE;
        toReturn.setTexture(icon);

        DisplayInteger display = DisplayInteger.createAndStart();

        if (cachedOverlays.containsKey(side)) {
            cachedOverlays.get(side).put(mode, display);
        } else {
            Map<Integer, DisplayInteger> map = new HashMap<>();
            map.put(mode, display);
            cachedOverlays.put(side, map);
        }

        switch (side) {
            case DOWN:
                toReturn.minY = -0.01;
                toReturn.maxY = 0;

                toReturn.minX = 0;
                toReturn.minZ = 0;
                toReturn.maxX = 1;
                toReturn.maxZ = 1;
                break;
            case UP:
                toReturn.minY = 1;
                toReturn.maxY = 1.01;

                toReturn.minX = 0;
                toReturn.minZ = 0;
                toReturn.maxX = 1;
                toReturn.maxZ = 1;
                break;
            case NORTH:
                toReturn.minZ = -0.01;
                toReturn.maxZ = 0;

                toReturn.minX = 0;
                toReturn.minY = 0;
                toReturn.maxX = 1;
                toReturn.maxY = 1;
                break;
            case SOUTH:
                toReturn.minZ = 1;
                toReturn.maxZ = 1.01;

                toReturn.minX = 0;
                toReturn.minY = 0;
                toReturn.maxX = 1;
                toReturn.maxY = 1;
                break;
            case WEST:
                toReturn.minX = -0.01;
                toReturn.maxX = 0;

                toReturn.minY = 0;
                toReturn.minZ = 0;
                toReturn.maxY = 1;
                toReturn.maxZ = 1;
                break;
            case EAST:
                toReturn.minX = 1;
                toReturn.maxX = 1.01;

                toReturn.minY = 0;
                toReturn.minZ = 0;
                toReturn.maxY = 1;
                toReturn.maxZ = 1;
                break;
            default:
                break;
        }
        MekanismRenderer.renderObject(toReturn);
        GlStateManager.endList();
        return display;
    }*/

    private static class TransportInformation {

        @Nullable
        private final EnumColor color;
        private final HashedItem item;
        private final int progress;

        private TransportInformation(TransporterStack transporterStack) {
            this.progress = transporterStack.progress;
            this.color = transporterStack.color;
            this.item = new HashedItem(transporterStack.itemStack);
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
            if (obj instanceof TransportInformation) {
                TransportInformation other = (TransportInformation) obj;
                return progress == other.progress && color == other.color && item.equals(other.item);
            }
            return false;
        }
    }
}