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

/// Recipe book categories for chemical injecting recipes.
///
/// @since 10.8.0
public enum InjectingBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<InjectingBookCategory> CODEC = StringRepresentable.fromEnum(InjectingBookCategory::values);
    public static final IntFunction<InjectingBookCategory> BY_ID = ByIdMap.continuous(InjectingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, InjectingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, InjectingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    InjectingBookCategory(String name) {
        this.name = name;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_INJECTING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    /// Recipe book info for chemical injecting recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record InjectingBookInfo(InjectingBookCategory category, String group) implements Recipe.BookInfo<InjectingBookCategory> {

        public static final MapCodec<InjectingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, InjectingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, InjectingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(InjectingBookCategory.STREAM_CODEC, InjectingBookInfo::new);
    }
}