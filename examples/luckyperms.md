# SleepPlugin + LuckyPerms examples

SleepPlugin integrates with LuckyPerms as a soft-dependency. Install LuckPerms,
and weighted sleep votes are enabled automatically. No SleepPlugin config changes needed.

## 1. Weighted sleep votes (meta)

A player/group with weight N counts as N sleeping players. Default weight is 1.

```bash
# VIPs count as 2 sleeping players
/lp group vip meta set sleepplugin.weight 2

# A "donor" player counts as 3
/lp user Steve meta set sleepplugin.weight 3

# Weight 2 only in the survival world (LuckyPerms context)
/lp group vip meta set sleepplugin.weight 2 server=survival

# Reset back to default (1)
/lp group vip meta unset sleepplugin.weight
```

Weight is clamped to 1-100.

### Example scenario

- 10 players online, `sleep-percentage: 50` → 5 votes needed.
- 3 VIPs (weight 2) + 7 regular players (weight 1) are online.
- If all 3 VIPs sleep → 6 votes → night is skipped.
- Without weights, 5 of the 10 players would need to sleep.

## 2. Exempt players

Players with `sleepplugin.exempt` are not counted at all in sleep calculations.

```bash
# Exclude admins from the count
/lp group admin permission set sleepplugin.exempt

# Exclude a specific builder in spectator mode
/lp user BuildBot permission set sleepplugin.exempt

# Remove
/lp user BuildBot permission unset sleepplugin.exempt
```

## 3. Bypass minimum players

A sleeping player with `sleepplugin.bypass.min-players` can skip the night
alone, even when online players are below `min-players-required`.

```bash
/lp group vip permission set sleepplugin.bypass.min-players

# Remove
/lp group vip permission unset sleepplugin.bypass.min-players
```

## 4. Admin command permission

```bash
# Give a moderator access to /sleep status and /sleep reload
/lp group moderator permission set sleepplugin.admin
```

## 5. Verifying the integration

Run `/sleep status` on the server. It should show:

```
SleepPlugin status - percentage: 50%, min players: 2, worlds: 0
LuckyPerms: enabled (weights via sleepplugin.weight meta)
```

If LuckyPerms is not installed, the line will read `LuckyPerms: not detected (all players weight 1)`.
