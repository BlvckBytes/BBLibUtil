package me.blvckbytes.bblibutil;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/24/2022

  Represents a consumer function which may throw.
*/
@FunctionalInterface
public interface UnsafeConsumer<I> {
  void accept(I val) throws Exception;
}
