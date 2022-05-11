package mekanism.common.advancements;

import mekanism.api.MekanismAPI;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.FluidUtils;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;
import javax.annotation.Nonnull;

public class baseAdvancementProvider extends AdvancementProvider {

    private final String modid;

    public baseAdvancementProvider(DataGenerator generator, ExistingFileHelper helper, String modid) {
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

    protected ItemPredicate fullCanteen() {
        return ItemPredicate.Builder.item()
              .of(MekanismItems.CANTEEN)
              .hasNbt(FluidUtils.getFilledVariant(new ItemStack(MekanismItems.CANTEEN.get()), MekanismConfig.gear.canteenMaxStorage.get(), MekanismFluids.NUTRITIONAL_PASTE).getOrCreateTag())
              .build();
    }

    protected ItemPredicate maxedGear(ItemRegistryObject<? extends IModuleContainerItem> item) {
        ItemStack stack = item.getItemStack();
        if (stack.getItem() instanceof IModuleContainerItem container) {
            System.out.println(MekanismAPI.getModuleHelper().getSupported(stack));
            for (ModuleData<?> module : MekanismAPI.getModuleHelper().getSupported(stack)) {
                container.addModule(stack, module);
                ModuleHelper.INSTANCE.load(stack, module).setInstalledCount(module.getMaxStackSize());
                System.out.println("Added " + module.getMaxStackSize() + " modules of "+module.getName());
            }
        }
        return ItemPredicate.Builder.item()
              .of(stack.getItem())
              .hasNbt(stack.getOrCreateTag())
              .build();
    }
}
