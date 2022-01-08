package mekanism.client.render.item.block;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelFluidTank;
import mekanism.client.render.FluidRenderMap;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidType;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.ModelRenderer;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.item.MekanismISTER;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class RenderFluidTankItem extends MekanismISTER {

    public static final RenderFluidTankItem RENDERER = new RenderFluidTankItem();
    private static final FluidRenderMap<Int2ObjectMap<Model3D>> cachedCenterFluids = new FluidRenderMap<>();
    private static final int stages = 1_400;

    public static void resetCachedModels() {
        cachedCenterFluids.clear();
    }

    private ModelFluidTank modelFluidTank;

    @Override
    public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
        modelFluidTank = new ModelFluidTank(getEntityModels());
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull TransformType transformType, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer,
          int light, int overlayLight) {
        FluidTankTier tier = ((ItemBlockFluidTank) stack.getItem()).getTier();
        FluidStack fluid = StorageUtils.getStoredFluidFromNBT(stack);
        if (!fluid.isEmpty()) {
            float fluidScale = (float) fluid.getAmount() / tier.getStorage();
            if (fluidScale > 0) {
                int modelNumber = ModelRenderer.getStage(fluid, stages, fluidScale);
                MekanismRenderer.renderObject(getFluidModel(fluid, modelNumber), matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()),
                      MekanismRenderer.getColorARGB(fluid, fluidScale), MekanismRenderer.calculateGlowLight(light, fluid), overlayLight, FaceDisplay.FRONT,
                      transformType != TransformType.GUI);
            }
        }
        matrix.pushPose();
        //TODO: Eventually move more of this to the model json
        matrix.translate(0.5, -0.4, 0.5);
        matrix.scale(0.9F, 0.8F, 0.9F);
        //Scale to the size of item
        matrix.scale(1.168F, 1.168F, 1.168F);
        //Shift the fluid slightly so that is visible with the min amount in
        matrix.translate(0, -0.06, 0);
        modelFluidTank.render(matrix, renderer, light, overlayLight, tier, stack.hasFoil());
        matrix.popPose();
    }

    private Model3D getFluidModel(@Nonnull FluidStack fluid, int stage) {
        if (cachedCenterFluids.containsKey(fluid) && cachedCenterFluids.get(fluid).containsKey(stage)) {
            return cachedCenterFluids.get(fluid).get(stage);
        }
        Model3D model = new Model3D();
        model.setTexture(MekanismRenderer.getFluidTexture(fluid, FluidType.STILL));
        if (fluid.getFluid().getAttributes().getStillTexture(fluid) != null) {
            model.minX = 0.135F;//0.125 + .01;
            model.minY = 0.0725F;//0.0625 + .01;
            model.minZ = 0.135F;//0.125 + .01;

            model.maxX = 0.865F;//0.875 - .01;
            model.maxY = 0.0525F + 0.875F * (stage / (float) stages);//0.0625 - .01 + 0.875 * (stage / (float) stages);
            model.maxZ = 0.865F;//0.875 - .01;
        }
        cachedCenterFluids.computeIfAbsent(fluid, f -> new Int2ObjectOpenHashMap<>()).put(stage, model);
        return model;
    }
}