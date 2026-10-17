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
public enum NucleosynthesizingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<NucleosynthesizingBookCategory> BY_ID = ByIdMap.continuous(NucleosynthesizingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<NucleosynthesizingBookCategory> CODEC = StringRepresentable.fromEnum(NucleosynthesizingBookCategory::values);
    public static final StreamCodec<ByteBuf, NucleosynthesizingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, NucleosynthesizingBookCategory::ordinal);
    private final String name;

    NucleosynthesizingBookCategory(String name) {
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

    /// Recipe book info for nucleosynthesizing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record NucleosynthesizingBookInfo(NucleosynthesizingBookCategory category, String group) implements Recipe.BookInfo<NucleosynthesizingBookCategory> {

        public static final MapCodec<NucleosynthesizingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, NucleosynthesizingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, NucleosynthesizingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(NucleosynthesizingBookCategory.STREAM_CODEC, NucleosynthesizingBookInfo::new);
    }
}