package me.blvckbytes.bblibutil;

import me.blvckbytes.bblibdi.AutoConstruct;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/23/2022

  Various utilities in regards to handling inventory related tasks.
*/
@AutoConstruct
public class InventoryUtil {

  //=========================================================================//
  //                                   API                                   //
  //=========================================================================//

  /**
   * Gives a player as much as possible of the provided item stack and
   * drops the remaining items at their location.
   * @param target Target player
   * @param stack Items to hand out
   * @return Number of dropped items
   */
  public int giveItemsOrDrop(Player target, ItemStack stack) {
    // Add as much as possible into the inventory
    int remaining = addToInventory(target, stack);
    int dropped = remaining;

    // Done, everything fit
    if (remaining == 0)
      return 0;

    // Could not get the world, all further iterations make no sense
    World w = target.getLocation().getWorld();
    if (w == null)
      return 0;

    // Drop all remaining items
    int stackSize = stack.getMaxStackSize();
    while (remaining > 0) {
      ItemStack items = stack.clone();
      items.setAmount(Math.min(remaining, stackSize));
      w.dropItemNaturally(target.getEyeLocation(), items);
      remaining -= items.getAmount();
    }

    return dropped;
  }

  //=========================================================================//
  //                                Utilities                                //
  //=========================================================================//

  /**
   * Add the given items to a player's inventory as much as possible and
   * return the count of items that didn't fit
   * @param target Target player
   * @param item Item to add
   * @return Number of items that didn't fit
   */
  private int addToInventory(Player target, ItemStack item) {
    // This number will be decremented as space is found along the way
    int remaining = item.getAmount();
    int stackSize = item.getType().getMaxStackSize();

    // Iterate all slots
    ItemStack[] contents = target.getInventory().getStorageContents();
    for (int i = 0; i < contents.length; i++) {
      ItemStack stack = contents[i];

      // Done, no more items remaining
      if (remaining < 0)
        break;

      // Completely vacant slot
      if (stack == null || stack.getType() == Material.AIR) {
        ItemStack remItems = item.clone();

        // Set as many items as possible or as many as remain
        int num = Math.min(remaining, stackSize);
        remItems.setAmount(num);

        target.getInventory().setItem(i, remItems);
        remaining -= num;
        continue;
      }

      // Incompatible stacks, ignore
      if (!stack.isSimilar(item))
        continue;

      // Compatible stack but no more room left
      int usable = Math.max(0, stackSize - stack.getAmount());
      if (usable == 0)
        continue;

      // Add the last few remaining items, done
      if (usable >= remaining) {
        stack.setAmount(stack.getAmount() + remaining);
        remaining = 0;
        break;
      }

      // Set to a full stack and subtract the delta from remaining
      stack.setAmount(stackSize);
      remaining -= usable;
    }

    // Return remaining items that didn't fit
    return remaining;
  }
}
