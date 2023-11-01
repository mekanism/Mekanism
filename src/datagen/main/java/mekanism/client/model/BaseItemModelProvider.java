package mekanism.client.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import mekanism.api.providers.IItemProvider;
import mekanism.common.item.ItemModule;
import mekanism.common.lib.FieldReflectionHelper;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.RegistryUtils;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public abstract class BaseItemModelProvider extends ItemModelProvider {

    @SuppressWarnings("rawtypes")
    private final FieldReflectionHelper<ModelBuilder, Map<String, String>> MODEL_TEXTURES = new FieldReflectionHelper<>(ModelBuilder.class, "textures", HashMap::new);
    private static final TrimModelDataHelper<?> TRIM_HELPER = new TrimModelDataHelper<>();

    protected BaseItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @NotNull
    @Override
    public String getName() {
        return "Item model provider: " + modid;
    }

    public boolean textureExists(ResourceLocation texture) {
        return existingFileHelper.exists(texture, PackType.CLIENT_RESOURCES, ".png", "textures");
    }

    protected ResourceLocation itemTexture(IItemProvider itemProvider) {
        return modLoc("item/" + itemProvider.getName());
    }

    protected void registerGenerated(IItemProvider... itemProviders) {
        for (IItemProvider itemProvider : itemProviders) {
            generated(itemProvider);
        }
    }

    protected void registerModules(ItemDeferredRegister register) {
        for (IItemProvider itemProvider : register.getAllItems()) {
            Item item = itemProvider.asItem();
            if (item instanceof ItemModule) {
                generated(itemProvider);
            }
        }
    }

    protected void registerBuckets(FluidDeferredRegister register) {
        for (FluidRegistryObject<?, ?, ?, ?, ?> fluidRegistryObject : register.getAllFluids()) {
            registerBucket(fluidRegistryObject);
        }
    }

    protected ItemModelBuilder generated(IItemProvider itemProvider) {
        return generated(itemProvider, itemTexture(itemProvider));
    }

    protected ItemModelBuilder generated(IItemProvider itemProvider, ResourceLocation texture) {
        return withExistingParent(itemProvider.getName(), "item/generated").texture("layer0", texture);
    }

    protected ItemModelBuilder resource(IItemProvider itemProvider, String type) {
        //TODO: Try to come up with a better solution to this. Currently we have an empty texture for layer zero so that we can set
        // the tint only on layer one so that we only end up having the tint show for this fallback texture
        ItemModelBuilder modelBuilder = generated(itemProvider, modLoc("item/empty")).texture("layer1", modLoc("item/" + type));
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

    protected ItemModelBuilder armorWithTrim(IItemProvider itemProvider, ResourceLocation texture) {
        ItemModelBuilder builder = generated(itemProvider, texture);
        ArmorItem.Type type = ((ArmorItem) itemProvider.asItem()).getType();
        TRIM_HELPER.forEachTrim((trimId, itemModelIndex) -> {
                  ItemModelBuilder override = withExistingParent(builder.getLocation().withSuffix("_" + trimId + "_trim").getPath(), "item/generated")
                        .texture("layer0", texture);
                  //Directly add the layer1 to the texture map as the file doesn't actually exist
                  MODEL_TEXTURES.getValue(override).put("layer1", new ResourceLocation(type.getName() + "_trim_" + trimId).withPrefix("trims/items/").toString());
                  builder.override()
                        .predicate(ItemModelGenerators.TRIM_TYPE_PREDICATE_ID, itemModelIndex)
                        .model(override);
              }
        );
        return builder;
    }

    //Note: This isn't the best way to do this in terms of model file validation, but it works
    protected void registerBucket(FluidRegistryObject<?, ?, ?, ?, ?> fluidRO) {
        withExistingParent(RegistryUtils.getPath(fluidRO.getBucket()), new ResourceLocation("forge", "item/bucket"))
              .customLoader(DynamicFluidContainerModelBuilder::begin)
              .fluid(fluidRO.getStillFluid());
    }

    private static class TrimModelDataHelper<TMD_CLASS> {

        private final FieldReflectionHelper<ItemModelGenerators, List<TMD_CLASS>> generatedTrimModels = new FieldReflectionHelper<>(ItemModelGenerators.class, "f_265952_", Collections::emptyList);
        private final FieldReflectionHelper<TMD_CLASS, String> name;
        private final FieldReflectionHelper<TMD_CLASS, Float> itemModelIndex;

        public TrimModelDataHelper() {
            Class<TMD_CLASS> tmdClass;
            try {
                tmdClass = (Class<TMD_CLASS>) Class.forName("net.minecraft.data.models.ItemModelGenerators$TrimModelData");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            name = new FieldReflectionHelper<>(tmdClass, "f_265890_", () -> null);
            itemModelIndex = new FieldReflectionHelper<>(tmdClass, "f_265849_", () -> null);
        }

        public void forEachTrim(BiConsumer<String, Float> consumer) {
            List<TMD_CLASS> trims = generatedTrimModels.getValue(null);
            for (TMD_CLASS trim : trims) {
                String trimName = name.getValue(trim);
                Float modelIndex = itemModelIndex.getValue(trim);
                consumer.accept(trimName, modelIndex);
            }
        }
    }
}