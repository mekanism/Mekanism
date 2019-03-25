# Mekanica for Minecraft 1.12 #

Mekanica is a fork of the Mekanism mod featuring high-tech machinery that can be used to create powerful tools, armor, and weapons. This fork is fully backwards-compatible with Mekanism, with a focus on
delivering the same features with better performance and fewer bugs. 

As of the initial release of this mod, we've reduced the size of the code base by 12% (~18k LOC); less code means less bugs! Major improvements include:
* Overhauled entire sound system to work reliably with mufflers; reduced size of sound files by 90%
* Deduplicated code across the GUI to allow for consistency and easier maintenance
* Made first pass at improving custom item model rendering
* Removed voice-server functionality; have 5% of your CPU back!
* Changed gases to use tints instead of textures; less memory, easier to change and differentiate visually
* Improved CraftTweaker support; reduced associated log spam
* Added a `removeAllRecipes()` CraftTweaker method to each type. This removes all the built in recipes of that type.
* Osmium Compressor, Chemical Injection Chamber, and the Purification Chamber, now accept any gas that they have recipes added for via CraftTweaker.
* Improved charging of custom IC2 items (such as armor w/ jetpack attachments)
* Block of Charcoal burn time is now the standard 16000 ticks
* Disabled factories/machines no longer show up in JEI and various other JEI rendering fixes.
* Bronze recipe is now the standard 3 copper/1 tin = 4 bronze
* Glowstone and Refined Obsidian nuggets are craftable, and can be crafted back into their ingot variant.
* Fix bug in chunk loaders which caused them not to restore state on server restart
* Made things more cross compatible with other mods by moving Entity and Recipe registration to their proper registry events.
* Fix bugs in Thermal Evap containers, rendering of certain item models on 1.12.2, and many other minor bugs

# Frequently Asked Questions (FAQ) #

* If I replace Mekanism with Mekanica, will my world still work?

Yes. This mod aims to maintain full backward compatibility. Our focus right now is on improving performance, quality and maintability, not introducing new content. The one notable change that might break compatibility at this point is the removal of the voice server functionality. We might add/remove content in the future, but when that happens there will be ample and LOUD notice.

* Is there a Discord?

Yes. You can join us here: https://discord.gg/nmSjMGc

# License #

Mekanica, being a fork, is under the same MIT license as Mekanism. You may use it in modpacks, reviews or any other form as long as you abide by the terms below. 

Copyright 2017 Aidan C. Brady

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

# Credits #

## Mekanica ##
  * pupnewfster
  * dizzyd

## Mekanism ##
  * aidancbrady
  * thommy101
  * thiakil
  * unpairedbracket
  * CyanideX (artwork)
  * Cheapshot (artwork)
  * Archadia (artwork)
  * micdoodle
  * Bluexin
  * JaSpr

## YourKit ##
YourKit supports open source projects with innovative and intelligent tools for monitoring and 
profiling Java and .NET applications. YourKit is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler), 
[YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/) and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).
Mekanica uses YourKit for all our profiling needs!