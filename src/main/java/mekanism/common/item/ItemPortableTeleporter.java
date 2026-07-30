package mekanism.common.item;

import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.security.OwnerObject;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.FrequencyTypes;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.lib.security.ItemSecurityUtils;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ItemPortableTeleporter extends ItemEnergized implements IFrequencyItem, IGuiItem, ICapabilityAware {

    public ItemPortableTeleporter(Properties properties) {
        super(properties.rarity(Rarity.RARE).stacksTo(1));
    }

    @Override
    public FrequencyType<?> getFrequencyType() {
        return FrequencyTypes.TELEPORTER;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        return ItemSecurityUtils.get().claimOrOpenGui(world, player, hand, getContainerType()::tryOpenGui);
    }

    @Override
    public ContainerTypeRegistryObject<?> getContainerType() {
        return MekanismContainerTypes.PORTABLE_TELEPORTER;
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (_, itemAccess) -> new OwnerObject(itemAccess), this);
    }
}