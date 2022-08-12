package me.blvckbytes.bblibutil;

import me.blvckbytes.bblibdi.AutoConstructer;
import me.blvckbytes.bblibutil.logger.ILogColorSupplier;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/15/2022

  Adding common routines which need direct access to the plugin
  reference by decorating said instance.

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
   * Run a task on the next tick always (no delay when disabling)
   * @param task Task to run
   */
  public void runTaskAlways(Runnable task) {
    runTaskAlways(task, 0);
  }

  /**
   * Run a task after a certain amount of ticks elapsed
   * @param task Task to run
   * @param delay Delay to run this task in ticks
   */
  public BukkitTask runTask(Runnable task, long delay) {
    if (disabling)
      return null;

    if (delay == 0)
      return Bukkit.getScheduler().runTask(this, task);

    return Bukkit.getScheduler().runTaskLater(this, task, delay);
  }

  /**
   * Run a task after a certain amount of ticks elapsed always (no delay when disabling)
   * @param task Task to run
   * @param delay Delay to run this task in ticks
   */
  public BukkitTask runTaskAlways(Runnable task, long delay) {
    if (disabling) {
      task.run();
      return null;
    }

    return runTask(task, delay);
  }

  /**
   * Run a task asynchronously on the next tick
   * @param task Task to run
   */
  public BukkitTask runTaskAsynchronously(Runnable task) {
    if (disabling)
      return null;

    return Bukkit.getScheduler().runTaskAsynchronously(this, task);
  }

  /**
   * Run a task asynchronously on the next tick always (sync no delay when disabling)
   * @param task Task to run
   */
  public BukkitTask runTaskAsynchronouslyAlways(Runnable task) {
    if (disabling) {
      task.run();
      return null;
    }

    return runTaskAsynchronously(task);
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> getAutoConstructed(Class<T> type) {
    return (Optional<T>) ac.getAllInstances().stream()
      .filter(inst -> type.isAssignableFrom(inst.getClass()))
      .findFirst();
  }
}
