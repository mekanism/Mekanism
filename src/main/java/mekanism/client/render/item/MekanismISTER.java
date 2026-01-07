package mekanism.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

//TODO - 1.21.11: Figure out if this should be a no data special model renderer or what data we want to be including
public abstract class MekanismISTER implements NoDataSpecialModelRenderer {

    protected MekanismISTER() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    protected EntityModelSet getEntityModels() {
        //Just have this method as a helper for what we pass as entity models rather than bothering to
        // use an AT to access it directly
        return Minecraft.getInstance().getEntityModels();
    }

    protected BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
        //Just have this method as a helper for what we pass as the block entity render dispatcher
        // rather than bothering to use an AT to access it directly
        return Minecraft.getInstance().getBlockEntityRenderDispatcher();
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {

    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight) {

    }

    /**
     * @implNote Heavily based on/from vanilla's ItemRenderer#render code that calls the renderByItem method on the ISBER
     */
    protected void renderBlockItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight, ModelData modelData) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        Block block = blockItem.getBlock();
        boolean fabulous;
        if (displayContext != ItemDisplayContext.GUI && !displayContext.firstPerson()) {
            fabulous = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
        } else {
            fabulous = true;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        BlockState defaultState = block.defaultBlockState();
        //TODO: See if we can come up with a better way to handle getting the model, maybe even one that supports non block items??
        BakedModel baseModel = minecraft.getModelManager().getBlockModelShaper().getBlockModel(defaultState);
        long seed = 42;
        RandomSource random = RandomSource.create();
        boolean hasEffect = stack.hasFoil();
        for (BakedModel model : baseModel.getRenderPasses(stack, fabulous)) {
            for (RenderType renderType : model.getRenderTypes(stack, fabulous)) {
                VertexConsumer buffer = ItemRenderer.getFoilBuffer(renderer, renderType, true, hasEffect);
                //Note: Manually call the render quads lists rather than using renderModelLists so that we can pass the proper render type and model data
                for (Direction direction : EnumUtils.DIRECTIONS) {
                    random.setSeed(seed);
                    itemRenderer.renderQuadList(matrix, buffer, model.getQuads(defaultState, direction, random, modelData, renderType), stack, light, overlayLight);
                }
                random.setSeed(seed);
                itemRenderer.renderQuadList(matrix, buffer, model.getQuads(defaultState, null, random, modelData, renderType), stack, light, overlayLight);
            }
        }
    }
}