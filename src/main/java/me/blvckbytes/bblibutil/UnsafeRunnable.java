package me.blvckbytes.bblibutil;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 05/02/2022

  Represents a runnable which may throw.
*/
@FunctionalInterface
public interface UnsafeRunnable {
  void run() throws Exception;
}
