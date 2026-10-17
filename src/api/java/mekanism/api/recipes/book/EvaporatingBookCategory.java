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
public enum EvaporatingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<EvaporatingBookCategory> BY_ID = ByIdMap.continuous(EvaporatingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<EvaporatingBookCategory> CODEC = StringRepresentable.fromEnum(EvaporatingBookCategory::values);
    public static final StreamCodec<ByteBuf, EvaporatingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EvaporatingBookCategory::ordinal);
    private final String name;

    EvaporatingBookCategory(String name) {
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

    /// Recipe book info for evaporating recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record EvaporatingBookInfo(EvaporatingBookCategory category, String group) implements Recipe.BookInfo<EvaporatingBookCategory> {

        public static final MapCodec<EvaporatingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, EvaporatingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, EvaporatingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(EvaporatingBookCategory.STREAM_CODEC, EvaporatingBookInfo::new);
    }
}