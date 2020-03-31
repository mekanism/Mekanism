package mekanism.common.item;

import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemGeigerCounter extends Item {

    public ItemGeigerCounter(Properties props) {
        super(props.maxStackSize(1));
        addPropertyOverride(new ResourceLocation("radiation"), new IItemPropertyGetter() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public float call(ItemStack stack, @Nullable World world, @Nullable LivingEntity usingEntity) {
                if (usingEntity == null || !(usingEntity instanceof PlayerEntity)) {
                    return 0;
                }
                return Mekanism.radiationManager.getClientScale().ordinal();
            }
        });
    }
}
