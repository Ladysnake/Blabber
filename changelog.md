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
