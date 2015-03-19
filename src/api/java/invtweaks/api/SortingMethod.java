/*
 * Copyright (c) 2013 Andrew Crocker
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package invtweaks.api;

public enum SortingMethod {
    /** Standard 'r' sorting for generic inventories */
    DEFAULT,
    /** Sort method creating vertical columns of items.
     * Used for chests only, requires container to have a valid row size for correct results.
     */
    VERTICAL,
    /** Sort method creating horizontal rows of items.
     * Used for chests only, requires container to have a valid row size for correct results.
     */
    HORIZONTAL,
    /** Sort method for player inventory.
     * Applies to extra player-specified sorting rules for the main inventory.
     * Will always operate on main inventory.
     */
    INVENTORY,
    /** Attempts to even the number of items in each stack of the same type of item, without moving full stacks.
     * Used in crafting grid sorting.
     */
    EVEN_STACKS,
}
