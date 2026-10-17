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

/// Recipe book categories for rotary condensentrator recipes.
///
/// @since 10.8.0
public enum RotaryBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<RotaryBookCategory> CODEC = StringRepresentable.fromEnum(RotaryBookCategory::values);
    public static final IntFunction<RotaryBookCategory> BY_ID = ByIdMap.continuous(RotaryBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, RotaryBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, RotaryBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    RotaryBookCategory(String name) {
        this.name = name;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_ROTARY, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    /// Recipe book info for rotary condensentrator recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record RotaryBookInfo(RotaryBookCategory category, String group) implements Recipe.BookInfo<RotaryBookCategory> {

        public static final MapCodec<RotaryBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, RotaryBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, RotaryBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(RotaryBookCategory.STREAM_CODEC, RotaryBookInfo::new);
    }
}