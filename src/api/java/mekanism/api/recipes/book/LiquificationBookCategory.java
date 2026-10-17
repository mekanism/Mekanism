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

/// Recipe book categories for nutritional liquification recipes.
///
/// @since 10.8.0
public enum LiquificationBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<LiquificationBookCategory> CODEC = StringRepresentable.fromEnum(LiquificationBookCategory::values);
    public static final IntFunction<LiquificationBookCategory> BY_ID = ByIdMap.continuous(LiquificationBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, LiquificationBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, LiquificationBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    LiquificationBookCategory(String name) {
        this.name = name;
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