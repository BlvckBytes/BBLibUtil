package me.blvckbytes.bblibutil;

import me.blvckbytes.bblibdi.AutoConstruct;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

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

  public static final int[] EMPTY_SLOTMASK = new int[0];

  /**
   * Gives a player as much as possible of the provided item stack and
   * drops the remaining items at their location.
   * @param target Target player
   * @param stack Items to hand out
   * @return Number of dropped items
   */
  public int giveItemsOrDrop(Player target, ItemStack stack) {
    // Add as much as possible into the inventory
    int remaining = addToInventory(fromBukkit(target.getInventory()), stack, EMPTY_SLOTMASK);
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

  /**
   * Convert a bukkit inventory to the internally used abstraction
   * @param inv Bukkit inventory
   * @return Abstraction wrapper
   */
  public IInventory<Inventory> fromBukkit(Inventory inv) {
    return new IInventory<>() {

      @Override
      public @Nullable ItemStack get(int slot) {
        try {
          ItemStack ret = inv.getItem(slot);

          if (ret.getAmount() == 0)
            return null;

          return ret;
        } catch (Exception e) {
          return null;
        }
      }

      @Override
      public void set(int slot, @Nullable ItemStack item) {
        try {
          inv.setItem(slot, item);
        } catch (Exception ignored) {}
      }

      @Override
      public int getSize() {
        return inv.getSize();
      }

      @Override
      public Inventory getHandle() {
        return inv;
      }
    };
  }

  /**
   * Convert a plain array to the internally used abstraction
   * @param array ItemStack array
   * @return Abstraction wrapper
   */
  public IInventory<ItemStack[]> fromArray(ItemStack[] array) {
    return new IInventory<>() {

      @Override
      public @Nullable ItemStack get(int slot) {
        try {
          ItemStack ret = array[slot];

          if (ret.getAmount() == 0)
            return null;

          return ret;
        } catch (Exception e) {
          return null;
        }
      }

      @Override
      public void set(int slot, @Nullable ItemStack item) {
        try {
          array[slot] = item;
        } catch (Exception ignored) {}
      }

      @Override
      public int getSize() {
        return array.length;
      }

      @Override
      public ItemStack[] getHandle() {
        return array;
      }
    };
  }

  /**
   * Take and return the first matching ItemStack from an inventory
   * @param target Target inventory
   * @param predicate Matching predicate (returns how many of those items to remove, return all to null out, 0 to skip the item)
   * @param slotMask Optional (positive) mask of slots (empty means ignored)
   * @return First matching item which has been removed or empty if no item matched
   */
  public Optional<ItemStack> takeFirstMatching(IInventory<?> target, Function<ItemStack, Integer> predicate, int[] slotMask) {
    // Iterate all slots
    int[] slots = slotMask.length == 0 ? IntStream.range(0, target.getSize()).toArray() : slotMask;
    for (int i : slots) {
      ItemStack stack = target.get(i);

      // Empty slot
      if (stack == null)
        continue;

      // Item doesn't match
      int numRemove = predicate.apply(stack);
      if (numRemove == 0)
        continue;

      // Take all and null the slot
      if (stack.getAmount() <= numRemove) {
        target.set(i, null);
        return Optional.of(stack);
      }

      // Subtract requested amount
      stack.setAmount(stack.getAmount() - numRemove);
      ItemStack ret = new ItemStack(stack);
      ret.setAmount(numRemove);
      return Optional.of(ret);
    }

    return Optional.empty();
  }

  /**
   * Add the given items to an inventory as much as possible and
   * return the count of items that didn't fit
   * @param target Target inventory
   * @param item Item to add
   * @param slotMask Optional (positive) mask of slots (empty means ignored)
   * @return Number of items that didn't fit
   */
  public int addToInventory(IInventory<?> target, ItemStack item, int[] slotMask) {
    // This number will be decremented as space is found along the way
    int remaining = item.getAmount();
    int stackSize = item.getType().getMaxStackSize();

    // Iterate all slots
    int[] slots = slotMask.length == 0 ? IntStream.range(0, target.getSize()).toArray() : slotMask;
    for (int i : slots) {
      ItemStack stack = target.get(i);

      // Done, no more items remaining
      if (remaining < 0)
        break;

      // Completely vacant slot
      if (stack == null || stack.getType() == Material.AIR) {
        ItemStack remItems = item.clone();

        // Set as many items as possible or as many as remain
        int num = Math.min(remaining, stackSize);
        remItems.setAmount(num);

        target.set(i, remItems);
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
