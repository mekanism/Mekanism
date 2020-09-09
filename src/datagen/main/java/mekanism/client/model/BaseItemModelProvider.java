package mekanism.client.model;

import javax.annotation.Nonnull;
import mekanism.api.providers.IItemProvider;
import mekanism.client.model.builder.BucketModelBuilder;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.BucketItem;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class BaseItemModelProvider extends ItemModelProvider {

    protected BaseItemModelProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Item model provider: " + modid;
    }

    public boolean textureExists(ResourceLocation texture) {
        return existingFileHelper.exists(texture, ResourcePackType.CLIENT_RESOURCES, ".png", "textures");
    }

    protected ResourceLocation itemTexture(IItemProvider itemProvider) {
        return modLoc("item/" + itemProvider.getName());
    }

    protected void registerGenerated(IItemProvider... itemProviders) {
        for (IItemProvider itemProvider : itemProviders) {
            generated(itemProvider);
        }
    }

    protected ItemModelBuilder generated(IItemProvider itemProvider) {
        return generated(itemProvider, itemTexture(itemProvider));
    }

    protected ItemModelBuilder generated(IItemProvider itemProvider, ResourceLocation texture) {
        return getBuilder(itemProvider.getName()).parent(new UncheckedModelFile("item/generated")).texture("layer0", texture);
    }

    protected ItemModelBuilder resource(IItemProvider itemProvider, String type) {
        //TODO: Try to come up with a better solution to this. Currently we have an empty texture for layer zero so that we can set
        // the tint only on layer one so that we only end up having the tint show for this fallback texture
        ItemModelBuilder modelBuilder = getBuilder(itemProvider.getName()).parent(new UncheckedModelFile("item/generated"))
              .texture("layer0", modLoc("item/empty"))
              .texture("layer1", modLoc("item/" + type));
        ResourceLocation overlay = modLoc("item/" + type + "_overlay");
        if (textureExists(overlay)) {
            //If we have an overlay type for that resource type then add that as another layer
            modelBuilder = modelBuilder.texture("layer2", overlay);
        }
        return modelBuilder;
    }

    protected void registerHandheld(IItemProvider... itemProviders) {
        for (IItemProvider itemProvider : itemProviders) {
            handheld(itemProvider);
        }
    }

    protected ItemModelBuilder handheld(IItemProvider itemProvider) {
        return handheld(itemProvider, itemTexture(itemProvider));
    }

    protected ItemModelBuilder handheld(IItemProvider itemProvider, ResourceLocation texture) {
        return withExistingParent(itemProvider.getName(), "item/handheld").texture("layer0", texture);
    }

    //Note: This isn't the best way to do this in terms of model file validation, but it works
    protected void registerBucket(FluidRegistryObject<?, ?, ?, ?> fluidRO) {
        BucketItem bucket = fluidRO.getBucket();
        ResourceLocation outputLoc = extendWithFolder(bucket.getRegistryName());
        if (generatedModels.containsKey(outputLoc)) {
            throw new RuntimeException("Model with output loc: '" + outputLoc + "' has already been registered");
        }
        ItemModelBuilder modelBuilder = new BucketModelBuilder(outputLoc, existingFileHelper, fluidRO.getStillFluid().getRegistryName())
              .parent(new UncheckedModelFile(new ResourceLocation("forge", "item/bucket")));
        generatedModels.put(outputLoc, modelBuilder);
    }

    protected ResourceLocation extendWithFolder(ResourceLocation rl) {
        if (rl.getPath().contains("/")) {
            return rl;
        }
        return new ResourceLocation(rl.getNamespace(), folder + "/" + rl.getPath());
    }
}