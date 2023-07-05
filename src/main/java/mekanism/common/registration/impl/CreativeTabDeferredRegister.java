package mekanism.common.registration.impl;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.ILangEntry;
import mekanism.client.SpecialColors;
import mekanism.common.block.BlockBounding;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class CreativeTabDeferredRegister extends WrappedDeferredRegister<CreativeModeTab> {

    private final Consumer<BuildCreativeModeTabContentsEvent> addToExistingTabs;
    private final String modid;

    public CreativeTabDeferredRegister(String modid) {
        this(modid, event -> {
        });
    }

    public CreativeTabDeferredRegister(String modid, Consumer<BuildCreativeModeTabContentsEvent> addToExistingTabs) {
        super(modid, Registries.CREATIVE_MODE_TAB);
        this.modid = modid;
        this.addToExistingTabs = addToExistingTabs;
    }

    @Override
    public void register(IEventBus bus) {
        super.register(bus);
        bus.addListener(addToExistingTabs);
    }

    /**
     * @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
     */
    public CreativeTabRegistryObject registerMain(ILangEntry title, IItemProvider icon, UnaryOperator<CreativeModeTab.Builder> operator) {
        return register(modid, title, icon, operator);
    }

    /**
     * @apiNote We manually require the title and icon to be passed so that we ensure all tabs have one.
     */
    public CreativeTabRegistryObject register(String name, ILangEntry title, IItemProvider icon, UnaryOperator<CreativeModeTab.Builder> operator) {
        return register(name, () -> {
            CreativeModeTab.Builder builder = CreativeModeTab.builder()
                  .title(title.translate())
                  .icon(icon::getItemStack)
                  .withTabFactory(MekanismCreativeTab::new);
            return operator.apply(builder).build();
        }, CreativeTabRegistryObject::new);
    }

    public static void addToDisplay(CreativeModeTab.Output output, ItemLike... items) {
        for (ItemLike item : items) {
            addToDisplay(output, item);
        }
    }

    public static void addToDisplay(CreativeModeTab.Output output, ItemLike itemLike) {
        if (itemLike.asItem() instanceof ICustomCreativeTabContents contents) {
            if (contents.addDefault()) {
                output.accept(itemLike);
            }
            contents.addItems(output);
        } else {
            output.accept(itemLike);
        }
    }

    public static void addToDisplay(ItemDeferredRegister register, CreativeModeTab.Output output) {
        for (IItemProvider itemProvider : register.getAllItems()) {
            addToDisplay(output, itemProvider);
        }
    }

    public static void addToDisplay(BlockDeferredRegister register, CreativeModeTab.Output output) {
        for (IBlockProvider itemProvider : register.getAllBlocks()) {
            //Don't add bounding blocks to the creative tab
            if (!(itemProvider.getBlock() instanceof BlockBounding)) {
                addToDisplay(output, itemProvider);
            }
        }
    }

    public static void addToDisplay(FluidDeferredRegister register, CreativeModeTab.Output output) {
        for (FluidRegistryObject<?, ?, ?, ?, ?> fluidRO : register.getAllFluids()) {
            addToDisplay(output, fluidRO.getBucket());
        }
    }

    public interface ICustomCreativeTabContents {

        void addItems(CreativeModeTab.Output tabOutput);

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