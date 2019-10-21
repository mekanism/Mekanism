package mekanism.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class RenderConfigurableMachine<S extends TileEntity & ISideConfiguration> extends TileEntityRenderer<S> {

    private Minecraft minecraft = Minecraft.getInstance();

    private Map<Direction, Map<TransmissionType, DisplayInteger>> cachedOverlays = new EnumMap<>(Direction.class);

    public RenderConfigurableMachine() {
        rendererDispatcher = TileEntityRendererDispatcher.instance;
    }

    @Override
    public void render(S configurable, double x, double y, double z, float partialTick, int destroyStage) {
        ItemStack itemStack = minecraft.player.inventory.getCurrentItem();
        Item item = itemStack.getItem();
        if (!itemStack.isEmpty() && item instanceof ItemConfigurator && ((ItemConfigurator) item).getState(itemStack).isConfigurating()) {
            //TODO: Properly figure out which one the player is looking at
            BlockRayTraceResult pos = null;//minecraft.player.rayTrace(8.0D, 1.0F);
            if (pos != null) {
                BlockPos bp = pos.getPos();
                TransmissionType type = Objects.requireNonNull(((ItemConfigurator) item).getState(itemStack).getTransmission(), "Configurating state requires transmission type");
                if (configurable.getConfig().supports(type)) {
                    if (bp.equals(configurable.getPos())) {
                        DataType dataType = configurable.getConfig().getDataType(type, pos.getFace());
                        if (dataType != null) {
                            GlStateManager.pushMatrix();
                            GlStateManager.enableCull();
                            GlStateManager.disableLighting();
                            GlowInfo glowInfo = MekanismRenderer.enableGlow();
                            GlStateManager.shadeModel(GL11.GL_SMOOTH);
                            GlStateManager.disableAlphaTest();
                            GlStateManager.enableBlend();
                            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

                            MekanismRenderer.color(dataType.getColor(), 0.6F);
                            bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
                            GlStateManager.translatef((float) x, (float) y, (float) z);
                            int display = getOverlayDisplay(pos.getFace(), type).display;
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
            }
        }
    }

    private DisplayInteger getOverlayDisplay(Direction side, TransmissionType type) {
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
        GlStateManager.endList();

        return display;
    }
}