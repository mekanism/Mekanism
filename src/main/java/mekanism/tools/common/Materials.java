package mekanism.tools.common;

import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.common.MekanismItem;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.ToolsConfig;
import mekanism.common.config.ToolsConfig.ArmorBalance;
import mekanism.common.config.ToolsConfig.ToolBalance;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;

public enum Materials {
    OBSIDIAN("OBSIDIAN", cfg -> cfg.toolOBSIDIAN, cfg -> cfg.toolOBSIDIAN2, cfg -> cfg.armorOBSIDIAN, MekanismItem.REFINED_OBSIDIAN_INGOT::getItemStack),
    LAPIS_LAZULI("LAPIS_LAZULI", cfg -> cfg.toolLAZULI, cfg -> cfg.toolLAZULI2, cfg -> cfg.armorLAZULI, () -> new ItemStack(Items.DYE, 1, 4), SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND),
    OSMIUM("OSMIUM", cfg -> cfg.toolOSMIUM, cfg -> cfg.toolOSMIUM2, cfg -> cfg.armorOSMIUM, MekanismItem.OSMIUM_INGOT::getItemStack),
    BRONZE("BRONZE", cfg -> cfg.toolBRONZE, cfg -> cfg.toolBRONZE2, cfg -> cfg.armorBRONZE, MekanismItem.BRONZE_INGOT::getItemStack),
    GLOWSTONE("GLOWSTONE", tools -> tools.toolGLOWSTONE, tools -> tools.toolGLOWSTONE2, cfg -> cfg.armorGLOWSTONE, MekanismItem.REFINED_GLOWSTONE_INGOT::getItemStack),
    STEEL("STEEL", cfg -> cfg.toolSTEEL, cfg -> cfg.toolSTEEL2, cfg -> cfg.armorSTEEL, MekanismItem.STEEL_INGOT::getItemStack);

    private final Function<ToolsConfig, ToolBalance> materialFunction;
    private final Function<ToolsConfig, ToolBalance> paxelMaterialFunction;
    private final Function<ToolsConfig, ArmorBalance> armorMaterialFunction;
    private final Supplier<ItemStack> repairStackSupplier;
    private final SoundEvent equipSound;
    private final String materialName;
    private ToolMaterial material;
    private ToolMaterial paxelMaterial;
    private ArmorMaterial armorMaterial;
    private boolean initialized;
    private float axeDamage;
    private float axeSpeed;

    Materials(@Nonnull String name, @Nonnull Function<ToolsConfig, ToolBalance> material, @Nonnull Function<ToolsConfig, ToolBalance> paxelMaterial,
          @Nonnull Function<ToolsConfig, ArmorBalance> armorMaterial, @Nonnull Supplier<ItemStack> repairStack) {
        this(name, material, paxelMaterial, armorMaterial, repairStack, SoundEvents.ITEM_ARMOR_EQUIP_IRON);
    }

    Materials(@Nonnull String name, @Nonnull Function<ToolsConfig, ToolBalance> material, @Nonnull Function<ToolsConfig, ToolBalance> paxelMaterial,
          @Nonnull Function<ToolsConfig, ArmorBalance> armorMaterial, @Nonnull  Supplier<ItemStack> repairStack, @Nonnull SoundEvent equipSound) {
        this.materialName = name;
        this.materialFunction = material;
        this.paxelMaterialFunction = paxelMaterial;
        this.armorMaterialFunction = armorMaterial;
        this.repairStackSupplier = repairStack;
        this.equipSound = equipSound;
    }

    private static ToolMaterial getToolMaterial(@Nonnull String enumName, @Nonnull ToolBalance config) {
        return EnumHelper.addToolMaterial(enumName, config.harvestLevel.val(), config.maxUses.val(), config.efficiency.val(), config.damage.val(), config.enchantability.val());
    }

    private static ArmorMaterial getArmorMaterial(@Nonnull String enumName, @Nonnull ArmorBalance config, @Nonnull SoundEvent equipSound) {
        return EnumHelper.addArmorMaterial(enumName, "TODO", config.durability.val(), new int[]{
              config.feetProtection.val(), config.legsProtection.val(), config.chestProtection.val(), config.headProtection.val(),
              }, config.enchantability.val(), equipSound, config.toughness.val());
    }

    public static void load() {
        for (Materials material : values()) {
            material.init();
        }
    }

    private void init() {
        if (initialized) {
            return;
        }
        ToolBalance materialBalance = materialFunction.apply(MekanismConfig.current().tools);
        ToolBalance paxelMaterialBalance = paxelMaterialFunction.apply(MekanismConfig.current().tools);
        ArmorBalance armorBalance = armorMaterialFunction.apply(MekanismConfig.current().tools);

        this.material = getToolMaterial(materialName, materialBalance);
        this.paxelMaterial = getToolMaterial(materialName + "2", paxelMaterialBalance);
        this.armorMaterial = getArmorMaterial(materialName, armorBalance, equipSound);
        this.axeDamage = materialBalance.axeAttackDamage.val();
        this.axeSpeed = materialBalance.axeAttackSpeed.val();
        //Set repair stack of the material
        ItemStack repairStack = repairStackSupplier.get();
        material.setRepairItem(repairStack);
        paxelMaterial.setRepairItem(repairStack);
        armorMaterial.setRepairItem(repairStack);
        initialized = true;
    }

    //The below getters should **NOT** be used before the Materials are initialized. Storing the Materials object for reference
    // before any of the getters is fine as the Fucntions/Suppliers mean nothing is prematurely initialized (before the config gets red for example)
    public ToolMaterial getMaterial() {
        return material;
    }

    public ToolMaterial getPaxelMaterial() {
        return paxelMaterial;
    }

    public ArmorMaterial getArmorMaterial() {
        return armorMaterial;
    }

    public String getMaterialName() {
        return materialName;
    }

    public float getAxeDamage() {
        return axeDamage;
    }

    public float getAxeSpeed() {
        return axeSpeed;
    }
}