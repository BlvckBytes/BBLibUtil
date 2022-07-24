package me.blvckbytes.bblibutil;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/17/2022

  Wraps all parameters a chat prompt requires to be processed.
*/
@Setter
@Getter
@AllArgsConstructor
public class ChatPrompt {
  private @Nullable Consumer<String> chat;
  private Map<String, Runnable> actions;
  private @Nullable String expiredMessage;
  private boolean expired;
}
