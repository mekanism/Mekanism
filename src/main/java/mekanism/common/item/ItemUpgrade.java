package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.input.Keyboard;

public class ItemUpgrade extends ItemMekanism implements IUpgradeItem {

    private Upgrade upgrade;

    public ItemUpgrade(Upgrade type) {
        //Upgrade names are upgrade_type for purposes of tab complete
        super("upgrade_" + type.getRawName());
        upgrade = type;
        setMaxStackSize(type.getMax());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + "shift" + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails"));
        } else {
            list.addAll(MekanismUtils.splitTooltip(getUpgradeType(itemstack).getDescription(), itemstack));
        }
    }

    @Override
    public Upgrade getUpgradeType(ItemStack stack) {
        return upgrade;
    }

    @Nonnull
    @Override
    public ActionResultType onItemUseFirst(PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, Hand hand) {
        if (player.isSneaking()) {
            TileEntity tile = world.getTileEntity(pos);
            ItemStack stack = player.getHeldItem(hand);
            Upgrade type = getUpgradeType(stack);
            if (tile instanceof IUpgradeTile) {
                TileComponentUpgrade component = ((IUpgradeTile) tile).getComponent();
                if (component.supports(type)) {
                    if (!world.isRemote && component.getUpgrades(type) < type.getMax()) {
                        component.addUpgrade(type);
                        stack.shrink(1);
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }
}