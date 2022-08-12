package me.blvckbytes.bblibutil.logger;

import lombok.Getter;
import lombok.Setter;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;
import me.blvckbytes.bblibdi.AutoInjectLate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.PrintWriter;
import java.io.StringWriter;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 04/22/2022

  Logs all events to the server's console and automatically prepends
  the level's configured prefix

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
public class ConsoleSenderLogger implements ILogger {

  @Setter
  private boolean debugMode;

  @Getter
  private final ILogColorSupplier colorSupplier;
  private final String prefix;

  @AutoInjectLate
  private ObjectStringifier stringifier;

  public ConsoleSenderLogger(
    @AutoInject ILogColorSupplier colorSupplier,
    @AutoInject JavaPlugin plugin
  ) {
    this.colorSupplier = colorSupplier;
    this.prefix = colorSupplier.getLogColor(LogColor.PREFIX) + plugin.getName() + " §8| §7";
  }

  private void log(String message) {
    Bukkit.getConsoleSender().sendMessage(this.prefix + message);
  }

  @Override
  public void logInfo(String message) {
    log(colorSupplier.getLogColor(LogColor.INFO) + message);
  }

  @Override
  public void logDebug(String message) {
    if (!debugMode)
      return;

    log(colorSupplier.getLogColor(LogColor.DEBUG) + message);
  }

  @Override
  public void logDebug(Object o, int depth) {
    // Stringifier unavailable (yet)
    if (stringifier == null)
      return;

    logDebug(stringifier.stringifyObject(o, depth));
  }

  @Override
  public void logError(Exception e) {
    try {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);

      e.printStackTrace(pw);
      pw.close();
      sw.close();

      logError(sw.toString());
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void logError(String message) {
    log(colorSupplier.getLogColor(LogColor.ERROR) + message);
  }
}
