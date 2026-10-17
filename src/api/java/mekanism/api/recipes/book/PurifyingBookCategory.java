package mekanism.api.recipes.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.recipes.MekanismRecipeTypes;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;

/// Recipe book categories for chemical purification recipes.
///
/// @since 10.8.0
public enum PurifyingBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<PurifyingBookCategory> CODEC = StringRepresentable.fromEnum(PurifyingBookCategory::values);
    public static final IntFunction<PurifyingBookCategory> BY_ID = ByIdMap.continuous(PurifyingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, PurifyingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, PurifyingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    PurifyingBookCategory(String name) {
        this.name = name;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_PURIFYING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    /// Recipe book info for chemical purification recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record PurifyingBookInfo(PurifyingBookCategory category, String group) implements Recipe.BookInfo<PurifyingBookCategory> {

        public static final MapCodec<PurifyingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, PurifyingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, PurifyingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(PurifyingBookCategory.STREAM_CODEC, PurifyingBookInfo::new);
    }
}