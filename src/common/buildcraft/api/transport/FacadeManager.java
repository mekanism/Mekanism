package buildcraft.api.transport;

import java.lang.reflect.Method;
import net.minecraft.src.ItemStack;

public class FacadeManager
{
    private static Method addFacade;

    public static void addFacade(ItemStack is) {
        try {
            if(addFacade == null) {
                Class facade = Class.forName("buildcraft.transport.ItemFacade");
                addFacade = facade.getMethod("addFacade", ItemStack.class);
            }
            addFacade.invoke(null, is);
        } catch(Exception ex) {
        }
    }
}
