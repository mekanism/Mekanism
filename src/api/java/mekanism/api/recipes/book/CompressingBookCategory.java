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

/// Recipe book categories for compressing recipes.
///
/// @since 10.8.0
public enum CompressingBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<CompressingBookCategory> CODEC = StringRepresentable.fromEnum(CompressingBookCategory::values);
    public static final IntFunction<CompressingBookCategory> BY_ID = ByIdMap.continuous(CompressingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, CompressingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CompressingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    CompressingBookCategory(String name) {
        this.name = name;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_COMPRESSING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    /// Recipe book info for compressing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record CompressingBookInfo(CompressingBookCategory category, String group) implements Recipe.BookInfo<CompressingBookCategory> {

        public static final MapCodec<CompressingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, CompressingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, CompressingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(CompressingBookCategory.STREAM_CODEC, CompressingBookInfo::new);
    }
}