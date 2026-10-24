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
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import org.jspecify.annotations.Nullable;

/// Recipe book categories for pigment extracting recipes.
///
/// @since 10.8.0
public enum PigmentExtractingBookCategory implements MekanismBookCategory {
    DECORATIVE("decorative", "pigment_extracting", null),
    MISC("misc", "dye_base", null);

    public static final Codec<PigmentExtractingBookCategory> CODEC = StringRepresentable.fromEnum(PigmentExtractingBookCategory::values);
    public static final IntFunction<PigmentExtractingBookCategory> BY_ID = ByIdMap.continuous(PigmentExtractingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, PigmentExtractingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, PigmentExtractingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final Holder<Item> primaryIcon;
    @Nullable
    private final Holder<Item> secondaryIcon;
    private final String name;

    PigmentExtractingBookCategory(String name, String primaryMekIcon, @Nullable String secondaryMekIcon) {
        this(name, MekanismBookCategory.createMekanismKey(primaryMekIcon), MekanismBookCategory.createMekanismKey(secondaryMekIcon));
    }

    PigmentExtractingBookCategory(String name, ResourceKey<Item> primaryIcon, @Nullable ResourceKey<Item> secondaryIcon) {
        this.name = name;
        this.primaryIcon = MekanismBookCategory.createItem(primaryIcon);
        this.secondaryIcon = MekanismBookCategory.createItem(secondaryIcon);
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_PIGMENT_EXTRACTING, this.name);
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

    /// Recipe book info for pigment extracting recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record PigmentExtractingBookInfo(PigmentExtractingBookCategory category, String group) implements Recipe.BookInfo<PigmentExtractingBookCategory> {

        public static final MapCodec<PigmentExtractingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, PigmentExtractingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, PigmentExtractingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(PigmentExtractingBookCategory.STREAM_CODEC, PigmentExtractingBookInfo::new);
    }
}