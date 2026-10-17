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
public enum CombiningBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<CombiningBookCategory> BY_ID = ByIdMap.continuous(CombiningBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<CombiningBookCategory> CODEC = StringRepresentable.fromEnum(CombiningBookCategory::values);
    public static final StreamCodec<ByteBuf, CombiningBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CombiningBookCategory::ordinal);
    private final String name;

    CombiningBookCategory(String name) {
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

    /// Recipe book info for combining recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record CombiningBookInfo(CombiningBookCategory category, String group) implements Recipe.BookInfo<CombiningBookCategory> {

        public static final MapCodec<CombiningBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, CombiningBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, CombiningBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(CombiningBookCategory.STREAM_CODEC, CombiningBookInfo::new);
    }
}