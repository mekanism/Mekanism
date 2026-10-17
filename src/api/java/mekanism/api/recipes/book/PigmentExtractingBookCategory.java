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

/// Recipe book categories for pigment extracting recipes.
///
/// @since 10.8.0
public enum PigmentExtractingBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<PigmentExtractingBookCategory> CODEC = StringRepresentable.fromEnum(PigmentExtractingBookCategory::values);
    public static final IntFunction<PigmentExtractingBookCategory> BY_ID = ByIdMap.continuous(PigmentExtractingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, PigmentExtractingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, PigmentExtractingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    PigmentExtractingBookCategory(String name) {
        this.name = name;
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