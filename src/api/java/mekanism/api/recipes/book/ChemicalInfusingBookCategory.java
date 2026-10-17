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
public enum ChemicalInfusingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<ChemicalInfusingBookCategory> BY_ID = ByIdMap.continuous(ChemicalInfusingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<ChemicalInfusingBookCategory> CODEC = StringRepresentable.fromEnum(ChemicalInfusingBookCategory::values);
    public static final StreamCodec<ByteBuf, ChemicalInfusingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ChemicalInfusingBookCategory::ordinal);
    private final String name;

    ChemicalInfusingBookCategory(String name) {
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

    /// Recipe book info for infusing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record ChemicalInfusingBookInfo(ChemicalInfusingBookCategory category, String group) implements Recipe.BookInfo<ChemicalInfusingBookCategory> {

        public static final MapCodec<ChemicalInfusingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, ChemicalInfusingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalInfusingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(ChemicalInfusingBookCategory.STREAM_CODEC, ChemicalInfusingBookInfo::new);
    }
}