package mekanism.client.render.transmitter;

import java.util.HashMap;
import mekanism.api.Coord4D;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig.client;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderLogisticalTransporter extends RenderTransmitterBase<TileEntityLogisticalTransporter> {

    private static HashMap<EnumFacing, HashMap<Integer, DisplayInteger>> cachedOverlays = new HashMap<>();
    private static TextureAtlasSprite gunpowderIcon;
    private static TextureAtlasSprite torchOffIcon;
    private static TextureAtlasSprite torchOnIcon;
    private ModelTransporterBox modelBox = new ModelTransporterBox();
    private EntityItem entityItem = new EntityItem(null);
    private Render<Entity> renderer = Minecraft.getMinecraft().getRenderManager()
          .getEntityClassRenderObject(EntityItem.class);

    public RenderLogisticalTransporter() {
        super();
    }

    public static void onStitch(TextureMap map) {
        cachedOverlays.clear();

        gunpowderIcon = map.getTextureExtry("minecraft:items/gunpowder");
        torchOffIcon = map.getTextureExtry("minecraft:blocks/redstone_torch_off");
        torchOnIcon = map.getTextureExtry("minecraft:blocks/redstone_torch_on");
    }

    @Override
    public void render(TileEntityLogisticalTransporter transporter, double x, double y, double z, float partialTick,
          int destroyStage, float alpha) {
        if (client.opaqueTransmitters) {
            return;
        }

        GL11.glPushMatrix();

        entityItem.setNoDespawn();
        entityItem.hoverStart = 0;

        entityItem.setPosition(transporter.getPos().getX() + 0.5, transporter.getPos().getY() + 0.5,
              transporter.getPos().getZ() + 0.5);
        entityItem.world = transporter.getWorld();

        for (TransporterStack stack : transporter.getTransmitter().getTransit()) {
            if (stack != null) {
                GL11.glPushMatrix();
                entityItem.setItem(stack.itemStack);

                float[] pos = TransporterUtils
                      .getStackPosition(transporter.getTransmitter(), stack, partialTick * transporter.tier.speed);

                GL11.glTranslated(x + pos[0], y + pos[1], z + pos[2]);
                GL11.glScalef(0.75F, 0.75F, 0.75F);

                renderer.doRender(entityItem, 0, 0, 0, 0, 0);
                GL11.glPopMatrix();

                if (stack.color != null) {
                    bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "TransporterBox.png"));
                    GL11.glPushMatrix();
                    MekanismRenderer.glowOn();
                    GL11.glDisable(GL11.GL_CULL_FACE);
                    GL11.glColor4f(stack.color.getColor(0), stack.color.getColor(1), stack.color.getColor(2), 1.0F);
                    GL11.glTranslatef((float) (x + pos[0]), (float) (y + pos[1]), (float) (z + pos[2]));
                    modelBox.render(0.0625F);
                    MekanismRenderer.glowOff();
                    GL11.glPopMatrix();
                }
            }
        }

        if (transporter instanceof TileEntityDiversionTransporter) {
            EntityPlayer player = mc.player;
            World world = mc.player.world;
            ItemStack itemStack = player.inventory.getCurrentItem();
            RayTraceResult pos = player.rayTrace(8.0D, 1.0F);

            if (pos != null && !itemStack.isEmpty() && itemStack.getItem() instanceof ItemConfigurator) {
                Coord4D obj = new Coord4D(pos.getBlockPos(), transporter.getWorld());

                if (obj.equals(new Coord4D(transporter.getPos(), transporter.getWorld()))) {
                    int mode = ((TileEntityDiversionTransporter) transporter).modes[pos.sideHit.ordinal()];

                    pushTransporter();

                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.8F);

                    bindTexture(MekanismRenderer.getBlocksTexture());
                    GL11.glTranslatef((float) x, (float) y, (float) z);
                    GL11.glScalef(0.5F, 0.5F, 0.5F);
                    GL11.glTranslatef(0.5F, 0.5F, 0.5F);

                    int display = getOverlayDisplay(world, pos.sideHit, mode).display;
                    GL11.glCallList(display);

                    popTransporter();
                }
            }
        }

        GL11.glPopMatrix();
    }

    private DisplayInteger getOverlayDisplay(World world, EnumFacing side, int mode) {
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
            HashMap<Integer, DisplayInteger> map = new HashMap<>();
            map.put(mode, display);
            cachedOverlays.put(side, map);
        }

        switch (side) {
            case DOWN: {
                toReturn.minY = -0.01;
                toReturn.maxY = 0;

                toReturn.minX = 0;
                toReturn.minZ = 0;
                toReturn.maxX = 1;
                toReturn.maxZ = 1;
                break;
            }
            case UP: {
                toReturn.minY = 1;
                toReturn.maxY = 1.01;

                toReturn.minX = 0;
                toReturn.minZ = 0;
                toReturn.maxX = 1;
                toReturn.maxZ = 1;
                break;
            }
            case NORTH: {
                toReturn.minZ = -0.01;
                toReturn.maxZ = 0;

                toReturn.minX = 0;
                toReturn.minY = 0;
                toReturn.maxX = 1;
                toReturn.maxY = 1;
                break;
            }
            case SOUTH: {
                toReturn.minZ = 1;
                toReturn.maxZ = 1.01;

                toReturn.minX = 0;
                toReturn.minY = 0;
                toReturn.maxX = 1;
                toReturn.maxY = 1;
                break;
            }
            case WEST: {
                toReturn.minX = -0.01;
                toReturn.maxX = 0;

                toReturn.minY = 0;
                toReturn.minZ = 0;
                toReturn.maxY = 1;
                toReturn.maxZ = 1;
                break;
            }
            case EAST: {
                toReturn.minX = 1;
                toReturn.maxX = 1.01;

                toReturn.minY = 0;
                toReturn.minZ = 0;
                toReturn.maxY = 1;
                toReturn.maxZ = 1;
                break;
            }
            default: {
                break;
            }
        }

        MekanismRenderer.renderObject(toReturn);
        DisplayInteger.endList();

        return display;
    }

    private void popTransporter() {
        GL11.glPopAttrib();
        MekanismRenderer.glowOff();
        MekanismRenderer.blendOff();
        GL11.glPopMatrix();
    }

    private void pushTransporter() {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        MekanismRenderer.glowOn();
        MekanismRenderer.blendOn();
    }
}
