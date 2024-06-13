package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.inventory.container.entity.robit.RobitContainer;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.inventory.container.type.MekanismContainerType.IMekanismContainerFactory;
import mekanism.common.inventory.container.type.MekanismItemContainerType;
import mekanism.common.inventory.container.type.MekanismItemContainerType.IMekanismItemContainerFactory;
import mekanism.common.registration.INamedEntry;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.IContainerFactory;
import org.jetbrains.annotations.NotNull;

public class ContainerTypeDeferredRegister extends MekanismDeferredRegister<MenuType<?>> {

    public ContainerTypeDeferredRegister(String modid) {
        super(Registries.MENU, modid, ContainerTypeRegistryObject::new);
    }

    private <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> createHolder(String name) {
        return new ContainerTypeRegistryObject<>(ResourceLocation.fromNamespaceAndPath(getNamespace(), name));
    }

    public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<MekanismTileContainer<TILE>> register(INamedEntry nameProvider, Class<TILE> tileClass) {
        return register(nameProvider.getName(), tileClass);
    }

    public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<MekanismTileContainer<TILE>> register(String name, Class<TILE> tileClass) {
        ContainerTypeRegistryObject<MekanismTileContainer<TILE>> registryObject = createHolder(name);
        IMekanismContainerFactory<TILE, MekanismTileContainer<TILE>> factory = (id, inv, data) -> new MekanismTileContainer<>(registryObject, id, inv, data);
        register(name, () -> MekanismContainerType.tile(tileClass, factory));
        return registryObject;
    }

    public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<EmptyTileContainer<TILE>> registerEmpty(INamedEntry nameProvider, Class<TILE> tileClass) {
        return registerEmpty(nameProvider.getName(), tileClass);
    }

    public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<EmptyTileContainer<TILE>> registerEmpty(String name, Class<TILE> tileClass) {
        ContainerTypeRegistryObject<EmptyTileContainer<TILE>> registryObject = createHolder(name);
        IMekanismContainerFactory<TILE, EmptyTileContainer<TILE>> factory = (id, inv, data) -> new EmptyTileContainer<>(registryObject, id, inv, data);
        register(name, () -> MekanismContainerType.tile(tileClass, factory));
        return registryObject;
    }

    public <TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> ContainerTypeRegistryObject<CONTAINER> register(INamedEntry nameProvider,
          Class<TILE> tileClass, IMekanismContainerFactory<TILE, CONTAINER> factory) {
        return register(nameProvider.getName(), tileClass, factory);
    }

    public <TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> ContainerTypeRegistryObject<CONTAINER> register(String name,
          Class<TILE> tileClass, IMekanismContainerFactory<TILE, CONTAINER> factory) {
        return registerMenu(name, () -> MekanismContainerType.tile(tileClass, factory));
    }

    public <ENTITY extends Entity, CONTAINER extends AbstractContainerMenu & IEntityContainer<ENTITY>> ContainerTypeRegistryObject<CONTAINER> registerEntity(String name,
          Class<ENTITY> entityClass, IMekanismContainerFactory<ENTITY, CONTAINER> factory) {
        return registerMenu(name, () -> MekanismContainerType.entity(entityClass, factory));
    }

    public ContainerTypeRegistryObject<RobitContainer> register(String name) {
        ContainerTypeRegistryObject<RobitContainer> registryObject = createHolder(name);
        IMekanismContainerFactory<EntityRobit, RobitContainer> factory = (id, inv, data) -> new RobitContainer(registryObject, id, inv, data);
        register(name, () -> MekanismContainerType.entity(EntityRobit.class, factory));
        return registryObject;
    }

    public <ITEM extends Item, CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(INamedEntry nameProvider, Class<ITEM> itemClass,
          IMekanismItemContainerFactory<ITEM, CONTAINER> factory) {
        return register(nameProvider.getName(), itemClass, factory);
    }

    public <ITEM extends Item, CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(String name, Class<ITEM> itemClass,
          IMekanismItemContainerFactory<ITEM, CONTAINER> factory) {
        return registerMenu(name, () -> MekanismItemContainerType.item(itemClass, factory));
    }

    public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(String name, MenuSupplier<CONTAINER> factory) {
        return registerMenu(name, () -> new MenuType<>(factory, FeatureFlags.VANILLA_SET));
    }

    public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(String name, IContainerFactory<CONTAINER> factory) {
        return register(name, (MenuSupplier<CONTAINER>) factory);
    }

    public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(INamedEntry nameProvider, Supplier<MenuType<CONTAINER>> supplier) {
        return registerMenu(nameProvider.getName(), supplier);
    }

    public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> registerMenu(String name, Supplier<MenuType<CONTAINER>> supplier) {
        return (ContainerTypeRegistryObject<CONTAINER>) super.register(name, supplier);
    }

    public <TILE extends TileEntityMekanism> ContainerBuilder<TILE> custom(INamedEntry nameProvider, Class<TILE> tileClass) {
        return custom(nameProvider.getName(), tileClass);
    }

    public <TILE extends TileEntityMekanism> ContainerBuilder<TILE> custom(String name, Class<TILE> tileClass) {
        return new ContainerBuilder<>(name, tileClass);
    }

    public class ContainerBuilder<TILE extends TileEntityMekanism> {

        private final String name;
        private final Class<TILE> tileClass;
        private int offsetX, offsetY;
        private int armorSlotsX = -1, armorSlotsY = -1, offhandOffset = -1;

        private ContainerBuilder(String name, Class<TILE> tileClass) {
            this.name = name;
            this.tileClass = tileClass;
        }

        public ContainerBuilder<TILE> offset(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            return this;
        }

        public ContainerBuilder<TILE> armorSideBar() {
            return armorSideBar(-20, 67, 0);
        }

        public ContainerBuilder<TILE> armorSideBar(int armorSlotsX, int armorSlotsY) {
            return armorSideBar(armorSlotsX, armorSlotsY, -1);
        }

        public ContainerBuilder<TILE> armorSideBar(int armorSlotsX, int armorSlotsY, int offhandOffset) {
            this.armorSlotsX = armorSlotsX;
            this.armorSlotsY = armorSlotsY;
            this.offhandOffset = offhandOffset;
            return this;
        }

        public ContainerTypeRegistryObject<MekanismTileContainer<TILE>> build() {
            ContainerTypeRegistryObject<MekanismTileContainer<TILE>> registryObject = createHolder(name);
            IMekanismContainerFactory<TILE, MekanismTileContainer<TILE>> factory = (id, inv, data) -> new MekanismTileContainer<>(registryObject, id, inv, data) {
                @Override
                protected int getInventoryXOffset() {
                    return super.getInventoryXOffset() + offsetX;
                }

                @Override
                protected int getInventoryYOffset() {
                    return super.getInventoryYOffset() + offsetY;
                }

                @Override
                protected void addInventorySlots(@NotNull Inventory inv) {
                    super.addInventorySlots(inv);
                    if (armorSlotsX != -1 && armorSlotsY != -1) {
                        addArmorSlots(inv, armorSlotsX, armorSlotsY, offhandOffset);
                    }
                }
            };
            register(name, () -> MekanismContainerType.tile(tileClass, factory));
            return registryObject;
        }
    }
}