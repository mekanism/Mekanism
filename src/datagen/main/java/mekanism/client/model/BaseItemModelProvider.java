package mekanism.client.model;

import mekanism.common.item.ItemModule;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.ItemModelGenerators.TrimModelData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.NotNull;

public abstract class BaseItemModelProvider extends ItemModelProvider {

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

    protected ResourceLocation itemTexture(ItemLike itemLike) {
        return modLoc("item/" + RegistryUtils.getPath(itemLike.asItem()));
    }

    protected void registerGenerated(ItemLike... itemProviders) {
        for (ItemLike itemLike : itemProviders) {
            generated(itemLike);
        }
    }

    protected void registerModules(ItemDeferredRegister register) {
        for (Holder<Item> itemProvider : register.getEntries()) {
            if (itemProvider.value() instanceof ItemModule module) {
                generated(module);
            }
        }
    }

    protected void registerBuckets(FluidDeferredRegister register) {
        for (Holder<Item> holder : register.getBucketEntries()) {
            //Note: We expect this to always be the case
            if (holder.value() instanceof BucketItem bucket) {
                withExistingParent(RegistryUtils.getPath(bucket), ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "item/bucket"))
                      .customLoader(DynamicFluidContainerModelBuilder::begin)
                      .fluid(bucket.content);
            }
        }
    }

    protected ItemModelBuilder generated(ItemLike itemLike) {
        return generated(itemLike, itemTexture(itemLike));
    }

    protected ItemModelBuilder generated(ItemLike itemLike, ResourceLocation texture) {
        return withExistingParent(RegistryUtils.getPath(itemLike.asItem()), "item/generated").texture("layer0", texture);
    }

    protected ItemModelBuilder resource(ItemLike itemLike, String type) {
        //TODO: Try to come up with a better solution to this. Currently we have an empty texture for layer zero so that we can set
        // the tint only on layer one so that we only end up having the tint show for this fallback texture
        ItemModelBuilder modelBuilder = generated(itemLike, modLoc("item/empty")).texture("layer1", modLoc("item/" + type));
        ResourceLocation overlay = modLoc("item/" + type + "_overlay");
        if (textureExists(overlay)) {
            //If we have an overlay type for that resource type then add that as another layer
            modelBuilder = modelBuilder.texture("layer2", overlay);
        }
        return modelBuilder;
    }

    protected void registerHandheld(ItemLike... itemProviders) {
        for (ItemLike itemLike : itemProviders) {
            handheld(itemLike);
        }
    }

    protected ItemModelBuilder handheld(ItemLike itemLike) {
        return handheld(itemLike, itemTexture(itemLike));
    }

    protected ItemModelBuilder handheld(ItemLike itemLike, ResourceLocation texture) {
        return withExistingParent(RegistryUtils.getPath(itemLike.asItem()), "item/handheld").texture("layer0", texture);
    }

    protected ItemModelBuilder armorWithTrim(ArmorItem armorItem, ResourceLocation texture) {
        ItemModelBuilder builder = generated(armorItem, texture);
        for (TrimModelData trimModelData : ItemModelGenerators.GENERATED_TRIM_MODELS) {
            String trimId = trimModelData.name(armorItem.getMaterial());
            ItemModelBuilder override = withExistingParent(builder.getLocation().withSuffix("_" + trimId + "_trim").getPath(), "item/generated")
                  .texture("layer0", texture)
                  .texture("layer1", ResourceLocation.withDefaultNamespace("trims/items/" + armorItem.getType().getName() + "_trim_" + trimId));
            builder.override()
                  .predicate(ItemModelGenerators.TRIM_TYPE_PREDICATE_ID, trimModelData.itemModelIndex())
                  .model(override);
        }
        return builder;
    }
}