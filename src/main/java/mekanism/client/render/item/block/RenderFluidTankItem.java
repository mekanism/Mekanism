package mekanism.client.render.item.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.client.model.ModelFluidTank;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class RenderFluidTankItem extends MekanismItemStackRenderer {

    public static ItemLayerWrapper model;

    private static ModelFluidTank modelFluidTank = new ModelFluidTank();
    private static FluidRenderMap<Int2ObjectMap<Model3D>> cachedCenterFluids = new FluidRenderMap<>();
    private static int stages = 1400;

    public static void resetCachedModels() {
        cachedCenterFluids.clear();
    }

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        Optional<IFluidHandlerItem> capability = MekanismUtils.toOptional(FluidUtil.getFluidHandler(stack));
        if (!capability.isPresent()) {
            //If something went wrong and we don't have a fluid handler on our tank, then fail
            return;
        }
        IFluidHandlerItem fluidHandlerItem = capability.get();
        if (!(fluidHandlerItem instanceof IMekanismFluidHandler)) {
            //TODO: Decide if we want to support someone replacing our fluid handler with another?
            //If it isn't one of our fluid handlers fail
            return;
        }
        IExtendedFluidTank fluidTank = ((IMekanismFluidHandler) fluidHandlerItem).getFluidTank(0, null);
        if (fluidTank == null) {
            //If something went wrong and we don't have a fluid tank fail
            return;
        }
        ItemBlockFluidTank itemFluidTank = (ItemBlockFluidTank) stack.getItem();
        FluidTankTier tier = itemFluidTank.getTier();
        if (tier == null) {
            return;
        }
        if (!fluidTank.isEmpty()) {
            float fluidScale = (float) fluidTank.getFluidAmount() / fluidTank.getCapacity();
            if (fluidScale > 0) {
                FluidStack fluid = fluidTank.getFluid();
                matrix.push();
                matrix.translate(-0.5, -0.5, -0.5);
                int modelNumber;
                int color;
                if (fluid.getFluid().getAttributes().isGaseous(fluid)) {
                    modelNumber = stages - 1;
                    color = MekanismRenderer.getColorARGB(fluid, fluidScale);
                } else {
                    modelNumber = Math.min(stages - 1, (int) (fluidScale * ((float) stages - 1)));
                    color = MekanismRenderer.getColorARGB(fluid);
                }
                MekanismRenderer.renderObject(getFluidModel(fluid, modelNumber), matrix, renderer.getBuffer(MekanismRenderType.resizableCuboid()), color,
                      MekanismRenderer.calculateGlowLight(light, fluid));
                matrix.pop();
            }
        }
        matrix.push();
        matrix.translate(0, -0.9, 0);
        matrix.scale(0.9F, 0.8F, 0.9F);
        //Scale to to size of item
        matrix.scale(1.168F, 1.168F, 1.168F);
        //Shift the fluid slightly so that is visible with the min amount in
        matrix.translate(0, -0.06, 0);
        modelFluidTank.render(matrix, renderer, light, overlayLight, tier);
        matrix.pop();
    }

    private Model3D getFluidModel(@Nonnull FluidStack fluid, int stage) {
        if (cachedCenterFluids.containsKey(fluid) && cachedCenterFluids.get(fluid).containsKey(stage)) {
            return cachedCenterFluids.get(fluid).get(stage);
        }
        Model3D model = new Model3D();
        model.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));
        if (fluid.getFluid().getAttributes().getStillTexture(fluid) != null) {
            model.minX = 0.125 + .01;
            model.minY = 0.0625 + .01;
            model.minZ = 0.125 + .01;

            model.maxX = 0.875 - .01;
            model.maxY = 0.0625 + ((float) stage / (float) stages) * 0.875 - .01;
            model.maxZ = 0.875 - .01;
        }
        if (cachedCenterFluids.containsKey(fluid)) {
            cachedCenterFluids.get(fluid).put(stage, model);
        } else {
            Int2ObjectMap<Model3D> map = new Int2ObjectOpenHashMap<>();
            map.put(stage, model);
            cachedCenterFluids.put(fluid, map);
        }
        return model;
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}