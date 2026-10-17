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
public enum PaintingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<PaintingBookCategory> BY_ID = ByIdMap.continuous(PaintingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<PaintingBookCategory> CODEC = StringRepresentable.fromEnum(PaintingBookCategory::values);
    public static final StreamCodec<ByteBuf, PaintingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, PaintingBookCategory::ordinal);
    private final String name;

    PaintingBookCategory(String name) {
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

    /// Recipe book info for painting recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record PaintingBookInfo(PaintingBookCategory category, String group) implements Recipe.BookInfo<PaintingBookCategory> {

        public static final MapCodec<PaintingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, PaintingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, PaintingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(PaintingBookCategory.STREAM_CODEC, PaintingBookInfo::new);
    }
}