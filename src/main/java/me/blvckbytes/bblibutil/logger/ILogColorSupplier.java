package me.blvckbytes.bblibutil.logger;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/22/2022

  Implementations of this interface supply the logger with all required
  colors in order to differentiate log levels.
*/
public interface ILogColorSupplier {

  /**
   * Get a log color string by it's name
   * @param color Name of the target color
   * @return String containing the color using native notation
   */
  default String getLogColor(LogColor color) {
    switch (color) {
      case INFO:
        return "§a";

      case DEBUG:
        return "§6";

      case OSTR_ERROR:
      case ERROR:
        return "§c";

      case OSTR_SYMBOL:
      case PREFIX:
        return "§5";

      case OSTR_VALUE:
        return "§d";

      default:
      case OSTR_OTHER:
        return "§7";
    }
  }
}