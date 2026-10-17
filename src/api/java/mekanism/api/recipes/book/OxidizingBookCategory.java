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
public enum OxidizingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<OxidizingBookCategory> BY_ID = ByIdMap.continuous(OxidizingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<OxidizingBookCategory> CODEC = StringRepresentable.fromEnum(OxidizingBookCategory::values);
    public static final StreamCodec<ByteBuf, OxidizingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, OxidizingBookCategory::ordinal);
    private final String name;

    OxidizingBookCategory(String name) {
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

    /// Recipe book info for chemical oxidizing recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record OxidizingBookInfo(OxidizingBookCategory category, String group) implements Recipe.BookInfo<OxidizingBookCategory> {

        public static final MapCodec<OxidizingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, OxidizingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, OxidizingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(OxidizingBookCategory.STREAM_CODEC, OxidizingBookInfo::new);
    }
}