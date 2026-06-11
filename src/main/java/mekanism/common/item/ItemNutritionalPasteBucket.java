package mekanism.common.item;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.component.PasteBucketConsumption;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.BucketResourceHandler;

public class ItemNutritionalPasteBucket extends BucketItem implements ICapabilityAware {

    public ItemNutritionalPasteBucket(Fluid fluid, Properties properties) {
        super(fluid, properties.component(DataComponents.CONSUMABLE, Consumables.defaultDrink().build())
              .usingConvertsTo(Items.BUCKET)
              .component(MekanismDataComponents.NUTRITIONAL_PASTE_CONSUMPTION, PasteBucketConsumption.INSTANCE)
        );
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (MekanismUtils.isPlayingMode(player) && player.canEat(false) && FluidType.BUCKET_VOLUME / MekanismConfig.general.nutritionalPasteMBPerFood.get() > 0) {
            ItemStack stack = player.getItemInHand(hand);
            Consumable consumable = stack.get(DataComponents.CONSUMABLE);
            if (consumable != null) {
                return consumable.startConsuming(player, stack, hand);
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FLUID.item(), (_, itemAccess) -> new BucketResourceHandler(itemAccess), this);
    }
}
