package mekanism.api.recipes.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/// Recipe book categories for evaporating recipes.
///
/// @since 10.8.0
public enum EvaporatingBookCategory implements MekanismBookCategory {
    MISC("misc", "thermal_evaporation_controller", null);

    public static final Codec<EvaporatingBookCategory> CODEC = StringRepresentable.fromEnum(EvaporatingBookCategory::values);
    public static final IntFunction<EvaporatingBookCategory> BY_ID = ByIdMap.continuous(EvaporatingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, EvaporatingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EvaporatingBookCategory::ordinal);
    private static final Holder<Fluid> BRINE = DeferredHolder.create(ResourceKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "brine")));

    private final Holder<RecipeBookCategory> categoryHolder;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    EvaporatingBookCategory(String name, String primaryMekIcon, @Nullable String secondaryMekIcon) {
        this(name, MekanismBookCategory.createMekanismKey(primaryMekIcon), MekanismBookCategory.createMekanismKey(secondaryMekIcon));
    }

    EvaporatingBookCategory(String name, ResourceKey<Item> primaryIcon, @Nullable ResourceKey<Item> secondaryIcon) {
        this.name = name;
        this.primaryIcon = MekanismBookCategory.createItem(primaryIcon);
        this.secondaryIcon = MekanismBookCategory.createItem(secondaryIcon);
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_EVAPORATING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    @Override
    public Object getPrimaryIcon(Provider holderLookup) {
        if (this == MISC) {
            return FluidResource.of(BRINE);
        }
        return new ItemStack(primaryIcon);
    }

    @Override
    public Optional<Object> getSecondaryIcon(Provider holderLookup) {
        return secondaryIcon == null ? Optional.empty() : Optional.of(new ItemStack(secondaryIcon));
    }

    /// Recipe book info for evaporating recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record EvaporatingBookInfo(EvaporatingBookCategory category, String group) implements Recipe.BookInfo<EvaporatingBookCategory> {

        public static final MapCodec<EvaporatingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, EvaporatingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, EvaporatingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(EvaporatingBookCategory.STREAM_CODEC, EvaporatingBookInfo::new);
    }
}