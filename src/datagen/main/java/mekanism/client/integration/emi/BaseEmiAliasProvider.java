package mekanism.client.integration.emi;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiInitRegistryImpl;
import dev.emi.emi.registry.EmiPluginContainer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.text.IHasTranslationKey;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack;
import mekanism.common.DataGenSerializationConstants;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.lib.collection.HashList;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ItemLike;

@NothingNullByDefault
public abstract class BaseEmiAliasProvider implements DataProvider {

    private static boolean emiSerializersInitialized;

    //TODO: Remove the need for us bootstrapping it manually: https://github.com/emilyploszaj/emi/issues/537
    private static void bootstrapEmi() {
        if (!emiSerializersInitialized) {
            emiSerializersInitialized = true;
            //Bootstrap the initialization stage of emi plugins so that AliasInfo.INGREDIENT_CODEC has the backing
            // EmiIngredientSerializers present for it to wrap
            EmiInitRegistry initRegistry = new EmiInitRegistryImpl();
            for (EmiPluginContainer container : EmiAgnos.getPlugins().stream().sorted(Comparator.comparingInt(container -> container.id().equals("emi") ? 0 : 1)).toList()) {
                container.plugin().initialize(initRegistry);
            }
        }
    }

    private final CompletableFuture<HolderLookup.Provider> registries;
    private final HashList<AliasInfo> data = new HashList<>();
    private final PathProvider pathProvider;
    private final String modid;

    protected BaseEmiAliasProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        this.pathProvider = output.createPathProvider(Target.RESOURCE_PACK, "aliases");
        this.registries = registries;
        this.modid = modid;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput cachedOutput) {
        bootstrapEmi();
        return this.registries.thenCompose(lookupProvider -> {
            addAliases(lookupProvider);
            Path path = pathProvider.json(new ResourceLocation(MekanismHooks.EMI_MOD_ID, modid));
            return DataProvider.saveStable(cachedOutput, lookupProvider, AliasInfo.LIST_CODEC, data.elements(), path);
        });
    }

    protected EmiIngredient ingredient(IFluidProvider fluidProvider) {
        return EmiStack.of(fluidProvider.getFluid(), 1);
    }

    protected EmiIngredient ingredient(IChemicalProvider<?> chemicalProvider) {
        return ChemicalEmiStack.create(chemicalProvider, 1);
    }

    protected abstract void addAliases(HolderLookup.Provider lookupProvider);

    protected void addAlias(IHasTranslationKey alias, ItemLike... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("Expected to have at least one item");
        }
        addAliases(Arrays.stream(items).<EmiIngredient>map(EmiStack::of).toList(), alias);
    }

    protected void addAliases(ItemLike item, IHasTranslationKey... aliases) {
        addAliases(EmiStack.of(item), aliases);
    }

    protected void addAliases(Collection<? extends ItemLike> stacks, IHasTranslationKey... aliases) {
        addAliases(stacks.stream().<EmiIngredient>map(EmiStack::of).toList(), aliases);
    }

    protected void addAliases(EmiIngredient stack, IHasTranslationKey... aliases) {
        addAliases(List.of(stack), aliases);
    }

    protected void addAliases(List<EmiIngredient> stacks, IHasTranslationKey... aliases) {
        if (aliases.length == 0) {
            throw new IllegalArgumentException("Expected to have at least one alias");
        }
        //Sort the translation key aliases so that our datagen output is more stable
        List<String> sortedAliases = Arrays.stream(aliases)
              .map(IHasTranslationKey::getTranslationKey)
              .sorted()
              .toList();
        //TODO - 1.20.5: Is there some global sort, or stack based sort we can apply as well?
        if (!data.add(new AliasInfo(stacks, sortedAliases))) {
            //TODO: Can we improve the validation we have relating to duplicate values/make things more compact?
            // This if statement exists mainly as a simple check against copy-paste errors
            throw new IllegalStateException("Duplicate alias pair added");
        }
    }

    @Override
    public String getName() {
        return "EMI Alias Provider: " + modid;
    }

    private record AliasInfo(List<EmiIngredient> ingredients, List<String> aliases) {

        private static final Codec<EmiIngredient> INGREDIENT_CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
                  JsonElement element = dynamic.convert(JsonOps.INSTANCE).getValue();
                  EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(element);
                  return ingredient.isEmpty() ? DataResult.error(() -> "Empty or invalid ingredient") : DataResult.success(ingredient);
              }, ingredient -> new Dynamic<>(JsonOps.INSTANCE, EmiIngredientSerializer.getSerialized(ingredient))
        );
        private static final Codec<AliasInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
              singleOrListCodec(INGREDIENT_CODEC).fieldOf(DataGenSerializationConstants.STACKS).forGetter(AliasInfo::ingredients),
              singleOrListCodec(ExtraCodecs.NON_EMPTY_STRING).fieldOf(SerializationConstants.TEXT).forGetter(AliasInfo::aliases)
        ).apply(instance, AliasInfo::new));
        private static final Codec<List<AliasInfo>> LIST_CODEC = ExtraCodecs.nonEmptyList(CODEC.listOf()).fieldOf(DataGenSerializationConstants.ALIASES).codec();

        private static <T> Codec<List<T>> singleOrListCodec(Codec<T> codec) {
            return Codec.either(codec, ExtraCodecs.nonEmptyList(codec.listOf())).xmap(
                  either -> either.map(List::of, Function.identity()),
                  list -> list.size() == 1 ? Either.left(list.getFirst()) : Either.right(list)
            );
        }
    }
}