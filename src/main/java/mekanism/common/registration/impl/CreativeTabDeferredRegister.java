package mekanism.common.registration.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class CreativeTabDeferredRegister extends MekanismDeferredRegister<CreativeModeTab> {

    private final Consumer<BuildCreativeModeTabContentsEvent> addToExistingTabs;

    public CreativeTabDeferredRegister(String modid) {
        this(modid, _ -> {
        });
    }

    public CreativeTabDeferredRegister(String modid, Consumer<BuildCreativeModeTabContentsEvent> addToExistingTabs) {
        super(Registries.CREATIVE_MODE_TAB, modid);
        this.addToExistingTabs = addToExistingTabs;
    }

    @Override
    public void register(IEventBus bus) {
        super.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, addToExistingTabs);
    }

    /// @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
    public MekanismDeferredHolder<CreativeModeTab, CreativeModeTab> registerMain(ILangEntry title, Holder<Item> icon, UnaryOperator<CreativeModeTab.Builder> operator) {
        return register(getNamespace(), title, icon, operator);
    }

    /// @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
    public MekanismDeferredHolder<CreativeModeTab, CreativeModeTab> register(String name, ILangEntry title, Holder<Item> icon, UnaryOperator<CreativeModeTab.Builder> operator) {
        return register(name, () -> {
            CreativeModeTab.Builder builder = CreativeModeTab.builder()
                  .title(title.translate())
                  .icon(() -> new ItemStack(icon))
                  .withTabFactory(MekanismCreativeTab::new);
            return operator.apply(builder).build();
        });
    }

    public static void addToDisplay(ItemDisplayParameters displayParameters, CreativeModeTab.Output output, Collection<? extends Holder<Item>> items) {
        for (Holder<Item> itemProvider : items) {
            addToDisplay(displayParameters, output, itemProvider);
        }
    }

    public static void addToDisplay(BuildCreativeModeTabContentsEvent event, BlockRegistryObject<?, ?>... blocks) {
        addToDisplay(event.getParameters(), event, blocks);
    }

    @SuppressWarnings("unchecked")
    public static void addToDisplay(ItemDisplayParameters displayParameters, CreativeModeTab.Output output, BlockRegistryObject<?, ?>... blocks) {
        addToDisplay(displayParameters, output, Arrays.stream(blocks).map(BlockRegistryObject::getItemHolder).toArray(Holder[]::new));
    }

    @SafeVarargs
    public static void addToDisplay(BuildCreativeModeTabContentsEvent event, Holder<Item>... items) {
        addToDisplay(event.getParameters(), event, items);
    }

    @SafeVarargs
    public static void addToDisplay(ItemDisplayParameters displayParameters, CreativeModeTab.Output output, Holder<Item>... items) {
        CreativeModeTab.TabVisibility visibility;
        if (output instanceof BuildCreativeModeTabContentsEvent) {
            //If we are added from the event, only add the item to the parent tab, as we will already be contained in the search tab
            // from when we are adding to our tabs
            visibility = CreativeModeTab.TabVisibility.PARENT_TAB_ONLY;
        } else {
            visibility = CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
        }
        for (Holder<Item> item : items) {
            Item itemLike = item.value();
            if (itemLike instanceof ICustomCreativeTabContents contents) {
                if (contents.addDefault()) {
                    output.accept(itemLike, visibility);
                }
                contents.addItems(displayParameters, item, stack -> output.accept(stack, visibility));
            } else {
                output.accept(itemLike, visibility);
            }
        }
    }

    public static void addToDisplay(ItemDeferredRegister register, BuildCreativeModeTabContentsEvent event) {
        addToDisplay(register, event.getParameters(), event);
    }

    public static void addToDisplay(ItemDeferredRegister register, ItemDisplayParameters displayParameters, CreativeModeTab.Output output) {
        addToDisplay(displayParameters, output, register.getEntries());
    }

    public static void addToDisplay(BlockDeferredRegister register, BuildCreativeModeTabContentsEvent event) {
        addToDisplay(register, event.getParameters(), event);
    }

    public static void addToDisplay(BlockDeferredRegister register, ItemDisplayParameters displayParameters, CreativeModeTab.Output output) {
        addToDisplay(displayParameters, output, register.getSecondaryEntries());
    }

    public static void addToDisplay(FluidDeferredRegister register, BuildCreativeModeTabContentsEvent event) {
        addToDisplay(register, event.getParameters(), event);
    }

    public static void addToDisplay(FluidDeferredRegister register, ItemDisplayParameters displayParameters, CreativeModeTab.Output output) {
        addToDisplay(displayParameters, output, register.getBucketEntries());
    }

    @FunctionalInterface
    public interface ICustomCreativeTabContents {

        void addItems(ItemDisplayParameters displayParameters, Holder<Item> item, Consumer<ItemStack> addToTab);

        default boolean addDefault() {
            return true;
        }
    }

    public static class MekanismCreativeTab extends CreativeModeTab {

        protected MekanismCreativeTab(CreativeModeTab.Builder builder) {
            super(builder);
        }

        @Override
        public int getLabelColor() {
            return SpecialColors.TEXT_TITLE.argb();
        }
    }
}