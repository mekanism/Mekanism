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
public enum InjectingBookCategory implements MekanismBookCategory {
    MISC("misc");

    private static final IntFunction<InjectingBookCategory> BY_ID = ByIdMap.continuous(InjectingBookCategory::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<InjectingBookCategory> CODEC = StringRepresentable.fromEnum(InjectingBookCategory::values);
    public static final StreamCodec<ByteBuf, InjectingBookCategory> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, InjectingBookCategory::ordinal);
    private final String name;

    InjectingBookCategory(String name) {
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

    /// Recipe book info for chemical injecting recipes.
    ///
    /// @param category Recipe book category.
    /// @param group    Recipe group.
    ///
    /// @see net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo
    public record InjectingBookInfo(InjectingBookCategory category, String group) implements Recipe.BookInfo<InjectingBookCategory> {

        public static final MapCodec<InjectingBookInfo> MAP_CODEC = Recipe.BookInfo.mapCodec(CODEC, MISC, InjectingBookInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, InjectingBookInfo> STREAM_CODEC = Recipe.BookInfo.streamCodec(InjectingBookCategory.STREAM_CODEC, InjectingBookInfo::new);
    }
}