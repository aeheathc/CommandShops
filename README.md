CommandShops - A cuboid shop plugin for Bukkit
==================

Player created physical shops with custom 3D boundaries, buying and selling items using the money from your server's economy plugin.


Changelog
-------------------

**3.5.3**
- Added hard dependency on Register to alleviate dependency issues
- Avoid calling Register stuff in inappropriate cases

**3.5.2**
- Change Register support to connect to the separate Register plugin instead of it being embedded

**3.5.1**
- Moved to bukkit standard config file method; consolidated and categorized options. Now was the time to do it, before many people switch to this plugin.
- Enforce legit item stack limits 
- Fixed misleading errors
- Fixed misleading comments and added javadoc for everything (10% done making it meaningful)
- Minor performance improvements.
- Removed UUID that was only used for reporting.
- Removed lots of unused code
- Fixed lazy limit indicator in "/shop find"
- Tested with CB 1240

**3.5**
- Forked from LocalShops 3.1
- Economy support migrated to Register
- Updated (item list, item names, item stack limits) for 1.9, updated item search for conflicts
- /shop browse *itemname* -- No more wading through 7 pages to see stock/pricing for a single item
- Always show buy/sell prices when possible, but warn if it can't be honored by showing price in red (instead of showing price as "--"). That is, only show "--" on error. Also, fixed maxstock-detection for find/browse.
- Changed Permissions support to SuperPerms
- Changed permissions to inheriting heirarchy. Set defaults same as LS' "Local Fallback Permissions" for zero-configuration usability.
- Removed usage reporting.