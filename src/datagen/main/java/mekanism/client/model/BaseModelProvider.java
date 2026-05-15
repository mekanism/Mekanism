package mekanism.client.model;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.RegistryUtils;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.client.model.item.DynamicFluidContainerModel;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class BaseModelProvider extends ModelProvider {

    private static final DynamicFluidContainerModel.Textures DEFAULT_BUCKET = new DynamicFluidContainerModel.Textures(
          Optional.of(new Material(Identifier.parse("minecraft:item/bucket"))),
          Optional.of(new Material(Identifier.parse("minecraft:item/bucket"))),
          Optional.of(new Material(Identifier.parse("neoforge:item/mask/bucket_fluid"))),
          Optional.empty()// Neo bug: cover doesn't work. Optional.of(new Material(Identifier.parse("neoforge:item/mask/bucket_fluid_cover")))
    );
    protected final ResourceManager clientResources;
    /// Blocks to ignore for validation of everything generated (i.e. a manual blockstate exists in the normal source set)
    private final Set<ResourceKey<Block>> manuallyGeneratedBlockStates = new HashSet<>();

    public BaseModelProvider(PackOutput output, String modId, ResourceManager clientResources) {
        super(output, modId);
        this.clientResources = clientResources;
    }

    /// Simple BlockState with no props dispatch, with custom model template / custom path
    ///
    /// @param blockModels     the Generator from [#registerModels]
    /// @param block           The block to generate for
    /// @param targetModelPath Where the model will be saved at (inside models/)
    /// @param modelTemplate   the template to use
    /// @param textureMapping  the textures to use
    /// @param itemModel       The item model to use. e.g. <code>ItemModelUtils.plainModel(targetModelPath)</code>
    ///
    /// @return The same Identifier passed as {@param targetModelPath}, for chaining
    @CanIgnoreReturnValue
    protected static Identifier simpleCustomModel(BlockModelGenerators blockModels, Block block, Identifier targetModelPath, ModelTemplate modelTemplate, TextureMapping textureMapping, ItemModel.Unbaked itemModel) {
        modelTemplate.create(targetModelPath, textureMapping, blockModels.modelOutput);
        MultiVariantGenerator variantGenerator = BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(targetModelPath));
        blockModels.blockStateOutput.accept(variantGenerator);
        blockModels.itemModelOutput.accept(block.asItem(), itemModel);
        return targetModelPath;
    }

    protected void simpleISTER(ItemModelGenerators itemModels, Holder<Item> itemRegistryObject, SpecialModelRenderer.Unbaked<?> unbakedRender) {
        simpleISTER(itemModels, itemRegistryObject, unbakedRender, existingModel(itemRegistryObject.value()));
    }

    protected void simpleISTER(ItemModelGenerators itemModels, Holder<Item> itemRegistryObject, SpecialModelRenderer.Unbaked<?> unbakedRender, Identifier modelLoc) {
        ItemModel.Unbaked unbaked = ItemModelUtils.specialModel(modelLoc, unbakedRender);
        itemModels.itemModelOutput.accept(itemRegistryObject.value(), unbaked);
    }

    public boolean textureExists(Identifier texture) {
        return clientResources.getResource(texture.withPrefix("textures/").withSuffix(".png")).isPresent();
    }

    public boolean modelExists(Identifier model) {
        return clientResources.getResource(model.withPrefix("models/").withSuffix(".json")).isPresent();
    }

    public boolean textureExists(Material material) {
        return textureExists(material.sprite());
    }

    protected String getPath(Holder<Item> holder) {
        return RegistryUtils.getName(holder, BuiltInRegistries.ITEM).getPath();
    }

    protected Identifier existingModel(Holder<Item> item) {
        return existingModel(item.value());
    }

    protected Identifier existingModel(Item item) {
        return validateModelExists(defaultModelLoc(item));
    }

    protected static Identifier defaultModelLoc(Item item) {
        return ModelLocationUtils.getModelLocation(item);
    }

    protected Identifier existingModel(BlockRegistryObject<?, ?> block) {
        return existingModel(block.value());
    }

    protected Identifier existingModel(Block block) {
        return validateModelExists(defaultModelLoc(block));
    }

    protected Identifier existingModel(String modLocation) {
        return validateModelExists(modLocation(modLocation));
    }

    protected static Identifier defaultModelLoc(Block block) {
        return ModelLocationUtils.getModelLocation(block);
    }

    protected Identifier validateModelExists(Identifier modelLocation) {
        if (!modelExists(modelLocation)) {
            throw new IllegalStateException("model does not exist: " + modelLocation);
        }
        return modelLocation;
    }

    /**
     * @deprecated use {@link #modLocation(String)} instead
     */
    @Deprecated
    protected Identifier modLoc(String path) {
        return modLocation(path);
    }

    protected Material modTexture(String path) {
        return new Material(modLocation(path));
    }

    protected Identifier itemTexture(Holder<Item> item) {
        return ModelLocationUtils.getModelLocation(item.value());
    }

    protected void registerBuckets(FluidDeferredRegister register, ItemModelGenerators itemModels) {
        for (Holder<Item> holder : register.getBucketEntries()) {
            //Note: We expect this to always be the case
            if (holder.value() instanceof BucketItem bucket) {
                itemModels.itemModelOutput.accept(bucket, new DynamicFluidContainerModel.Unbaked(DEFAULT_BUCKET, bucket.content, true, false, true));
            }
        }
    }

    protected void registerFluidBlockStates(BlockModelGenerators blockModels, FluidDeferredRegister register) {
        for (DeferredHolder<Block, ? extends Block> blockEntry : register.getBlockEntries()) {
            //Note: We expect this to always be the case
            if (blockEntry.value() instanceof LiquidBlock block && block.fluid.getFluidType() instanceof FluidDeferredRegister.MekanismFluidType fluidType) {
                blockModels.createTrivialBlock(block, _ -> new TexturedModel(
                      TextureMapping.particle(
                            new Material(fluidType.stillTexture)
                      ),
                      ModelTemplates.PARTICLE_ONLY
                ));
            }
        }
    }

    protected void registerModules(ItemDeferredRegister register, ItemModelGenerators itemModels) {
        for (Holder<Item> itemProvider : register.getEntries()) {
            if (itemProvider.value() instanceof ItemModule module) {
                itemModels.generateFlatItem(module, ModelTemplates.FLAT_ITEM);
            }
        }
    }

    /// Default flat item, texture same as reg name
    @SafeVarargs
    protected final void registerGenerated(ItemModelGenerators itemModels, Holder<Item>... items) {
        for (Holder<Item> item : items) {
            itemModels.generateFlatItem(item.value(), ModelTemplates.FLAT_ITEM);
        }
    }

    protected void markManualBlockState(BlockRegistryObject<?, ?> registryObject) {
        manuallyGeneratedBlockStates.add(registryObject.getKey());
    }

    protected void plainBlockItemModel(BlockModelGenerators blockModels, BlockRegistryObject<?, ?> registryObject, String name) {
        blockModels.itemModelOutput.accept(
              registryObject.asItem(),
              ItemModelUtils.plainModel(modLocation(name))
        );
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks().filter(holder -> !manuallyGeneratedBlockStates.contains(holder.getKey()));
    }

    @Override
    protected abstract void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels);
}
