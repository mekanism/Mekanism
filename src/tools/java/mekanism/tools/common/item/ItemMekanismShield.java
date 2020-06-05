package mekanism.tools.common.item;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMekanismShield extends ShieldItem implements IHasRepairType {

    private final BaseMekanismMaterial material;

    public ItemMekanismShield(BaseMekanismMaterial material, Supplier<Callable<ItemStackTileEntityRenderer>> ister) {
        super(ItemDeferredRegister.getMekBaseProperties().maxDamage(material.getShieldDurability()).setISTER(ister));
        this.material = material;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);//Add the banner type description
        tooltip.add(ToolsLang.HP.translate(stack.getMaxDamage() - stack.getDamage()));
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return material.getRepairMaterial();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return material.getShieldDurability();
    }

    @Override
    public boolean isDamageable() {
        return material.getShieldDurability() > 0;
    }

    @Override
    public boolean getIsRepairable(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
        return getRepairMaterial().test(repair);
    }

    @Override
    public boolean isShield(ItemStack stack, @Nullable LivingEntity entity) {
        //Has to override this because default impl in IForgeItem checks for exact equality with the shield item instead of instanceof
        return true;
    }

    @Override
    public int getItemEnchantability() {
        return material.getEnchantability();
    }
}