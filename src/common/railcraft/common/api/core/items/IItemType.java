package railcraft.common.api.core.items;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.src.ItemStack;

/**
 * This interface is used with several of the functions in IItemTransfer
 * to provide a convenient means of dealing with entire classes of items without
 * having to specify each item individually.
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface IItemType
{
    /**
     * Railcraft adds the following IItemTypes during preInit: FUEL, TRACK, MINECART, BALLAST, FEED
     *
     * Feel free to grab them from here or define your own.
     */
    public static final Map<String, IItemType> types = new HashMap<String, IItemType>();

    public boolean isItemType(ItemStack stack);
}
