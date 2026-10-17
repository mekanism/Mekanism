package mekanism.api.recipes.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;

//TODO - 26.3: Docs
public enum ReactingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<ReactingBookCategory> BY_ID = ByIdMap.continuous(ReactingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<ReactingBookCategory> CODEC = StringRepresentable.fromEnum(ReactingBookCategory::values);
    public static final StreamCodec<ByteBuf, ReactingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ReactingBookCategory::ordinal);
    private final String name;

    ReactingBookCategory(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        //TODO - 26.3: Implement this
        return RecipeBookCategories.CRAFTING_MISC;
    }

    /// Recipe book info for pressurized reaction recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record ReactingBookInfo(ReactingBookCategory category, String group) implements Recipe.BookInfo<ReactingBookCategory> {

        public static final MapCodec<ReactingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, ReactingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, ReactingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(ReactingBookCategory.STREAM_CODEC, ReactingBookInfo::new);
    }
}