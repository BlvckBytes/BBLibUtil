package me.blvckbytes.bblibutil.logger;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/22/2022

  Implementations of this interface supply the logger with all required
  colors in order to differentiate log levels.

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