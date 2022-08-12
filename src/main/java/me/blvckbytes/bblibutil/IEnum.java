package me.blvckbytes.bblibutil;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/23/2022

  Forces implementing enums to provide basic functionality which
  aims at improving efficiency by standardizing reoccurring actions.

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
public interface IEnum<T extends Enum<?>> {

  /**
   * Get the next enum value in the enum's ordinal sequence
   * and wrap around if performed on the last value
   * @return Next enum value
   */
  IEnum<T> nextValue();

  /**
   * Get the name of the current enum constant
   * @return Name of the constant
   */
  String name();

  /**
   * Get the ordinal integer value of the current enum constant
   * @return Ordinal integer value
   */
  int ordinal();

  /**
   * Lists all available enum values, including the current constant
   * @return Array of values
   */
  IEnum<T>[] listValues();

}
