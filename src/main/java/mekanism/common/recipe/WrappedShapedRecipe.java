package mekanism.common.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

@NothingNullByDefault
public abstract class WrappedShapedRecipe extends ShapedRecipe {

    public static <CLAZZ extends WrappedShapedRecipe> RecipeSerializer<CLAZZ> serializer(Factory<CLAZZ> constructor) {
        MapCodec<CLAZZ> mapCodec = RecordCodecBuilder.mapCodec(
              i -> i.group(
                          CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
                          CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
                          ShapedRecipePattern.MAP_CODEC.forGetter(o -> o.pattern),
                          ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result)
                    )
                    .apply(i, constructor::get)
        );
        StreamCodec<RegistryFriendlyByteBuf, CLAZZ> streamCodec = StreamCodec.composite(
              CommonInfo.STREAM_CODEC,
              o -> o.commonInfo,
              CraftingBookInfo.STREAM_CODEC,
              o -> o.bookInfo,
              ShapedRecipePattern.STREAM_CODEC,
              o -> o.pattern,
              ItemStackTemplate.STREAM_CODEC,
              o -> o.result,
              constructor::get
        );
        return new RecipeSerializer<>(mapCodec, streamCodec);
    }

    protected final ItemStackTemplate result;

    protected WrappedShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        //Note: We do not override the matches method if it matches ignoring NBT,
        // to ensure that we return the proper value for if there is a match that gives a proper output
        return super.matches(input, world) && !assemble(input).isEmpty();//TODO - 26.1: can this ever be empty?
    }

    public interface Factory<CLAZZ extends WrappedShapedRecipe> {

        CLAZZ get(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result);
    }
}