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

/// Recipe book categories for sawing recipes.
///
/// @since 10.8.0
public enum SawingBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<SawingBookCategory> CODEC = StringRepresentable.fromEnum(SawingBookCategory::values);
    public static final IntFunction<SawingBookCategory> BY_ID = ByIdMap.continuous(SawingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, SawingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, SawingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    SawingBookCategory(String name) {
        this.name = name;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_SAWING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    /// Recipe book info for sawing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record SawingBookInfo(SawingBookCategory category, String group) implements Recipe.BookInfo<SawingBookCategory> {

        public static final MapCodec<SawingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, SawingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, SawingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(SawingBookCategory.STREAM_CODEC, SawingBookInfo::new);
    }
}