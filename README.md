CommandShops - A cuboid shop plugin for Bukkit
==================

Player created physical shops with custom 3D boundaries, buying and selling items using the money from your server's economy plugin.

Changelog
-------------------

**HEAD**

* Added worldguard integration. Give a region name with the "/shop select" commadn, and instead of entering selection mode, the shop you move/create will just be attached to the named region.
* Restricted /reload to admin

**4.1.3**

* Added transaction-log querying in-game! See the commands page for "/shop log". Supports filtering parameters to find specific information.
* Pending notifications now persist after a server reboot! Now, you are guaranteed to get notified about every transaction eventually for your shops having notifications enabled.
* Notification system now consolidates messages having the same shop+player+item+action. No more notification spam when someone buys a ton of some item 1 at a time.
* Fixed logging for transactions with a shop you own/manage: Total cost is now correctly recorded as zero and these actions no longer generate notifications for the player who did them.  
* Made brick related item names more unique, distinguishing regular clay bricks from stone/nether bricks, and distinguishing individual bricks from blocks.
* Fixed carrot being detected as Rotten Flesh
* Fixed Poison Potato being detected as Posion Potion (base wordform for all potions changed from "pot" to "poti")
* Removed support for old "shop file" format. It only worked for files from LS3 which is ancient now.

**4.1.2**

* Removed third party code for database layer to correct licensing issue
* Fixed "Ballooning log file" issue by correcting trim method. 

**4.1.1**

* Tested with CB 1.5.2
* Fixed stack size for pork and bucket
* Added config option to block moving shops to another world - should be useful for servers with per-world inventory
* Updated item list for latest snapshot with focus on Minecraft 1.5.2
* Added "/shop reload" to reload the configuration of this plugin without restarting the server or doing a server-wide reload. (This is not a way to recover from plugin crashes however)
* Made MySQL reconnect on timeout - should eliminate the need to restart periodically

**4.1.0**

* Tested with CB 1.4.7
* Added custom item support -- should be able to support modded items now as well as ad hoc updates to the item list
* Items updated for 1.4.7 and a few more snapshots. Some existing names changed to match official disambiguations.
* Permissions fix to allow denying manager.* while allowing user.*
* Log trim fix for MySQL mode

**4.0.5**

* Signs stack to 16

**4.0.4**

* Tested with CB 1.3.1-R2
* Simplified DB management to improve future DB support
* Updated items for 1.3.2
* Added stub for WorldGuard integration but it has no functionality yet.

**4.0.3**

* Tested with CB 1.2.5-R1.2
* Fixed log overriding leaf detection for redwood
* Fixed leather detection
* Integrated SQLibrary 3.0.7 to fix "too many connections" issue

**4.0.2**

* Fixed locale problem inserting commas into numbers.

**4.0.1**

* Tested with CB 1.2.4-R0.1
* Fixed MySQL 5.0 compliance
* Fixed price setting by id:damage getting overlooked
* Fixed detection for Jungle Sapling and Splash Potion of Fire Resistance
* Added new items
* Removed default shop size, all shops must be made with selections
* Changed browse and find output to be less likely to wrap

**4.0**

* Tested with CB 1.2.3-R0.2
* Updated for new event system
* Gave commandshops.user.* by default
* Made "select" ability implicitly granted by permissions for commands it is used with instead of separately
* Removed aliasing; now only command is "/shop"
* Switched to Vault for econ support
* Fixed many places where economy failure was interpreted as insufficient funds instead of actually checking the balance
* Remove references to bundles in documentation and in-game help
* Switched to SQL (both sqlite for convenience and mysql for power) using "PatPeter.SQLibrary" which may give us support for other DBs in the future.
* Removed concept of an item being "added" to a shop. Shops now independently have or not have an amount, buy price, and sell price for any item. You can now set buy and sell prices to 0. Set prices to NULL by running the set command with no number.
* Removed player data tracking system. Should improve performance. This unfortunately has removed player enter/exit messages. You can replicate this functionality with WorldGuard. Future work in this direction will likely be automatic WorldGuard integration.
* Commands now always check what shop you're in, so no more having to move around after warping to register that you're in a shop.  
* Fixed bad help and error messages
* Even more compact and informative output
* Log everything - including shop management actions, not just transactions
* All permanent, non-thirdparty code now has full Javadoc coverage and no TODOs

**3.6.2**

* Fixed search for blaze rod, glistering melon, fermented spider eye
* Removed bundle system
* Changed Register dependency to softdepend to get around long-standing Bukkit bug. Less correct but it works.
* Block faulty pricing (shop's buy price greater than sell etc)
* Added support for commonly spawned alternate non-legit versions of potions (as "Bootleg") so people stop complaining that "potions don't work!!111" when they find they can't add their normally-unobtainable potion.
* stop letting shops with unlimited money collect revenue because it duplicated money

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