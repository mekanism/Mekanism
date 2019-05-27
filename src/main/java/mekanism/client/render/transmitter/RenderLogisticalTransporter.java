package mekanism.client.render.transmitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.EnumColor;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;

public class RenderLogisticalTransporter extends RenderTransmitterBase<TileEntityLogisticalTransporter> {

    private static Map<EnumFacing, Map<Integer, DisplayInteger>> cachedOverlays = new EnumMap<>(EnumFacing.class);
    private final static ResourceLocation transporterBox = MekanismUtils.getResource(ResourceType.RENDER, "TransporterBox.png");
    private static TextureAtlasSprite gunpowderIcon;
    private static TextureAtlasSprite torchOffIcon;
    private static TextureAtlasSprite torchOnIcon;
    private ModelTransporterBox modelBox = new ModelTransporterBox();
    private EntityItem entityItem = new EntityItem(null);
    private Render<EntityItem> renderer = Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(EntityItem.class);

    public static void onStitch(TextureMap map) {
        cachedOverlays.clear();

        gunpowderIcon = map.getTextureExtry("minecraft:items/gunpowder");
        torchOffIcon = map.getTextureExtry("minecraft:blocks/redstone_torch_off");
        torchOnIcon = map.getTextureExtry("minecraft:blocks/redstone_torch_on");
    }

    @Override
    public void render(TileEntityLogisticalTransporter transporter, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
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
            for (TransporterStack stack : getReducedTransit(inTransit)) {
                entityItem.setItem(stack.itemStack);
                float[] pos = TransporterUtils.getStackPosition(transporter.getTransmitter(), stack, partial);
                double xShifted = x + pos[0];
                double yShifted = y + pos[1];
                double zShifted = z + pos[2];

                GlStateManager.pushMatrix();
                GlStateManager.translate(xShifted, yShifted, zShifted);
                GlStateManager.scale(0.75, 0.75, 0.75);
                renderer.doRender(entityItem, 0, 0, 0, 0, 0);
                GlStateManager.popMatrix();

                if (stack.color != null) {
                    bindTexture(transporterBox);
                    GlStateManager.pushMatrix();
                    MekanismRenderer.glowOn();
                    GlStateManager.disableCull();
                    GlStateManager.color(stack.color.getColor(0), stack.color.getColor(1), stack.color.getColor(2));
                    GlStateManager.translate(xShifted, yShifted, zShifted);
                    modelBox.render(0.0625F);
                    MekanismRenderer.glowOff();
                    GlStateManager.popMatrix();
                }
            }
        }

        if (transporter instanceof TileEntityDiversionTransporter) {
            if (!pushed) {
                GlStateManager.pushMatrix();
                pushed = true;
            }
            ItemStack itemStack = mc.player.inventory.getCurrentItem();
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemConfigurator) {
                RayTraceResult pos = mc.player.rayTrace(8.0D, 1.0F);
                if (pos != null && pos.sideHit != null && pos.getBlockPos().equals(transporter.getPos())) {
                    int mode = ((TileEntityDiversionTransporter) transporter).modes[pos.sideHit.ordinal()];
                    pushTransporter();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 0.8F);
                    bindTexture(MekanismRenderer.getBlocksTexture());
                    GlStateManager.translate(x, y, z);
                    GlStateManager.scale(0.5, 0.5, 0.5);
                    GlStateManager.translate(0.5, 0.5, 0.5);

                    int display = getOverlayDisplay(pos.sideHit, mode).display;
                    GlStateManager.callList(display);
                    popTransporter();
                }
            }
        }
        if (pushed) {
            //If we did anything we need to pop the matrix we pushed
            GlStateManager.popMatrix();
        }
    }

    /**
     * Shrink the in transit list as much as possible. Don't try to render things that are in the same spot with the same color
     */
    private Collection<TransporterStack> getReducedTransit(Collection<TransporterStack> inTransit) {
        Collection<TransporterStack> reducedTransit = new ArrayList<>();
        //TODO: Should this check stack type also. Not sure if it really matters.
        Set<Integer> progresses = new HashSet<>();
        Set<EnumColor> colors = EnumSet.noneOf(EnumColor.class);
        for (TransporterStack stack : inTransit) {
            if (stack == null || progresses.contains(stack.progress) || (stack.color != null && colors.contains(stack.color))) {
                continue;
            }
            reducedTransit.add(stack);
            progresses.add(stack.progress);
            if (stack.color != null) {
                colors.add(stack.color);
            }
        }
        return reducedTransit;
    }

    private DisplayInteger getOverlayDisplay(EnumFacing side, int mode) {
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
        DisplayInteger.endList();
        return display;
    }

    private void popTransporter() {
        MekanismRenderer.blendOff();
        MekanismRenderer.glowOff();
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }

    private void pushTransporter() {
        GlStateManager.pushMatrix();
        GlStateManager.enableCull();
        GlStateManager.disableLighting();
        MekanismRenderer.glowOn();
        MekanismRenderer.blendOn();
    }
}