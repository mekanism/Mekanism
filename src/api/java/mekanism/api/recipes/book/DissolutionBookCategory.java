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

/// Recipe book categories for chemical dissolution recipes.
///
/// @since 10.8.0
public enum DissolutionBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<DissolutionBookCategory> CODEC = StringRepresentable.fromEnum(DissolutionBookCategory::values);
    public static final IntFunction<DissolutionBookCategory> BY_ID = ByIdMap.continuous(DissolutionBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, DissolutionBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DissolutionBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    DissolutionBookCategory(String name) {
        this.name = name;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_DISSOLUTION, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    /// Recipe book info for chemical dissolution recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record DissolutionBookInfo(DissolutionBookCategory category, String group) implements Recipe.BookInfo<DissolutionBookCategory> {

        public static final MapCodec<DissolutionBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, DissolutionBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, DissolutionBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(DissolutionBookCategory.STREAM_CODEC, DissolutionBookInfo::new);
    }
}