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
public enum ChemicalConversionBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<ChemicalConversionBookCategory> BY_ID = ByIdMap.continuous(ChemicalConversionBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<ChemicalConversionBookCategory> CODEC = StringRepresentable.fromEnum(ChemicalConversionBookCategory::values);
    public static final StreamCodec<ByteBuf, ChemicalConversionBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ChemicalConversionBookCategory::ordinal);
    private final String name;

    ChemicalConversionBookCategory(String name) {
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

    /// Recipe book info for chemical conversion recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record ChemicalConversionBookInfo(ChemicalConversionBookCategory category, String group) implements Recipe.BookInfo<ChemicalConversionBookCategory> {

        public static final MapCodec<ChemicalConversionBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, ChemicalConversionBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalConversionBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(ChemicalConversionBookCategory.STREAM_CODEC, ChemicalConversionBookInfo::new);
    }
}