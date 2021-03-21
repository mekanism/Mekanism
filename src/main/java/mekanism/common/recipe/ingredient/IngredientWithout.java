package mekanism.common.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.common.Mekanism;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ITag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

//Slightly modified version of Tinker's IngredientWithout
// https://github.com/SlimeKnights/TinkersConstruct/blob/1.16/src/main/java/slimeknights/tconstruct/common/IngredientWithout.java
@MethodsReturnNonnullByDefault
public class IngredientWithout extends Ingredient {

    public static final ResourceLocation ID = Mekanism.rl("without");

    //Helper methods for the two most common types we may be creating
    public static IngredientWithout create(ITag<Item> base, IItemProvider without) {
        return new IngredientWithout(Ingredient.of(base), Ingredient.of(without));
    }

    public static IngredientWithout create(ITag<Item> base, ITag<Item> without) {
        return new IngredientWithout(Ingredient.of(base), Ingredient.of(without));
    }

    private final Ingredient base;
    private final Ingredient without;
    private ItemStack[] filteredMatchingStacks;
    private IntList packedMatchingStacks;

    public IngredientWithout(Ingredient base, Ingredient without) {
        super(Stream.empty());
        this.base = base;
        this.without = without;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return base.test(stack) && !without.test(stack);
    }

    @Override
    public ItemStack[] getItems() {
        if (this.filteredMatchingStacks == null) {
            this.filteredMatchingStacks = Arrays.stream(base.getItems())
                  .filter(stack -> !without.test(stack))
                  .toArray(ItemStack[]::new);
        }
        return filteredMatchingStacks;
    }

    @Override
    public boolean isEmpty() {
        return getItems().length == 0;
    }

    @Override
    public boolean isSimple() {
        return base.isSimple() && without.isSimple();
    }

    @Override
    protected void invalidate() {
        super.invalidate();
        this.filteredMatchingStacks = null;
        this.packedMatchingStacks = null;
    }

    @Override
    public IntList getStackingIds() {
        if (this.packedMatchingStacks == null) {
            ItemStack[] matchingStacks = getItems();
            this.packedMatchingStacks = new IntArrayList(matchingStacks.length);
            for (ItemStack stack : matchingStacks) {
                this.packedMatchingStacks.add(RecipeItemHelper.getStackingIndex(stack));
            }
            this.packedMatchingStacks.sort(IntComparators.NATURAL_COMPARATOR);
        }
        return packedMatchingStacks;
    }

    @Override
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", ID.toString());
        json.add("base", base.toJson());
        json.add("without", without.toJson());
        return json;
    }

    @Override
    public IIngredientSerializer<IngredientWithout> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<IngredientWithout> {

        public static final IIngredientSerializer<IngredientWithout> INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public IngredientWithout parse(@Nonnull JsonObject json) {
            if (json.has("base") && json.has("without")) {
                return new IngredientWithout(Ingredient.fromJson(json.getAsJsonObject("base")), Ingredient.fromJson(json.getAsJsonObject("without")));
            }
            throw new JsonParseException("A without ingredient must have both a bse ingredient, and a negation ingredient.");
        }

        @Override
        public IngredientWithout parse(@Nonnull PacketBuffer buffer) {
            Ingredient base = Ingredient.fromNetwork(buffer);
            Ingredient without = Ingredient.fromNetwork(buffer);
            return new IngredientWithout(base, without);
        }

        @Override
        public void write(@Nonnull PacketBuffer buffer, IngredientWithout ingredient) {
            CraftingHelper.write(buffer, ingredient.base);
            CraftingHelper.write(buffer, ingredient.without);
        }
    }
}