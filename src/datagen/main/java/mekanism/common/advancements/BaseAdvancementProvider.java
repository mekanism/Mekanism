package mekanism.common.advancements;

import mekanism.api.MekanismAPI;
import mekanism.api.gear.ModuleData;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.FluidUtils;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import javax.annotation.Nonnull;

public class BaseAdvancementProvider extends AdvancementProvider {

    private final String modid;

    public BaseAdvancementProvider(DataGenerator generator, ExistingFileHelper helper, String modid) {
        super(generator, helper);
        this.modid = modid;
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    protected String advancementLocation(String name) {
        return new ResourceLocation(Mekanism.MODID, "mekanism/" + name).toString();
    }

    protected TranslatableComponent title(String name) {
        return advancement(name + ".title");
    }

    protected TranslatableComponent description(String name) {
        return advancement(name + ".description");
    }

    protected MutableComponent obfuscatedDescription(String name) {
        return advancement(name + ".description").setStyle(Style.EMPTY.withObfuscated(true));
    }

    private TranslatableComponent advancement(String name) {
        return new TranslatableComponent("advancements.mekanism." + name);
    }

    protected CriterionTriggerInstance hasItems(ItemLike... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    protected ItemPredicate fullCanteen() {
        return ItemPredicate.Builder.item()
              .of(MekanismItems.CANTEEN)
              .hasNbt(FluidUtils.getFilledVariant(new ItemStack(MekanismItems.CANTEEN.get()), MekanismConfig.gear.canteenMaxStorage.get(), MekanismFluids.NUTRITIONAL_PASTE).getOrCreateTag())
              .build();
    }

    protected CriterionTriggerInstance hasMaxed(ItemRegistryObject<? extends IModuleContainerItem> item) {
        //TODO: Replace this with a custom inventory change trigger and then don't bother bootstrapping IMC
        ItemStack stack = maxedGear(item);
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item()
              .of(stack.getItem())
              .hasNbt(stack.getOrCreateTag())
              .build());
    }

    protected ItemStack maxedGear(ItemRegistryObject<? extends IModuleContainerItem> item) {
        ItemStack stack = item.getItemStack();
        if (stack.getItem() instanceof IModuleContainerItem container) {
            for (ModuleData<?> module : MekanismAPI.getModuleHelper().getSupported(stack)) {
                container.addModule(stack, module);
                ModuleHelper.INSTANCE.load(stack, module).setInstalledCount(module.getMaxStackSize());
            }
        }
        return stack;
    }
}
