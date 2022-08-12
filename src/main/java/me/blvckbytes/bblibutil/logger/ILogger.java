package me.blvckbytes.bblibutil.logger;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 04/22/2022

  Public interfaces to handle logging of events on different levels.

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
public interface ILogger {

  /**
   * Log a message on the information level
   * @param message Message to log
   */
  void logInfo(String message);

  /**
   * Log a message on the debug level
   * @param message Message to log
   */
  void logDebug(String message);

  /**
   * Log a stringified object on the debug level
   * @param o Object to stringify
   * @param depth Maximum depth to stringify until
   */
  void logDebug(Object o, int depth);

  /**
   * Log an exception on the error level
   * @param e Exception to log
   */
  void logError(Exception e);

  /**
   * Log a message on the error level
   * @param message Message to log
   */
  void logError(String message);

  /**
   * Set the current debug enable mode
   * @param mode Mode to set
   */
  void setDebugMode(boolean mode);
}
