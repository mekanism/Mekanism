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

/// Recipe book categories for evaporating recipes.
///
/// @since 10.8.0
public enum EvaporatingBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<EvaporatingBookCategory> CODEC = StringRepresentable.fromEnum(EvaporatingBookCategory::values);
    public static final IntFunction<EvaporatingBookCategory> BY_ID = ByIdMap.continuous(EvaporatingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, EvaporatingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EvaporatingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    EvaporatingBookCategory(String name) {
        this.name = name;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_EVAPORATING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    /// Recipe book info for evaporating recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record EvaporatingBookInfo(EvaporatingBookCategory category, String group) implements Recipe.BookInfo<EvaporatingBookCategory> {

        public static final MapCodec<EvaporatingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, EvaporatingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, EvaporatingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(EvaporatingBookCategory.STREAM_CODEC, EvaporatingBookInfo::new);
    }
}