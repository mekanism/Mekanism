package mekanism.client.model.builder;

import com.google.gson.JsonObject;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class CustomLoaderItemModelBuilder extends ItemModelBuilder {

    private final ResourceLocation loader;

    public CustomLoaderItemModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper, ResourceLocation loader) {
        super(outputLocation, existingFileHelper);
        this.loader = loader;
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = super.toJson();
        root.addProperty(DataGenJsonConstants.LOADER, loader.toString());
        return root;
    }
}