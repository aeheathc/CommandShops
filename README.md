CommandShops - A cuboid shop plugin for Bukkit
==================

Player created physical shops with custom 3D boundaries, buying and selling items using the money from your server's economy plugin.

* No physical components needed, business done entirely through commands
* Perfect for RP type servers so you can let your players build elaborate markets and not need ugly coded signs/chests all over it

**Requirements:**

* Vault
* Any economy plugin supported by Vault

**Optional Dependencies:** 

* To use permissions, get any permissions plugin that can feed into SuperPerms. By default everyone can use and create shops but only ops have the admin powers.
* To enable MySQL mode you need a MySQL server obviously.
* To use WorldGuard integration features you'll need WorldGuard.

**Documentation**

* [Commands](https://rawgithub.com/aeheathc/CommandShops/master/doc/commands.html)
* [Permissions](src/plugin.yml?raw=true)
* [Config file](src/config.yml?raw=true)
* [Upcoming Features](doc/todo.txt?raw=true)


Changelog
-------------------

**4.2.4**

* Tested with CB beta 1.7.9-R0.2
* Fixed SQLite getting deadlocked on startup in bukkit 1.7.x
* Having a non-legit oversized stack of items in your inventory no longer confuses the inventory space counter.
* Fixed format of items.yml. Previously, the maxstack numbers were written with quotes causing them all the be read as 0.
* Max stack size of items is now checked by asking Bukkit instead of using hardcoded values. (This makes the items.yml change moot as the maxstack field is now gone entirely)
* Checking for whether an item has "durability" is now done by asking bukkit instead of comparing against a hardcoded list.
* Tons of changes done for eliminating use of item ID related commands provided by Bukkit as they are now deprecated.
* Reworked Vault interaction since it now wants player objects instead of player name strings.
* Fixed detection of various slabs
* Removed "remove" command. It was always a weird and useless command to take ALL of an item out of your shop even if you don't have inventory space. An accident could be bad if there are too many items to get them back in before they despawn. Remember that you can just use the buy command to take items out of your shop as an owner and it doesn't cost you any money.
* Removed much unused code
* Documented the ItemInfo class after reclassifying it as permanent.
* Fixed softdepend on WorldGuard. CommandShops should once again be usable without WorldGuard, but having WorldGaurd will allow additional features.
* Added items up to snapshot 14w21b (which is between the current 1.7.9 and an expected 1.8.x)
* Log now reports remaining stock for shops with unlimited stock as "Unlimited" instead of "-1" 

**4.2.3**

* Tested with CB 1.6.2
* Fixed confusion between discs "11" and "wait"
* New items up to the current snapshot of MC 1.7
* Moving or deleting a shop that had a WG region now removes enter message from the old region.

**4.2.2**

* New config option "Markets" where you can specify a list of WG regions that act as Markets, meaning region-based shops cannot be created outside them. A market can't have a shop connected directly to it, either. The default is blank, meaning region-based shops can still be created anywhere.

**4.2.1**

* Allow admins to select regions they don't own

**4.2.0**

* Added worldguard integration. Give a region name with the "/shop select" command, and instead of entering selection mode, the shop you move/create will just be attached to the named region.
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
