package mekanism.common.content.gear;

import java.util.Map;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@NothingNullByDefault
final class InvalidModuleContainer extends ModuleContainer {

    static final InvalidModuleContainer INSTANCE = new InvalidModuleContainer();

    private InvalidModuleContainer() {
        super(ItemStack.EMPTY, Map.of());
    }

    @Override
    public void deserializeNBT(CompoundTag modulesTag) {
        //NO-OP
    }

    @Override
    public boolean isContainerOnCooldown(Player player) {
        //Always return true because as far as the modules are concerned the stack backing an invalid container is on cooldown
        return true;
    }

    @Override
    public boolean isInstance(Class<?> clazz) {
        return false;
    }

    @Override
    public Set<ModuleData<?>> supportedTypes() {
        return Set.of();
    }

    @Override
    public void removeModule(IModuleDataProvider<?> typeProvider) {
        //NO-OP
    }

    @Override
    public void addModule(IModuleDataProvider<?> typeProvider) {
        //NO-OP
    }

    @Override
    public boolean isCompatible(IModuleContainer other) {
        return other == this;
    }
}