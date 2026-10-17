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
public enum ElectrolysisBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<ElectrolysisBookCategory> BY_ID = ByIdMap.continuous(ElectrolysisBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<ElectrolysisBookCategory> CODEC = StringRepresentable.fromEnum(ElectrolysisBookCategory::values);
    public static final StreamCodec<ByteBuf, ElectrolysisBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ElectrolysisBookCategory::ordinal);
    private final String name;

    ElectrolysisBookCategory(String name) {
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

    /// Recipe book info for electrolysis recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record ElectrolysisBookInfo(ElectrolysisBookCategory category, String group) implements Recipe.BookInfo<ElectrolysisBookCategory> {

        public static final MapCodec<ElectrolysisBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, ElectrolysisBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, ElectrolysisBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(ElectrolysisBookCategory.STREAM_CODEC, ElectrolysisBookInfo::new);
    }
}