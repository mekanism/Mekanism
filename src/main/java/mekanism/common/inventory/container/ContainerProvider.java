package mekanism.common.inventory.container;

import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContainerProvider implements MenuProvider {

    private final Component displayName;
    private final MenuConstructor provider;

    public ContainerProvider(ILangEntry translationHelper, MenuConstructor provider) {
        this(translationHelper.translate(), provider);
    }

    public ContainerProvider(Component displayName, MenuConstructor provider) {
        this.displayName = displayName;
        this.provider = provider;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @NotNull Inventory inv, @NotNull Player player) {
        return provider.createMenu(i, inv, player);
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return displayName;
    }
}