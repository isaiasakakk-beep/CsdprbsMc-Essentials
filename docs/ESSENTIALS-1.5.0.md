# CsdprbsMc Essentials 1.5.0

## Interface
Java and Bedrock players use the same inventory-based menus. The previous Floodgate/Cumulus form prototype was removed.

## PvP and CombatLog
- Combat lasts 15 seconds after the most recent player hit.
- The plugin warns both players and displays a countdown.
- The main compass menu, TPA, BACK, HOME, WARPS, plugin/command teleports, Ender Pearls and Chorus Fruit teleports are blocked while tagged.
- Logging out during combat drops the player's carried items. If the opponent is online, the opponent receives the logout kill, reputation and the logged-out player's wallet balance.

## Economy
- `/bal`: wallet and reputation.
- `/pay <player> <amount>`: transfer wallet money.
- `/bank`, `/bank depositar <amount>`, `/bank sacar <amount>`: bank balance, deposit and withdraw.
- `/shop`: item shop, including name/material search, inventory selling and sell-all.
- `/shopadmin preco <MATERIAL> <amount>`: set a shop purchase/sale base price.
- `/shopadmin limite <MATERIAL> <true|false>`: set whether an item is limited to one per purchase.
- `/auction`: browse player listings.
- `/auction vender <price>`: list the item in the main hand.
- `/auction cancelar <id-prefix>`: cancel one of your own listings.

When a player kills another player, the killer takes the victim's entire wallet balance. The victim's wallet becomes zero. Kills increase reputation by one and deaths decrease it by one.

## Player trades
- `/trade <player>`: send a trade request.
- `/trade aceitar` or `/trade recusar`: respond to a pending request.
- In the trade inventory, click items in your own inventory to add them to your offer; click your offered items to take them back.
- Both players must accept the final offer.
- Changing an offer resets both acceptances.
- `PEDIR MAIS` notifies the other player and resets acceptance.
- Closing the trade or disconnecting cancels it and returns the offered items.

## Reputation and tags
- `/reputacao`: view reputation.
- Default member tag is bold gold/orange and yellow. Clan gradient letters and ranking tags are bold.
- `/tagadmin <player> <tag...>`: set a custom player tag. Use ampersand color codes, for example `&6&l[VIP]`.
- `/tagadmin <player> reset`: restore the default member tag.
- Tag configuration requires `csdprbsmc.admin`.

## Clans
The existing clan system remains: maximum 8 members, leader-controlled invitations and applications, join/leave, kick, transfer leadership, disband, clan information, and leader-only two-color letter gradient customization.
