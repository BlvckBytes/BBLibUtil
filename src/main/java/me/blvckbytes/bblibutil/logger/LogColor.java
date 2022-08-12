package me.blvckbytes.bblibutil.logger;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/22/2022

  Represents all different types of color a logger implementation
  requires to colorize it's output on different levels.

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
public enum LogColor {
  // Log levels
  INFO,
  DEBUG,
  ERROR,

  // Misc
  PREFIX,

  // Object stringifier
  OSTR_VALUE,
  OSTR_SYMBOL,
  OSTR_OTHER,
  OSTR_ERROR
}
