package me.blvckbytes.bblibutil;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/23/2022

  Forces implementing enums to provide basic functionality which
  aims at improving efficiency by standardizing reoccurring actions.
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
