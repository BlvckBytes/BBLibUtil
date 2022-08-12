package me.blvckbytes.bblibutil;

import me.blvckbytes.bblibdi.AutoConstruct;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/23/2022

  Various utilities in regards to handling inventory related tasks.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published
  by the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
    int remaining = addToInventory(fromBukkit(target.getInventory()), stack, EMPTY_SLOTMASK, false);
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
      w.dropItem(target.getEyeLocation(), items);
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
        return inv.getStorageContents().length;
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
   * @param allOrNothing Whether all items need to fit, if true, it's either all or none and no inbetween
   * @return Number of items that didn't fit
   */
  public int addToInventory(IInventory<?> target, ItemStack item, int[] slotMask, boolean allOrNothing) {
    // This number will be decremented as space is found along the way
    int remaining = item.getAmount();
    int stackSize = item.getType().getMaxStackSize();

    // At first, only store planned partitions using the format <Slot, Amount>
    // and execute them all at once at the end, to have a transaction-like behavior
    List<Tuple<Integer, Integer>> partitions = new ArrayList<>();
    List<Integer> vacant = new ArrayList<>();

    // Iterate all slots
    int[] slots = slotMask.length == 0 ? IntStream.range(0, target.getSize()).toArray() : slotMask;
    for (int i : slots) {
      ItemStack stack = target.get(i);

      // Done, no more items remaining
      if (remaining < 0)
        break;

      // Completely vacant slot
      if (stack == null || stack.getType() == Material.AIR) {
        vacant.add(i);
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
        partitions.add(new Tuple<>(i, stack.getAmount() + remaining));
        remaining = 0;
        break;
      }

      // Set to a full stack and subtract the delta from remaining
      partitions.add(new Tuple<>(i, stackSize));
      remaining -= usable;
    }

    // If there are still items remaining, start using vacant slots
    if (remaining > 0 && vacant.size() > 0) {
      for (int v : vacant) {
        if (remaining <= 0)
          break;

        // Set as many items as possible or as many as remain
        int num = Math.min(remaining, stackSize);
        partitions.add(new Tuple<>(v, num));
        remaining -= num;
      }
    }

    // Requested all or nothing, didn't fit completely
    if (allOrNothing && remaining > 0)
      return item.getAmount();

    // Apply partitions to inventory
    for (Tuple<Integer, Integer> partition : partitions) {
      ItemStack curr = target.get(partition.getA());

      // Slot empty, create new item
      if (curr == null) {
        curr = item.clone();
        curr.setAmount(partition.getB());
        target.set(partition.getA(), curr);
        continue;
      }

      // Update existing slot
      curr.setAmount(partition.getB());
    }

    // Return remaining items that didn't fit
    return remaining;
  }
}
