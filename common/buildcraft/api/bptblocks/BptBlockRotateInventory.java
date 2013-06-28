package buildcraft.api.bptblocks;

import net.minecraft.inventory.IInventory;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;

@Deprecated
public class BptBlockRotateInventory extends BptBlockRotateMeta {

	public BptBlockRotateInventory(int blockId, int[] rotations, boolean rotateForward) {
		super(blockId, rotations, rotateForward);

	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		super.buildBlock(slot, context);

		IInventory inv = (IInventory) context.world().getBlockTileEntity(slot.x, slot.y, slot.z);

		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			inv.setInventorySlotContents(i, null);
		}

	}

}
