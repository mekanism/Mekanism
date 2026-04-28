package mekanism.client.model;

import java.util.Optional;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.util.RegistryUtils;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class BaseModelProvider extends ModelProvider {

    private static final DynamicFluidContainerModel.Textures DEFAULT_BUCKET = new DynamicFluidContainerModel.Textures(
          Optional.of(new Material(Identifier.parse("minecraft:item/bucket"))),
          Optional.of(new Material(Identifier.parse("minecraft:item/bucket"))),
          Optional.of(new Material(Identifier.parse("neoforge:item/mask/bucket_fluid"))),
          Optional.of(new Material(Identifier.parse("neoforge:item/mask/bucket_fluid_cover")))
    );
    protected final ResourceManager clientResources;

    public BaseModelProvider(PackOutput output, String modId, ResourceManager clientResources) {
        super(output, modId);
        this.clientResources = clientResources;
    }

    public boolean textureExists(Identifier texture) {//todo - 26.1: check me
        return clientResources.getResource(texture.withPrefix("textures/").withSuffix(".png")).isPresent();
    }

    public boolean textureExists(Material material) {
        return textureExists(material.sprite());
    }

    protected String getPath(Holder<Item> holder) {
        return RegistryUtils.getName(holder, BuiltInRegistries.ITEM).getPath();
    }

    protected Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(modId, path);
    }

    protected Identifier itemTexture(Holder<Item> item) {
        return ModelLocationUtils.getModelLocation(item.value());
    }

    protected void registerBuckets(FluidDeferredRegister register, ItemModelGenerators itemModels) {
        for (Holder<Item> holder : register.getBucketEntries()) {
            //Note: We expect this to always be the case
            if (holder.value() instanceof BucketItem bucket) {
                itemModels.itemModelOutput.accept(bucket, new DynamicFluidContainerModel.Unbaked(DEFAULT_BUCKET, bucket.content, true, true, true));
            }
        }
    }

    @Override
    protected abstract void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels);
}
