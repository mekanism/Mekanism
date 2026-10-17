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
public enum CompressingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<CompressingBookCategory> BY_ID = ByIdMap.continuous(CompressingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<CompressingBookCategory> CODEC = StringRepresentable.fromEnum(CompressingBookCategory::values);
    public static final StreamCodec<ByteBuf, CompressingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, CompressingBookCategory::ordinal);
    private final String name;

    CompressingBookCategory(String name) {
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

    /// Recipe book info for compressing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record CompressingBookInfo(CompressingBookCategory category, String group) implements Recipe.BookInfo<CompressingBookCategory> {

        public static final MapCodec<CompressingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, CompressingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, CompressingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(CompressingBookCategory.STREAM_CODEC, CompressingBookInfo::new);
    }
}