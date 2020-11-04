package mekanism.common.integration.crafttweaker.bracket;

import mekanism.common.integration.crafttweaker.CrTConstants;

public interface IBracketSupport {

    String getBracketName();

    interface IGasBracketSupport extends IBracketSupport {

        @Override
        default String getBracketName() {
            return CrTConstants.BRACKET_GAS;
        }
    }

    interface IInfuseTypeBracketSupport extends IBracketSupport {

        @Override
        default String getBracketName() {
            return CrTConstants.BRACKET_INFUSE_TYPE;
        }
    }

    interface IPigmentBracketSupport extends IBracketSupport {

        @Override
        default String getBracketName() {
            return CrTConstants.BRACKET_PIGMENT;
        }
    }

    interface ISlurryBracketSupport extends IBracketSupport {

        @Override
        default String getBracketName() {
            return CrTConstants.BRACKET_SLURRY;
        }
    }
}