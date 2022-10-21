package mekanism.client.render.tileentity;

import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.MekanismJavaModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

@NothingNullByDefault
public abstract class ModelTileEntityRenderer<TILE extends BlockEntity, MODEL extends MekanismJavaModel> extends MekanismTileEntityRenderer<TILE> {

    protected final MODEL model;

    protected ModelTileEntityRenderer(BlockEntityRendererProvider.Context context, Function<EntityModelSet, MODEL> modelCreator) {
        super(context);
        this.model = modelCreator.apply(context.getModelSet());
    }
}