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

/// Recipe book categories for chemical infusing recipes.
///
/// @since 10.8.0
public enum ChemicalInfusingBookCategory implements MekanismBookCategory {
    MISC("misc");

    public static final Codec<ChemicalInfusingBookCategory> CODEC = StringRepresentable.fromEnum(ChemicalInfusingBookCategory::values);
    public static final IntFunction<ChemicalInfusingBookCategory> BY_ID = ByIdMap.continuous(ChemicalInfusingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ChemicalInfusingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ChemicalInfusingBookCategory::ordinal);

    private final Holder<RecipeBookCategory> categoryHolder;
    private final String name;

    ChemicalInfusingBookCategory(String name) {
        this.name = name;
        this.categoryHolder = MekanismBookCategory.createHolder(MekanismRecipeTypes.NAME_CHEMICAL_INFUSING, this.name);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public RecipeBookCategory toRecipeBookCategory() {
        return categoryHolder.value();
    }

    /// Recipe book info for chemical infusing recipes.
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