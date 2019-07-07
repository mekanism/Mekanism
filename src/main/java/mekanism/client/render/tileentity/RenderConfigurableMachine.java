package mekanism.client.render.tileentity;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.GLSMHelper;
import mekanism.client.render.GLSMHelper.GlowInfo;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.SideData;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderConfigurableMachine<S extends TileEntity & ISideConfiguration> extends TileEntitySpecialRenderer<S> {

    private Minecraft mc = FMLClientHandler.instance().getClient();

    private Map<EnumFacing, Map<TransmissionType, DisplayInteger>> cachedOverlays = new EnumMap<>(EnumFacing.class);

    public RenderConfigurableMachine() {
        rendererDispatcher = TileEntityRendererDispatcher.instance;
    }

    @Override
    public void render(S configurable, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        ItemStack itemStack = mc.player.inventory.getCurrentItem();
        Item item = itemStack.getItem();
        if (!itemStack.isEmpty() && item instanceof ItemConfigurator && ((ItemConfigurator) item).getState(itemStack).isConfigurating()) {
            RayTraceResult pos = mc.player.rayTrace(8.0D, 1.0F);
            if (pos != null) {
                BlockPos bp = pos.getBlockPos();
                TransmissionType type = Objects.requireNonNull(((ItemConfigurator) item).getState(itemStack).getTransmission(), "Configurating state requires transmission type");
                if (configurable.getConfig().supports(type)) {
                    if (bp.equals(configurable.getPos())) {
                        SideData data = configurable.getConfig().getOutput(type, pos.sideHit, configurable.getOrientation());
                        if (data != TileComponentConfig.EMPTY) {
                            GlStateManager.pushMatrix();
                            GlStateManager.enableCull();
                            GlStateManager.disableLighting();
                            GlowInfo glowInfo = GLSMHelper.enableGlow();
                            GlStateManager.shadeModel(GL11.GL_SMOOTH);
                            GlStateManager.disableAlpha();
                            GlStateManager.enableBlend();
                            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

                            GLSMHelper.color(data.color, 0.6F);
                            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                            GlStateManager.translate(x, y, z);
                            int display = getOverlayDisplay(pos.sideHit, type).display;
                            GlStateManager.callList(display);
                            GLSMHelper.resetColor();

                            GlStateManager.disableBlend();
                            GlStateManager.enableAlpha();
                            GLSMHelper.disableGlow(glowInfo);
                            GlStateManager.enableLighting();
                            GlStateManager.disableCull();
                            GlStateManager.popMatrix();
                        }
                    }
                }
            }
        }
    }

    private DisplayInteger getOverlayDisplay(EnumFacing side, TransmissionType type) {
        if (cachedOverlays.containsKey(side) && cachedOverlays.get(side).containsKey(type)) {
            return cachedOverlays.get(side).get(type);
        }

        Model3D toReturn = new Model3D();
        toReturn.baseBlock = Blocks.STONE;
        toReturn.setTexture(MekanismRenderer.overlays.get(type));

        DisplayInteger display = DisplayInteger.createAndStart();

        if (cachedOverlays.containsKey(side)) {
            cachedOverlays.get(side).put(type, display);
        } else {
            Map<TransmissionType, DisplayInteger> map = new EnumMap<>(TransmissionType.class);
            map.put(type, display);
            cachedOverlays.put(side, map);
        }

        switch (side) {
            case DOWN:
                toReturn.minY = -.01;
                toReturn.maxY = -.001;

                toReturn.minX = 0;
                toReturn.minZ = 0;
                toReturn.maxX = 1;
                toReturn.maxZ = 1;
                break;
            case UP:
                toReturn.minY = 1.001;
                toReturn.maxY = 1.01;

                toReturn.minX = 0;
                toReturn.minZ = 0;
                toReturn.maxX = 1;
                toReturn.maxZ = 1;
                break;
            case NORTH:
                toReturn.minZ = -.01;
                toReturn.maxZ = -.001;

                toReturn.minX = 0;
                toReturn.minY = 0;
                toReturn.maxX = 1;
                toReturn.maxY = 1;
                break;
            case SOUTH:
                toReturn.minZ = 1.001;
                toReturn.maxZ = 1.01;

                toReturn.minX = 0;
                toReturn.minY = 0;
                toReturn.maxX = 1;
                toReturn.maxY = 1;
                break;
            case WEST:
                toReturn.minX = -.01;
                toReturn.maxX = -.001;

                toReturn.minY = 0;
                toReturn.minZ = 0;
                toReturn.maxY = 1;
                toReturn.maxZ = 1;
                break;
            case EAST:
                toReturn.minX = 1.001;
                toReturn.maxX = 1.01;

                toReturn.minY = 0;
                toReturn.minZ = 0;
                toReturn.maxY = 1;
                toReturn.maxZ = 1;
                break;
        }

        MekanismRenderer.renderObject(toReturn);
        DisplayInteger.endList();

        return display;
    }
}