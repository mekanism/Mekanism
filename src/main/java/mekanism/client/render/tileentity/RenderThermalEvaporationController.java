package mekanism.client.render.tileentity;

import java.util.Arrays;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.FluidRenderer;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.DisplayInteger;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderThermalEvaporationController extends TileEntitySpecialRenderer<TileEntityThermalEvaporationController> {

    private static final int LEVELS = 16;
    private static final int ALL_LEVELS = LEVELS + 2;
    private static final int RING_INDEX = ALL_LEVELS - 2;
    private static final int CONCAVE_INDEX = ALL_LEVELS - 1;
    private static FluidRenderMap<DisplayInteger[]> cachedCenterFluids = new FluidRenderMap<>();

    public static void resetDisplayInts() {
        cachedCenterFluids.clear();
    }

    @Override
    public void render(TileEntityThermalEvaporationController tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (tileEntity.structured && tileEntity.inputTank.getFluid() != null) {
            bindTexture(MekanismRenderer.getBlocksTexture());
            if (tileEntity.height - 2 >= 1 && tileEntity.inputTank.getCapacity() > 0) {
                MekanismRenderHelper renderHelper = initHelper();
                FluidRenderer.translateToOrigin(tileEntity.getRenderLocation());
                MekanismRenderer.glowOn(tileEntity.inputTank.getFluid().getFluid().getLuminosity());
                renderHelper.color(tileEntity.inputTank.getFluid());
                DisplayInteger[] displayList = getListAndRender(tileEntity.inputTank.getFluid());

                float levels = Math.min((float) tileEntity.inputTank.getFluidAmount() / tileEntity.inputTank.getCapacity(), 1);
                levels *= tileEntity.height - 2;
                int partialLevels = (int) ((levels - (int) levels) * 16);
                switch (tileEntity.facing) {
                    case SOUTH:
                        GlStateManager.translate(-1, 0, -1);
                        break;
                    case EAST:
                        GlStateManager.translate(-1, 0, 0);
                        break;
                    case WEST:
                        GlStateManager.translate(0, 0, -1);
                        break;
                    default:
                        break;
                }

                GlStateManager.translate(0, 0.01, 0);
                if ((int) levels > 0) {
                    displayList[CONCAVE_INDEX].render();
                    GlStateManager.translate(0, 1, 0);
                    for (int i = 1; i < (int) levels; i++) {
                        displayList[RING_INDEX].render();
                        GlStateManager.translate(0, 1, 0);
                    }
                }
                displayList[partialLevels].render();
                MekanismRenderer.glowOff();
                renderHelper.cleanup();
            }
        }
    }

    private MekanismRenderHelper initHelper() {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).enableCull().enableBlend().disableLighting();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        return renderHelper;
    }

    private DisplayInteger[] getListAndRender(FluidStack fluid) {
        if (cachedCenterFluids.containsKey(fluid)) {
            return cachedCenterFluids.get(fluid);
        }

        DisplayInteger[] displays = new DisplayInteger[ALL_LEVELS];

        Model3D model = new Model3D();
        model.baseBlock = fluid.getFluid().getBlock();
        if (model.baseBlock == null) {
            model.baseBlock = FluidRegistry.WATER.getBlock();
        }
        model.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));

        MekanismRenderHelper renderHelper = new MekanismRenderHelper().color(fluid);
        if (fluid.getFluid().getStill(fluid) == null) {
            DisplayInteger empty = DisplayInteger.createAndStart();
            DisplayInteger.endList();
            Arrays.fill(displays, 0, LEVELS, empty);
        } else {
            model.setSideRender(EnumFacing.DOWN, false);

            for (int i = 0; i < LEVELS; i++) {
                displays[i] = generateLevel(i, model);
            }

            model.setSideRender(EnumFacing.UP, false);
            displays[RING_INDEX] = generateLevel(LEVELS - 1, model);
            model.setSideRender(EnumFacing.DOWN, true);
            displays[CONCAVE_INDEX] = generateLevel(LEVELS - 1, model);
        }
        renderHelper.cleanup();
        cachedCenterFluids.put(fluid, displays);
        return displays;
    }

    private DisplayInteger generateLevel(int height, Model3D model) {
        DisplayInteger displayInteger = DisplayInteger.createAndStart();

        model.minX = 0 + .01;
        model.minY = 0;
        model.minZ = 0 + .01;
        model.maxX = 2 - .01;
        model.maxY = (float) height / (float) (LEVELS - 1) + (height == 0 ? .02 : 0);
        model.maxZ = 2 - .01;

        MekanismRenderer.renderObject(model);
        DisplayInteger.endList();

        return displayInteger;
    }
}