package me.blvckbytes.bblibutil;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 05/02/2022

  Represents a lambda function which may throw.
*/
@FunctionalInterface
public interface UnsafeBiFunction<I1, I2, O> {
  O apply(I1 val1, I2 val2) throws Exception;
}
