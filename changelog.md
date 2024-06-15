------------------------------------------------------
Version 1.7.0
------------------------------------------------------
Updated to MC 1.20.6

**Mod Interactions**
- JEI no longer appears on the dialogue screen with the RPG layout

------------------------------------------------------
Version 1.6.0
------------------------------------------------------
**Additions**
- New illustration types:
  - `blabber:fake_player` draws a (potentially offline) player with custom data
  - `blabber:texture` draws a 2D texture from a resourcepack
- New anchor system for illustrations: you do not have to draw everything relative to the top-left corner anymore, you can pick any corner plus a couple predefined "good spots" for illustrations
  - use the `/blabber settings set debug.anchors <true|false>` command to toggle the illustration anchor debug mode, which displays the coordinates of the cursor relative to every available anchor
- Added the `scale` property for item illustrations (defaults to `1.0`)
  - Limitation: durability bars and stack count indicators do not render when a custom scale is set
- API: Added an experimental API for custom parameterized dialogue layouts
- Added customization options for dialogue layouts
  - Currently, the only configuration available is `main_text_margins` on the RPG layout
- Added Mexican Spanish localization (thanks TheLegendofSaram !)

**Changes**
- The format of entity illustrations has changed
  - They now use `x`/`y`/`width`/`height` properties instead of `x1`/`y1`/`x2`/`y2` to bring them in line with other illustrations
  - `size` has been renamed to `entity_size`
  - `stare_at_x`/`stare_at_y` properties have been moved to a `stare_at` object, which can optionally take an `anchor`
  - Illustrations in the old format should keep working, but only the new format will be actively supported going forward
- Illustration deserialization error messages have been improved slightly
- Blabber will now log a warning with some additional details when it detects that a player gets stuck without available choices
- API: `DialogueIllustration#parseText` can now throw `CommandSyntaxException`
- API: the mod is now compatible with split sourceset environments
  - The experimental illustration API has been consequently reworked

**Mod Interactions**
- JEI no longer appears on the dialogue screen

------------------------------------------------------
Version 1.5.1
------------------------------------------------------
**Fixes**
- Actions for the starting state now run as expected when a dialogue is started

------------------------------------------------------
Version 1.5.0
------------------------------------------------------

**Additions**
- Dialogues can now contain *illustrations*, extra visual content that can be positioned anywhere on the screen *(thanks Sekoia !)*
  - Added 3 default illustration types : in-world entities, fake entities, and items
  - Illustrations can also be added to individual choices, in which case they are positioned relative to the corresponding choice
- Added Chinese localization (thanks SettingDust and nageih !)

**Mod Interactions**
- Fixed compatibility with REI when loaded through Sinytra Connector

------------------------------------------------------
Version 1.4.0
------------------------------------------------------
Updated to 1.20.4

**Mod Interactions**
- REI no longer appears on the RPG dialogue screen variant

------------------------------------------------------
Version 1.3.1
------------------------------------------------------
**Fixes**
- Fixed the `/blabber` command failing to find dialogues added through regular datapacks

------------------------------------------------------
Version 1.3.0
------------------------------------------------------
**Additions**
- Added a new `DialogueActionV2` interface that lets mods act upon the interlocutor in a dialogue

------------------------------------------------------
Version 1.2.0
------------------------------------------------------
**Additions**
- Dialogues now support advanced text components, like entity selectors and scores
- You can now specify an *interlocutor* when starting a dialogue
- The interlocutor entity can be referred to in commands and texts using a new entity selector, `@interlocutor`
- It can also be referred to using a new loot condition, `blabber:interlocutor_properties`

**Changes**
- Dialogues can now be reloaded using the `/reload` command

------------------------------------------------------
Version 1.1.0
------------------------------------------------------
**Additions**
- A new dialogue screen you can use : the RPG layout, ideal for dynamic NPC dialogues with short choices
  - This new layout can be chosen on a per-dialogue basis - look at [the documentation](https://ladysnake.org/wiki/blabber#layout) for details

**Changes**
- Updated the French localization

**Fixes**
- Fixed scrolling in the dialogue screen behaving erratically

------------------------------------------------------
Version 1.0.0
------------------------------------------------------
- Updated to MC 1.20.2
- First version available on Modrinth

**Additions**
- _Conditional choices !_
  - A dialogue choice can require an arbitrary condition in the form of a JSON predicate
  - You can make it so that, when a choice is unavailable, it displays as either grayed out or hidden entirely
  - Grayed out choices display a customizable explanation when hovered
  - Conditions are refreshed every tick while a dialogue is active
  - Blabber will warn you in the logs at initialization if a dialogue has a risk of leaving a player without choices
- You can now see a little arrow icon next to the currently selected choice
  - This icon gets replaced with a lock when the choice is unavailable
- If despite all validation a player ends up on a dialogue screen with no choice available, they will now see an "escape hatch" choice suggesting they report the issue

**Changes**
- **BREAKING :** Dialogues are now loaded from `data/<namespace>/blabber/dialogues/` instead of `data/<namespace>/blabber_dialogues/`
- **BREAKING (for modders) :** The maven group and package are now `org.ladysnake` instead of `io.github.ladysnake`

**Mod Interactions**
- REI and EMI no longer appear on the dialogue screen

------------------------------------------------------
Version 0.6.0
------------------------------------------------------
Updated to MC 1.19.4

------------------------------------------------------
Version 0.5.1
------------------------------------------------------
Fixes
- Fixed quilt incompatibility caused by unsupported API

------------------------------------------------------
Version 0.5.0
------------------------------------------------------
Updated to MC 1.19.3

------------------------------------------------------
Version 0.4.0
------------------------------------------------------
Updated to MC 1.19.1

------------------------------------------------------
Version 0.3.0
------------------------------------------------------
Updated to MC 1.19

Additions
- Dialogues now get validated upon world loading - the following issues will cause failures :
  - Infinite loops preventing users from reaching a dialogue state with type `dialogue_end`
  - Dialogue states that are not endings but offer no choice either (unless it is guaranteed that players cannot reach them)

------------------------------------------------------
Version 0.2.0
------------------------------------------------------
- Updated to 1.18.2

------------------------------------------------------
Version 0.1.3
------------------------------------------------------
- Updated CCA dependency => **Update your dependencies: `io.github.onyxstudios.Cardinal-Components-API` -> `dev.onyxstudios.cardinal-components-api`

------------------------------------------------------
Version 0.1.2
------------------------------------------------------
- Fixed `unskippable` not being actually optional
- Made more errors appear when loading an invalid dialogue definition file

------------------------------------------------------
Version 0.1.1
------------------------------------------------------
- Add localization for dialogue instructions (thanks cominixo01, Tijmen, BingQI, and AwsAlex!)

------------------------------------------------------
Version 0.1.0
------------------------------------------------------
Initial version

Additions
- Data-driven dialogues
- In-game command to start a dialogue
