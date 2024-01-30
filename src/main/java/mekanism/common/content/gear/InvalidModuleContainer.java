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
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@NothingNullByDefault
final class InvalidModuleContainer extends ModuleContainer {

    static final InvalidModuleContainer INSTANCE = new InvalidModuleContainer();

    private InvalidModuleContainer() {
        super(ItemStack.EMPTY, Map.of(), Map.of());
    }

    @Override
    public void deserializeNBT(CompoundTag modulesTag) {
        //NO-OP
    }

    @Nullable
    @Override
    public <T, C> T getCapabilityFromStack(ItemCapability<T, C> capability, @UnknownNullability C context) {
        return null;
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