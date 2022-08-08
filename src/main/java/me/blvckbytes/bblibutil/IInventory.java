package me.blvckbytes.bblibutil;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/08/2022

  Internally used inventory abstraction to also allow for
  direct array access, for example.
*/
public interface IInventory<T> {

  /**
   * Get an item by it's slot without any exceptions
   * @param slot Target slot
   * @return Target item, null for vacant slots or out of bounds indices
   */
  @Nullable ItemStack get(int slot);

  /**
   * Set an item to a specific slot without any exceptions
   * (does nothing on out of bounds indices)
   * @param slot Target slot
   * @param item Item to set, null to clear
   */
  void set(int slot, @Nullable ItemStack item);

  /**
   * Get the size of this inventory, which results in an interval
   * of valid slots ranging from [0;size-1]
   */
  int getSize();

  /**
   * Get the underlying storage handle
   */
  T getHandle();

}
