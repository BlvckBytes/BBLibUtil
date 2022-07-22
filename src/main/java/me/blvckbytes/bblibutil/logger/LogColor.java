package me.blvckbytes.bblibutil.logger;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/22/2022

  Represents all different types of color a logger implementation
  requires to colorize it's output on different levels.
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
