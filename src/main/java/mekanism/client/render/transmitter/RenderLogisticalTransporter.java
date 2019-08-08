package mekanism.client.render.transmitter;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.transporter.HashedItem;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.logistical_transporter.TileEntityLogisticalTransporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import org.lwjgl.opengl.GL11;

public class RenderLogisticalTransporter extends RenderTransmitterBase<TileEntityLogisticalTransporter> {

    private static Map<Direction, Map<Integer, DisplayInteger>> cachedOverlays = new EnumMap<>(Direction.class);
    private final static ResourceLocation transporterBox = MekanismUtils.getResource(ResourceType.RENDER, "TransporterBox.png");
    private static TextureAtlasSprite gunpowderIcon;
    private static TextureAtlasSprite torchOffIcon;
    private static TextureAtlasSprite torchOnIcon;
    private ModelTransporterBox modelBox = new ModelTransporterBox();
    private ItemEntity entityItem = new ItemEntity(null);
    private EntityRenderer<ItemEntity> renderer = Minecraft.getInstance().getRenderManager().getEntityClassRenderObject(ItemEntity.class);

    public static void onStitch(AtlasTexture map) {
        cachedOverlays.clear();

        gunpowderIcon = map.getAtlasSprite("minecraft:items/gunpowder");
        torchOffIcon = map.getAtlasSprite("minecraft:blocks/redstone_torch_off");
        torchOnIcon = map.getAtlasSprite("minecraft:blocks/redstone_torch_on");
    }

    @Override
    public void render(TileEntityLogisticalTransporter transporter, double x, double y, double z, float partialTick, int destroyStage) {
        if (MekanismConfig.current().client.opaqueTransmitters.val()) {
            return;
        }
        //Keep track of if we had to push. Makes it so that we don't have to push and pop if we end up doing no rendering
        boolean pushed = false;
        Collection<TransporterStack> inTransit = transporter.getTransmitter().getTransit();
        if (!inTransit.isEmpty()) {
            GlStateManager.pushMatrix();
            pushed = true;

            entityItem.setNoDespawn();
            entityItem.hoverStart = 0;
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

                GlStateManager.pushMatrix();
                GlStateManager.translatef(xShifted, yShifted, zShifted);
                GlStateManager.translatef(0.75F, 0.75F, 0.75F);
                renderer.doRender(entityItem, 0, 0, 0, 0, 0);
                GlStateManager.popMatrix();

                if (stack.color != null) {
                    bindTexture(transporterBox);
                    GlStateManager.pushMatrix();
                    GlowInfo glowInfo = MekanismRenderer.enableGlow();
                    GlStateManager.disableCull();
                    MekanismRenderer.color(stack.color);
                    GlStateManager.translatef(xShifted, yShifted, zShifted);
                    modelBox.render(0.0625F);
                    MekanismRenderer.resetColor();
                    GlStateManager.enableCull();
                    MekanismRenderer.disableGlow(glowInfo);
                    GlStateManager.popMatrix();
                }
            }
        }

        if (transporter instanceof TileEntityDiversionTransporter) {
            if (!pushed) {
                GlStateManager.pushMatrix();
                pushed = true;
            }
            ItemStack itemStack = minecraft.player.inventory.getCurrentItem();
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemConfigurator) {
                BlockRayTraceResult pos = minecraft.player.rayTrace(8.0D, 1.0F);
                if (pos != null && pos.getFace() != null && pos.getPos().equals(transporter.getPos())) {
                    int mode = ((TileEntityDiversionTransporter) transporter).modes[pos.getFace().ordinal()];
                    GlStateManager.pushMatrix();
                    GlStateManager.enableCull();
                    GlStateManager.disableLighting();
                    GlowInfo glowInfo = MekanismRenderer.enableGlow();
                    GlStateManager.shadeModel(GL11.GL_SMOOTH);
                    GlStateManager.disableAlphaTest();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
                    GlStateManager.color4f(1, 1, 1, 0.8F);
                    bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                    GlStateManager.translatef((float) x, (float) y, (float) z);
                    GlStateManager.translatef(0.5F, 0.5F, 0.5F);
                    GlStateManager.translatef(0.5F, 0.5F, 0.5F);

                    int display = getOverlayDisplay(pos.getFace(), mode).display;
                    GlStateManager.callList(display);

                    MekanismRenderer.resetColor();
                    GlStateManager.disableBlend();
                    GlStateManager.enableAlphaTest();
                    MekanismRenderer.disableGlow(glowInfo);
                    GlStateManager.enableLighting();
                    GlStateManager.disableCull();
                    GlStateManager.popMatrix();
                }
            }
        }
        if (pushed) {
            //If we did anything we need to pop the matrix we pushed
            GlStateManager.popMatrix();
        }
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

    private DisplayInteger getOverlayDisplay(Direction side, int mode) {
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
    }

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