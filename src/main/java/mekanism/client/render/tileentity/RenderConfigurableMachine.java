package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.common.base.ISideConfiguration;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public class RenderConfigurableMachine<S extends TileEntity & ISideConfiguration> extends TileEntityRenderer<S> {

    private Map<Direction, Map<TransmissionType, DisplayInteger>> cachedOverlays = new EnumMap<>(Direction.class);

    public RenderConfigurableMachine(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void func_225616_a_(@Nonnull S configurable, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        /*ItemStack itemStack = Minecraft.getInstance().player.inventory.getCurrentItem();
        Item item = itemStack.getItem();
        if (!itemStack.isEmpty() && item instanceof ItemConfigurator && ((ItemConfigurator) item).getState(itemStack).isConfigurating()) {
            //TODO: Properly figure out which one the player is looking at
            BlockRayTraceResult pos = null;//minecraft.player.rayTrace(8.0D, 1.0F);
            if (pos != null) {
                BlockPos bp = pos.getPos();
                TransmissionType type = Objects.requireNonNull(((ItemConfigurator) item).getState(itemStack).getTransmission(), "Configurating state requires transmission type");
                if (configurable.getConfig().supports(type)) {
                    if (bp.equals(configurable.getPos())) {
                        DataType dataType = configurable.getConfig().getDataType(type, RelativeSide.fromDirections(configurable.getOrientation(), pos.getFace()));
                        if (dataType != null) {
                            RenderSystem.pushMatrix();
                            RenderSystem.enableCull();
                            RenderSystem.disableLighting();
                            GlowInfo glowInfo = MekanismRenderer.enableGlow();
                            RenderSystem.shadeModel(GL11.GL_SMOOTH);
                            RenderSystem.disableAlphaTest();
                            RenderSystem.enableBlend();
                            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

                            MekanismRenderer.color(dataType.getColor(), 0.6F);
                            field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
                            RenderSystem.translatef((float) x, (float) y, (float) z);
                            int display = getOverlayDisplay(pos.getFace(), type).display;
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
            }
        }*/
    }

    //TODO: 1.15
    /*private DisplayInteger getOverlayDisplay(Direction side, TransmissionType type) {
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
    }*/
}