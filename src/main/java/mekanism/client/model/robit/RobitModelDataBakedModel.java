package mekanism.client.model.robit;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.RobitSpriteUploader;
import mekanism.client.model.baked.ModelDataBakedModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

@NothingNullByDefault
public class RobitModelDataBakedModel extends ModelDataBakedModel {

    public RobitModelDataBakedModel(BakedModel original, ModelData data) {
        super(original, data);
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
        //TODO: Handle the original model being layered properly as currently we don't have any way to properly bounce them
        return RobitSpriteUploader.RENDER_TYPES;
    }
}