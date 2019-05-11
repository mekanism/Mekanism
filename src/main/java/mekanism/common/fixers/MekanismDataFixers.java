package mekanism.common.fixers;

import mekanism.common.Mekanism;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Created by Thiakil on 5/05/2019.
 */
public class MekanismDataFixers {

    public static final int DATA_VERSION = MekFixers.values().length;

    public static void register() {
        CompoundDataFixer fixer = FMLCommonHandler.instance().getDataFixer();
        ModFixs fixes = fixer.init(Mekanism.MODID, DATA_VERSION);
        //Fix old tile entity names
        fixes.registerFix(FixTypes.BLOCK_ENTITY, new MekanismTEFixer(MekFixers.TILE_ENTITIES));
    }

    /**
     * Defines fix versions. DO NOT CHANGE ORDERING or you will break things.
     */
    public enum MekFixers {
        TILE_ENTITIES,
        ;

        /**
         * 1 based version number
         *
         * @return the version number to use for this fix
         */
        public int getFixVersion() {
            return this.ordinal() + 1;
        }
    }
}
