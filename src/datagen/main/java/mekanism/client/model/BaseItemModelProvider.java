package mekanism.client.model;

import mekanism.common.item.ItemModule;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.RegistryUtils;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.NeoForgeVersion;
import org.jetbrains.annotations.NotNull;

public abstract class BaseItemModelProvider extends ModelProvider {

    protected final ResourceManager clientResources;

    protected BaseItemModelProvider(PackOutput output, String modid, ResourceManager clientResources) {
        super(output, modid);
        this.clientResources = clientResources;
    }

    @NotNull
    @Override
    public String getName() {
        return "Item model provider: " + modId;
    }

    protected abstract void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels);

    public boolean textureExists(Identifier texture) {//todo - 26.1: check me
        return clientResources.getResource(texture.withPrefix("textures/").withSuffix(".png")).isPresent();
    }
    
    protected String getPath(Holder<Item> holder) {
        return RegistryUtils.getName(holder, BuiltInRegistries.ITEM).getPath();
    }

    protected Identifier itemTexture(Holder<Item> item) {
        return modLoc("item/" + getPath(item));
    }

    private Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(modId, path);
    }

    @SafeVarargs
    protected final void registerGenerated(Holder<Item>... items) {
        for (Holder<Item> item : items) {
            generated(item);
        }
    }

    protected void registerModules(ItemDeferredRegister register) {
        for (Holder<Item> itemProvider : register.getEntries()) {
            if (itemProvider.value() instanceof ItemModule) {
                generated(itemProvider);
            }
        }
    }

    protected void registerBuckets(FluidDeferredRegister register) {
        for (Holder<Item> holder : register.getBucketEntries()) {
            //Note: We expect this to always be the case
            if (holder.value() instanceof BucketItem bucket) {
                withExistingParent(getPath(holder), Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "item/bucket"))
                      .customLoader(DynamicFluidContainerModelBuilder::begin)
                      .fluid(bucket.content);
            }
        }
    }

    protected ItemModelBuilder generated(Holder<Item> item) {
        return generated(item, itemTexture(item));
    }

    protected ItemModelBuilder generated(Holder<Item> item, Identifier texture) {
        return withExistingParent(getPath(item), "item/generated").texture("layer0", texture);
    }

    protected ItemModelBuilder resource(Holder<Item> item, String type) {
        //TODO: Try to come up with a better solution to this. Currently we have an empty texture for layer zero so that we can set
        // the tint only on layer one so that we only end up having the tint show for this fallback texture
        ItemModelBuilder modelBuilder = generated(item, modLoc("item/empty")).texture("layer1", modLoc("item/" + type));
        Identifier overlay = modLoc("item/" + type + "_overlay");
        if (textureExists(overlay)) {
            //If we have an overlay type for that resource type then add that as another layer
            modelBuilder = modelBuilder.texture("layer2", overlay);
        }
        return modelBuilder;
    }
    

    protected ItemModelBuilder handheld(Holder<Item> item, Identifier texture) {
        return withExistingParent(getPath(item), "item/handheld").texture("layer0", texture);
    }

    protected ItemModelBuilder armorOrHandheld(Holder<Item> holder, Identifier texture) {
        if (holder.value() instanceof ArmorItem armorItem) {
            ItemModelBuilder builder = generated(holder, texture);
            for (TrimModelData trimModelData : ItemModelGenerators.GENERATED_TRIM_MODELS) {
                String trimId = trimModelData.name(armorItem.getMaterial());
                ItemModelBuilder override = withExistingParent(builder.getLocation().withSuffix("_" + trimId + "_trim").getPath(), "item/generated")
                      .texture("layer0", texture)
                      .texture("layer1", Identifier.withDefaultNamespace("trims/items/" + armorItem.getType().getName() + "_trim_" + trimId));
                builder.override()
                      .predicate(ItemModelGenerators.TRIM_TYPE_PREDICATE_ID, trimModelData.itemModelIndex())
                      .model(override);
            }
            return builder;
        }
        return handheld(holder, texture);
    }
}