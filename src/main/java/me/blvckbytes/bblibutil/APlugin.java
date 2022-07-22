package me.blvckbytes.bblibutil;

import me.blvckbytes.bblibdi.AutoConstructer;
import me.blvckbytes.bblibutil.logger.ILogColorSupplier;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/15/2022

  Adding common routines which need direct access to the plugin
  reference by decorating said instance.
*/
public abstract class APlugin extends JavaPlugin implements ILogColorSupplier {

  // Keeps track of whether the plugin went into
  // disabling state (may not request any more tasks)
  private boolean disabling = false;

  private AutoConstructer ac;

  @Override
  public void onEnable() {
    this.disabling = false;

    try {
      // Create all resources within this package
      ac = new AutoConstructer(this);
    } catch (Exception e) {
      e.printStackTrace();
      // Disable this plugin if it didn't pass auto-construct
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    this.disabling = true;

    // Call cleanup on all interested resources
    if (ac != null)
      ac.cleanup();
  }

  /**
   * Run a task on the next tick
   * @param task Task to run
   */
  public void runTask(Runnable task) {
    runTask(task, 0);
  }

  /**
   * Run a task after a certain amount of ticks elapsed
   * @param task Task to run
   * @param delay Delay to run this task in ticks
   */
  public void runTask(Runnable task, int delay) {
    if (disabling)
      return;

    if (delay == 0) {
      Bukkit.getScheduler().runTask(this, task);
      return;
    }

    Bukkit.getScheduler().runTaskLater(this, task, delay);
  }

  /**
   * Run a task asynchronously on the next tick
   * @param task Task to run
   */
  public void runTaskAsynchronously(Runnable task) {
    if (disabling)
      return;

    Bukkit.getScheduler().runTaskAsynchronously(this, task);
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> getAutoConstructed(Class<T> type) {
    return (Optional<T>) ac.getAllInstances().stream()
      .filter(inst -> type.isAssignableFrom(inst.getClass()))
      .findFirst();
  }
}
