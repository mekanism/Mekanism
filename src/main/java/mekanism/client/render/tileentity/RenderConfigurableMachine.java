package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public class RenderConfigurableMachine<S extends TileEntity & ISideConfiguration> extends TileEntityRenderer<S> {

    private static Map<Direction, Map<TransmissionType, Model3D>> cachedOverlays = new EnumMap<>(Direction.class);

    public static void resetCachedOverlays() {
        cachedOverlays.clear();
    }

    public RenderConfigurableMachine(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(@Nonnull S configurable, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        ItemStack itemStack = Minecraft.getInstance().player.inventory.getCurrentItem();
        Item item = itemStack.getItem();
        if (!itemStack.isEmpty() && item instanceof ItemConfigurator && ((ItemConfigurator) item).getState(itemStack).isConfigurating()) {
            BlockRayTraceResult pos = MekanismUtils.rayTrace(Minecraft.getInstance().player);
            if (!pos.getType().equals(Type.MISS)) {
                BlockPos bp = pos.getPos();
                TransmissionType type = Objects.requireNonNull(((ItemConfigurator) item).getState(itemStack).getTransmission(), "Configurating state requires transmission type");
                if (configurable.getConfig().supports(type)) {
                    if (bp.equals(configurable.getPos())) {
                        DataType dataType = configurable.getConfig().getDataType(type, RelativeSide.fromDirections(configurable.getOrientation(), pos.getFace()));
                        if (dataType != null) {
                            matrix.push();
                            GlowInfo glowInfo = MekanismRenderer.enableGlow();
                            Model3D overlayModel = getOverlayModel(pos.getFace(), type);
                            MekanismRenderer.renderObject(overlayModel, matrix, renderer, MekanismRenderType.configurableMachineState(AtlasTexture.LOCATION_BLOCKS_TEXTURE),
                                  MekanismRenderer.getColorARGB(dataType.getColor(), 0.6F));
                            MekanismRenderer.disableGlow(glowInfo);
                            matrix.pop();
                        }
                    }
                }
            }
        }
    }

    private Model3D getOverlayModel(Direction side, TransmissionType type) {
        if (cachedOverlays.containsKey(side) && cachedOverlays.get(side).containsKey(type)) {
            return cachedOverlays.get(side).get(type);
        }

        Model3D toReturn = new Model3D();
        toReturn.baseBlock = Blocks.STONE;
        toReturn.setTexture(MekanismRenderer.overlays.get(type));

        if (cachedOverlays.containsKey(side)) {
            cachedOverlays.get(side).put(type, toReturn);
        } else {
            Map<TransmissionType, Model3D> map = new EnumMap<>(TransmissionType.class);
            map.put(type, toReturn);
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
        return toReturn;
    }
}