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

/// Recipe book categories for chemical washing recipes.
///
/// @since 10.8.0
public enum WashingBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<WashingBookCategory> CODEC = StringRepresentable.fromEnum(WashingBookCategory::values);
    public static final IntFunction<WashingBookCategory> BY_ID = ByIdMap.continuous(WashingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, WashingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, WashingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    WashingBookCategory(String name) {
        this.name = name;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_WASHING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    /// Recipe book info for chemical washing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record WashingBookInfo(WashingBookCategory category, String group) implements Recipe.BookInfo<WashingBookCategory> {

        public static final MapCodec<WashingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, WashingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, WashingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(WashingBookCategory.STREAM_CODEC, WashingBookInfo::new);
    }
}