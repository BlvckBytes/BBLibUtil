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
