package mekanism.client.model.builder;

import com.google.gson.JsonObject;
import mekanism.common.DataGenJsonConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BucketModelBuilder extends CustomLoaderItemModelBuilder {

    private static final ResourceLocation BUCKET_MODEL_LOADER = new ResourceLocation("forge", "bucket");

    private final ResourceLocation fluid;

    public BucketModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper, ResourceLocation fluid) {
        super(outputLocation, existingFileHelper, BUCKET_MODEL_LOADER);
        this.fluid = fluid;
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = super.toJson();
        root.addProperty(DataGenJsonConstants.FLUID, fluid.toString());
        return root;
    }
}