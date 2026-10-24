package mekanism.api.recipes.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.jspecify.annotations.Nullable;

/// Recipe book categories for nucleosynthesizing recipes.
///
/// @since 10.8.0
public enum NucleosynthesizingBookCategory implements MekanismBookCategory {
    MISC("misc", MekanismBookCategory.createMekanismKey("pellet_antimatter"), ItemIds.HEART_OF_THE_SEA);

    public static final Codec<NucleosynthesizingBookCategory> CODEC = StringRepresentable.fromEnum(NucleosynthesizingBookCategory::values);
    public static final IntFunction<NucleosynthesizingBookCategory> BY_ID = ByIdMap.continuous(NucleosynthesizingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, NucleosynthesizingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, NucleosynthesizingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    NucleosynthesizingBookCategory(String name, String primaryMekIcon, @Nullable String secondaryMekIcon) {
        this(name, MekanismBookCategory.createMekanismKey(primaryMekIcon), MekanismBookCategory.createMekanismKey(secondaryMekIcon));
    }

    NucleosynthesizingBookCategory(String name, ResourceKey<Item> primaryIcon, @Nullable ResourceKey<Item> secondaryIcon) {
        this.name = name;
        this.primaryIcon = MekanismBookCategory.createItem(primaryIcon);
        this.secondaryIcon = MekanismBookCategory.createItem(secondaryIcon);
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_NUCLEOSYNTHESIZING, this.name);
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
        return secondaryIcon == null ? Optional.empty() : Optional.of(new ItemStack(secondaryIcon));
    }

    /// Recipe book info for nucleosynthesizing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record NucleosynthesizingBookInfo(NucleosynthesizingBookCategory category, String group) implements Recipe.BookInfo<NucleosynthesizingBookCategory> {

        public static final MapCodec<NucleosynthesizingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, NucleosynthesizingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, NucleosynthesizingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(NucleosynthesizingBookCategory.STREAM_CODEC, NucleosynthesizingBookInfo::new);
    }
}