package mekanism.common.registration.impl;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
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
import mekanism.common.registration.WrappedForgeDeferredRegister;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;

public class ContainerTypeDeferredRegister extends WrappedForgeDeferredRegister<MenuType<?>> {

    public ContainerTypeDeferredRegister(String modid) {
        super(modid, ForgeRegistries.CONTAINERS);
    }

    public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<MekanismTileContainer<TILE>> register(INamedEntry nameProvider, Class<TILE> tileClass) {
        return register(nameProvider.getInternalRegistryName(), tileClass);
    }

    public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<MekanismTileContainer<TILE>> register(String name, Class<TILE> tileClass) {
        //Temporarily generate this using null as we replace it with a proper value before we actually use this, so it is fine
        ContainerTypeRegistryObject<MekanismTileContainer<TILE>> registryObject = new ContainerTypeRegistryObject<>(null);
        IMekanismContainerFactory<TILE, MekanismTileContainer<TILE>> factory = (id, inv, data) -> new MekanismTileContainer<>(registryObject, id, inv, data);
        return register(name, () -> MekanismContainerType.tile(tileClass, factory), registryObject::setRegistryObject);
    }

    public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<EmptyTileContainer<TILE>> registerEmpty(INamedEntry nameProvider, Class<TILE> tileClass) {
        return registerEmpty(nameProvider.getInternalRegistryName(), tileClass);
    }

    public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<EmptyTileContainer<TILE>> registerEmpty(String name, Class<TILE> tileClass) {
        //Temporarily generate this using null as we replace it with a proper value before we actually use this, so it is fine
        ContainerTypeRegistryObject<EmptyTileContainer<TILE>> registryObject = new ContainerTypeRegistryObject<>(null);
        IMekanismContainerFactory<TILE, EmptyTileContainer<TILE>> factory = (id, inv, data) -> new EmptyTileContainer<>(registryObject, id, inv, data);
        return register(name, () -> MekanismContainerType.tile(tileClass, factory), registryObject::setRegistryObject);
    }

    public <TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> ContainerTypeRegistryObject<CONTAINER> register(INamedEntry nameProvider,
          Class<TILE> tileClass, IMekanismContainerFactory<TILE, CONTAINER> factory) {
        return register(nameProvider.getInternalRegistryName(), tileClass, factory);
    }

    public <TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> ContainerTypeRegistryObject<CONTAINER> register(String name,
          Class<TILE> tileClass, IMekanismContainerFactory<TILE, CONTAINER> factory) {
        return register(name, () -> MekanismContainerType.tile(tileClass, factory));
    }

    public <ENTITY extends Entity, CONTAINER extends AbstractContainerMenu & IEntityContainer<ENTITY>> ContainerTypeRegistryObject<CONTAINER> registerEntity(String name,
          Class<ENTITY> entityClass, IMekanismContainerFactory<ENTITY, CONTAINER> factory) {
        return register(name, () -> MekanismContainerType.entity(entityClass, factory));
    }

    public ContainerTypeRegistryObject<RobitContainer> register(String name) {
        //Temporarily generate this using null as we replace it with a proper value before we actually use this, so it is fine
        ContainerTypeRegistryObject<RobitContainer> registryObject = new ContainerTypeRegistryObject<>(null);
        IMekanismContainerFactory<EntityRobit, RobitContainer> factory = (id, inv, data) -> new RobitContainer(registryObject, id, inv, data);
        return register(name, () -> MekanismContainerType.entity(EntityRobit.class, factory), registryObject::setRegistryObject);
    }

    public <ITEM extends Item, CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(INamedEntry nameProvider, Class<ITEM> itemClass,
          IMekanismItemContainerFactory<ITEM, CONTAINER> factory) {
        return register(nameProvider.getInternalRegistryName(), itemClass, factory);
    }

    public <ITEM extends Item, CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(String name, Class<ITEM> itemClass,
          IMekanismItemContainerFactory<ITEM, CONTAINER> factory) {
        return register(name, () -> MekanismItemContainerType.item(itemClass, factory));
    }

    public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(String name, IContainerFactory<CONTAINER> factory) {
        return register(name, () -> new MenuType<>(factory));
    }

    public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(INamedEntry nameProvider, Supplier<MenuType<CONTAINER>> supplier) {
        return register(nameProvider.getInternalRegistryName(), supplier);
    }

    public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(String name, Supplier<MenuType<CONTAINER>> supplier) {
        return register(name, supplier, ContainerTypeRegistryObject::new);
    }

    public <TILE extends TileEntityMekanism> ContainerBuilder<TILE> custom(INamedEntry nameProvider, Class<TILE> tileClass) {
        return custom(nameProvider.getInternalRegistryName(), tileClass);
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
            ContainerTypeRegistryObject<MekanismTileContainer<TILE>> registryObject = new ContainerTypeRegistryObject<>(null);
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
                protected void addInventorySlots(@Nonnull Inventory inv) {
                    super.addInventorySlots(inv);
                    if (armorSlotsX != -1 && armorSlotsY != -1) {
                        addArmorSlots(inv, armorSlotsX, armorSlotsY, offhandOffset);
                    }
                }
            };
            return register(name, () -> MekanismContainerType.tile(tileClass, factory), registryObject::setRegistryObject);
        }
    }
}