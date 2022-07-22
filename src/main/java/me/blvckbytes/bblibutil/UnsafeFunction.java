package me.blvckbytes.bblibutil;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 05/02/2022

  Represents a lambda function which may throw.
*/
@FunctionalInterface
public interface UnsafeFunction<I, O> {
  O apply(I val) throws Exception;
}
