package me.blvckbytes.bblibutil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/15/2022

  Wraps two linked values.
*/
@Setter
@Getter
@AllArgsConstructor
public class Tuple<A, B> {
  private A a;
  private B b;
}
