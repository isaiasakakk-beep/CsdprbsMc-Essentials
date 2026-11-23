# CsdprbsMc Essentials 1.5.0

## Interface
Java and Bedrock players use the same inventory-based menus opened by the Recovery Compass. The Floodgate/Cumulus form prototype was discarded.

## Player features through the compass
- Main menu: TPA, homes, warps, clans, rankings, shop/economy, missions, wanted players, contracts, treasures, BACK, marketplace, trades, bank and profile.
- TPA: select an online player, choose the direction, and accept or decline incoming requests in the menu.
- Homes: create up to five automatic homes, teleport, update the saved location or delete one.
- Clans: create from the compass (the name is generated from the player's username), browse clans, apply to join, accept invitations, invite online players, see applications and clan details, leave, and customize the two-color name gradient as leader.
- Clan leader management: view members, transfer leadership, expel a member, and dissolve the clan with confirmation.
- Shop: browse categories or the full alphabetical catalog, buy one or 16 items, select inventory items to sell, choose a quantity and confirm, or use sell-all.
- Wanted players: select a target and set a bounty using preset values.
- Missions: claim completed standard, weekly and specific missions from their menus.
- Player marketplace: browse listings, announce an inventory stack at a preset price, buy other players' listings, or click one's own listing to cancel and return the item.
- Trades: choose a player, view incoming requests, accept or decline, manage offers in the trade inventory, and require both players to accept the final offer.
- Bank and profile: view wallet/bank/reputation/kills, deposit and withdraw preset amounts or all available funds, and transfer preset amounts or all wallet funds to an online player.

## PvP and CombatLog
- Combat lasts 15 seconds after the most recent player hit.
- Both players receive a warning and countdown.
- The compass, TPA, BACK, HOME, WARPS, plugin/command teleports, Ender Pearls and Chorus Fruit teleports are blocked while tagged.
- Logging out during combat causes a CombatLog defeat and item drop; the online opponent receives the logout kill, reputation and the defeated player's wallet and bank money.

## Economy
- Killing another player transfers the victim's wallet and bank money to the killer and sets both victim balances to zero.
- Kills increase reputation by one and deaths decrease it by one.
- /bal, /bank, /pay, /auction and /trade remain optional command shortcuts.
- /shopadmin preco MATERIAL amount and /shopadmin limite MATERIAL true|false remain administrative shop configuration commands.

## Tags
- Default member tag uses bold gold/orange and yellow.
- Clan gradient and ranking tags are bold.
- /tagadmin player tag... and /tagadmin player reset are administrative shortcuts requiring csdprbsmc.admin.

## Build and testing status
- A previous code revision compiled successfully in GitHub Actions (run #90).
- The latest compass UI additions were pushed after that successful run and still need a fresh build check.
- No claim of successful Paper/Geyser gameplay testing is made yet. The inventory menus, economy transactions, trade/marketplace edge cases and Java/Bedrock interaction still need testing on a live test server.
