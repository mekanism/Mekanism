package ic2.api.item;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IItemHudInfo {
	/*
    Add Info to Nano- and Quantum-Suit Helm Hud
    for stack

        @Override
        public List<String> getHudInfo(ItemStack stack) {
        List<String> info = new LinkedList<String>();
        info.add("i am a Cool Item");
        info.add("and have Cool info");
        return info;
    }


	 */

	public List<String> getHudInfo(ItemStack stack);
}
