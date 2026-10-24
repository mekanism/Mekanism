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
import net.minecraft.references.ItemIds;
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

/// Recipe book categories for nutritional liquification recipes.
///
/// @since 10.8.0
public enum LiquificationBookCategory implements MekanismBookCategory {
    MISC("misc", ItemIds.BREAD, null);

    public static final Codec<LiquificationBookCategory> CODEC = StringRepresentable.fromEnum(LiquificationBookCategory::values);
    public static final IntFunction<LiquificationBookCategory> BY_ID = ByIdMap.continuous(LiquificationBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, LiquificationBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, LiquificationBookCategory::ordinal);
    private static final Holder<Fluid> PASTE = DeferredHolder.create(ResourceKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "nutritional_paste")));

    private final Holder<RecipeBookCategory> categoryHolder;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    LiquificationBookCategory(String name, String primaryMekIcon, @Nullable String secondaryMekIcon) {
        this(name, MekanismBookCategory.createMekanismKey(primaryMekIcon), MekanismBookCategory.createMekanismKey(secondaryMekIcon));
    }

    LiquificationBookCategory(String name, ResourceKey<Item> primaryIcon, @Nullable ResourceKey<Item> secondaryIcon) {
        this.name = name;
        this.primaryIcon = MekanismBookCategory.createItem(primaryIcon);
        this.secondaryIcon = MekanismBookCategory.createItem(secondaryIcon);
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_LIQUIFICATION, this.name);
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
        return new ItemStack(primaryIcon);
    }

    @Override
    public Optional<Object> getSecondaryIcon(Provider holderLookup) {
        if (this == MISC) {
            return Optional.of(FluidResource.of(PASTE));
        }
        return secondaryIcon == null ? Optional.empty() : Optional.of(new ItemStack(secondaryIcon));
    }

    /// Recipe book info for nutritional liquification recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record LiquificationBookInfo(LiquificationBookCategory category, String group) implements Recipe.BookInfo<LiquificationBookCategory> {

        public static final MapCodec<LiquificationBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, LiquificationBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, LiquificationBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(LiquificationBookCategory.STREAM_CODEC, LiquificationBookInfo::new);
    }
}