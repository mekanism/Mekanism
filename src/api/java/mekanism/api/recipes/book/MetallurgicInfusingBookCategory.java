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

/// Recipe book categories for metallurgic infusing recipes.
///
/// @since 10.8.0
public enum MetallurgicInfusingBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<MetallurgicInfusingBookCategory> CODEC = StringRepresentable.fromEnum(MetallurgicInfusingBookCategory::values);
    public static final IntFunction<MetallurgicInfusingBookCategory> BY_ID = ByIdMap.continuous(MetallurgicInfusingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, MetallurgicInfusingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, MetallurgicInfusingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    MetallurgicInfusingBookCategory(String name) {
        this.name = name;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_METALLURGIC_INFUSING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    /// Recipe book info for metallurgic infusing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record MetallurgicInfusingBookInfo(MetallurgicInfusingBookCategory category, String group) implements Recipe.BookInfo<MetallurgicInfusingBookCategory> {

        public static final MapCodec<MetallurgicInfusingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, MetallurgicInfusingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, MetallurgicInfusingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(MetallurgicInfusingBookCategory.STREAM_CODEC, MetallurgicInfusingBookInfo::new);
    }
}