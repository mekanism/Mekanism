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
public enum CrushingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<CrushingBookCategory> BY_ID = ByIdMap.continuous(CrushingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<CrushingBookCategory> CODEC = StringRepresentable.fromEnum(CrushingBookCategory::values);
    public static final StreamCodec<ByteBuf, CrushingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CrushingBookCategory::ordinal);
    private final String name;

    CrushingBookCategory(String name) {
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

    /// Recipe book info for crushing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record CrushingBookInfo(CrushingBookCategory category, String group) implements Recipe.BookInfo<CrushingBookCategory> {

        public static final MapCodec<CrushingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, CrushingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, CrushingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(CrushingBookCategory.STREAM_CODEC, CrushingBookInfo::new);
    }
}