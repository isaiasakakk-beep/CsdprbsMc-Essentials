# CsdprbsMc Essentials 1.5.0

## Interface
Java and Bedrock players use the same inventory-based menus. The previous Floodgate/Cumulus form prototype was removed.

## PvP and CombatLog
- Combat lasts 15 seconds after the most recent player hit.
- The plugin warns both players and displays a countdown.
- The main compass menu, TPA, BACK, HOME, WARPS, plugin/command teleports, Ender Pearls and Chorus Fruit teleports are blocked while tagged.
- Logging out during combat drops the player's carried items. If the opponent is online, the opponent receives the logout kill, reputation, and all money from the logged-out player's wallet and bank.

## Economy
- The compass main menu opens the bank directly.
- Bank deposits and withdrawals are available through preset-amount buttons and all available funds; no player command is needed.
- The bank screen shows wallet and bank balances.
- `/bal` and `/bank` remain optional shortcuts.
- `/pay <player> <amount>` remains an optional shortcut for player-to-player wallet transfers.
- `/shop`: item shop, including name/material search, inventory selling and sell-all.
- `/shopadmin preco <MATERIAL> <amount>`: set a shop purchase/sale base price.
- `/shopadmin limite <MATERIAL> <true|false>`: set whether an item is limited to one per purchase.
- The compass opens the player marketplace.
- The marketplace has an **ANUNCIAR ITEM** button, then lets the player select an inventory stack and choose a preset price.
- Clicking one of the seller's own listings cancels it and returns the item.
- `/auction` and its subcommands remain optional shortcuts.

When a player kills another player, the killer takes all money from the victim's wallet and bank. Both balances become zero. Kills increase reputation by one and deaths decrease it by one.


- The compass **Meu Perfil** screen shows wallet, bank, reputation and kills.
- Money transfers are available from **Banco > Transferir dinheiro**, by choosing an online player and a preset amount or all wallet funds.

## Player trades
- The compass opens the player list for starting a trade.
- Incoming requests are shown in **Trocas > Pedidos recebidos**, with accept and decline buttons.
- `/trade` subcommands remain optional shortcuts.
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
