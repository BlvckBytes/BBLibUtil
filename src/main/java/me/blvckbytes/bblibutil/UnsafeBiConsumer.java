package me.blvckbytes.bblibutil;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/24/2022

  Represents a bi-consumer function which may throw.

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
@FunctionalInterface
public interface UnsafeBiConsumer<I1, I2> {
  void accept(I1 val1, I2 val2) throws Exception;
}
