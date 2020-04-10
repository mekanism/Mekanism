package mekanism.api.recipes.outputs;


import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import org.jetbrains.annotations.Contract;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
//TODO: Rename to tag supplier, maybe even generify it so be not only for ItemStacks
public class OreDictSupplier implements Supplier<ItemStack> {

    private final Tag<Item> itemTag;

    public OreDictSupplier(Tag<Item> itemTag) {
        this.itemTag = itemTag;
    }

    @Override
    @Contract("-> new")
    public ItemStack get() {
        //Get the first element
        for (Item item : itemTag.getAllElements()) {
            return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> getPossibleOutputs() {
        return itemTag.getAllElements().stream().map(ItemStack::new).collect(Collectors.toList());
    }
}