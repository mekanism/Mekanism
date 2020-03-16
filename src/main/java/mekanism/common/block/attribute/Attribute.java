package mekanism.common.block.attribute;

import java.util.function.Consumer;
import mekanism.common.block.interfaces.ITypeBlock;
import net.minecraft.block.Block;

public interface Attribute {

    public static boolean has(Block block, Class<? extends Attribute> type) {
        return block instanceof ITypeBlock && ((ITypeBlock<?>) block).getType().has(type);
    }

    public static <T extends Attribute> T get(Block block, Class<T> type) {
        return block instanceof ITypeBlock ? ((ITypeBlock<?>) block).getType().get(type) : null;
    }

    public static boolean has(Block block1, Block block2, Class<? extends Attribute> type) {
        return has(block1, type) && has(block2, type);
    }

    public static <T extends Attribute> void ifHas(Block block, Class<T> type, Consumer<T> run) {
        if (block instanceof ITypeBlock) {
            T attribute = ((ITypeBlock<?>) block).getType().get(type);
            if (attribute != null) {
                run.accept(attribute);
            }
        }
    }
}
