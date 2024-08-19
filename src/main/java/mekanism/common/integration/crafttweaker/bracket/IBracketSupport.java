package mekanism.common.integration.crafttweaker.bracket;

import mekanism.common.integration.crafttweaker.CrTConstants;

/**
 * Represents an object that has a concept of brackets.
 */
public interface IBracketSupport {

    /**
     * Gets the string representing the "type" of substance for use in printing out brackets.
     */
    String getBracketName();

    interface IChemicalBracketSupport extends IBracketSupport {

        @Override
        default String getBracketName() {
            return CrTConstants.BRACKET_CHEMICAL;
        }
    }
}