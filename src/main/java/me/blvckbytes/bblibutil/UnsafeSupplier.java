package me.blvckbytes.bblibutil;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/24/2022

  Represents a supplier function which may throw.
*/
@FunctionalInterface
public interface UnsafeSupplier<O> {
  O get() throws Exception;
}
