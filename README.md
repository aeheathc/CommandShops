CommandShops - A cuboid shop plugin for Bukkit
==================

Player created physical shops with custom 3D boundaries, buying and selling items using the money from your server's economy plugin.

Changelog
-------------------
**HEAD **
* Fixed blaze rod detection
* Removed bundle system
* Changed Register dependency to softdepend to get around long-standing Bukkit bug. Less correct but it works.
* Block faulty pricing (shop's buy price greater than sell etc)

**3.6.1**

* Admins can now move other players' shops
* Updated command documentation
* Fixed Readme formatting

**3.6**

* Tested with CB 1597
* Switched to new Config class, as old became deprecated. No change for users.
* Improved item search, Redstone Repeater in particular should be easier to work with
* Fixed bad error "no item was (not) found"
* Added required damage value for some items to be found (e.g. Piston needs :7)
* Updated available items for Minecraft 1.0 including all potion types
* Default max stock for newly added items is now 10 for your protection
* Shops that don't appear in /find list (because they have no prices set) no longer count toward the number of shops shown
* Corrected "durable" status of leather armor
* Wrong durability calculation determined to be a Bukkit bug, removed special cases.
* Removed unused code. No more compiler warnings
* Added stock/max to "/shop find"
* Stop showing stock/max information when stock is unlimited
* "/shop search" with no params searches for item in hand

**3.5.4**

* Fixed bug where transactions would fail based on seller's balance
* Improved error reporting
* Gave admins more power to meddle with shops (to better fit the existing description)
* Tested with CB 1317

**3.5.3**

* Added hard dependency on Register to alleviate dependency issues
* Avoid calling Register stuff in inappropriate cases

**3.5.2**

* Change Register support to connect to the separate Register plugin instead of it being embedded

**3.5.1**

* Moved to bukkit standard config file method; consolidated and categorized options. Now was the time to do it, before many people switch to this plugin.
* Enforce legit item stack limits 
* Fixed misleading errors
* Fixed misleading comments and added javadoc for everything (10% done making it meaningful)
* Minor performance improvements.
* Removed UUID that was only used for reporting.
* Removed lots of unused code
* Fixed lazy limit indicator in "/shop find"
* Tested with CB 1240

**3.5**

* Forked from LocalShops 3.1
* Economy support migrated to Register
* Updated (item list, item names, item stack limits) for 1.9, updated item search for conflicts
* /shop browse *itemname* -- No more wading through 7 pages to see stock/pricing for a single item
* Always show buy/sell prices when possible, but warn if it can't be honored by showing price in red (instead of showing price as "--"). That is, only show "--" on error. Also, fixed maxstock-detection for find/browse.
* Changed Permissions support to SuperPerms
* Changed permissions to inheriting heirarchy. Set defaults same as LS' "Local Fallback Permissions" for zero-configuration usability.
* Removed usage reporting.