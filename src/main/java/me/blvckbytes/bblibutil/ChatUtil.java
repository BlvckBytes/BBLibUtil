package me.blvckbytes.bblibutil;

import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 04/28/2022

  Utility methods that targets the chat and messages sent to it.
*/
@AutoConstruct
public class ChatUtil implements Listener {

  // Pattern to check if a command matches the UUID format (and thus is a temporary command)
  private static final Pattern UUID_PATTERN = Pattern.compile("[a-f\\d]{8}(?:-[a-f\\d]{4}){4}[a-f\\d]{8}");

  private final Map<Player, List<ChatPrompt>> chatPrompts;
  private final APlugin plugin;

  public ChatUtil(
    @AutoInject APlugin plugin
  ) {
    this.chatPrompts = new HashMap<>();
    this.plugin = plugin;
  }

  //=========================================================================//
  //                                    API                                  //
  //=========================================================================//

  /**
   * Begin a new prompt session for a given player
   * @param p Target player
   * @param chat Chat message listener, null if no chat message is required
   * @param prepend Message to prepend to the buttons
   * @param expired Message to print if the prompt already expired
   * @param actions Action buttons to append to the message (text, hover, action)
   * @return Prompt handle
   */
  public ChatPrompt beginPrompt(
    Player p,
    @Nullable Consumer<String> chat,
    String prepend,
    @Nullable String expired,
    @Nullable List<Triple<String, @Nullable String, Runnable>> actions
  ) {
    TextComponent head = new TextComponent(prepend);
    Map<String, Runnable> actionButtons = new HashMap<>();

    if (actions == null)
      actions = new ArrayList<>();

    for (Triple<String, @Nullable String, Runnable> action : actions) {
      // Create the component from it's displayed text, space buttons out
      TextComponent btn = new TextComponent(" " + action.getA());
      String actionCommand = UUID.randomUUID().toString();

      // Bind the temporary command to it's click listener
      btn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + actionCommand));

      // Show hover text, if provided
      if (action.getB() != null)
        btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{ new TextComponent(action.getB()) }));

      // Append to head
      head.addExtra(btn);

      actionButtons.put(actionCommand, action.getC());
    }

    // Register the prompt
    if (!chatPrompts.containsKey(p))
      chatPrompts.put(p, new ArrayList<>());

    ChatPrompt prompt = new ChatPrompt(chat, actionButtons, expired, false);
    chatPrompts.get(p).add(prompt);

    // Send out the prompt component
    p.spigot().sendMessage(head);
    return prompt;
  }

  /**
   * Expire a previously started prompt
   * @param prompt Prompt handle
   */
  public void expirePrompt(ChatPrompt prompt) {
    prompt.setExpired(true);
  }

  /**
   * Checks whether the given player has any active (pending) prompts
   * @param p Target player
   */
  public boolean hasActivePrompt(Player p) {
    return chatPrompts.getOrDefault(p, new ArrayList<>()).stream().anyMatch(prompt -> !prompt.isExpired());
  }

  //=========================================================================//
  //                                 Listener                                //
  //=========================================================================//

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPreCommand(PlayerCommandPreprocessEvent e) {
    String message = e.getMessage();
    Player p = e.getPlayer();

    // Not a unique command
    if (message.length() == 0 || message.contains(" "))
      return;

    String command = message.substring(1);

    // Command needs to be a unique ID
    if (!UUID_PATTERN.matcher(command).matches())
      return;

    // Cancel all commands that are UUIDs - they're only internal
    e.setCancelled(true);

    // Has no active prompts yet
    List<ChatPrompt> prompts = chatPrompts.get(p);
    if (prompts == null)
      return;

    // Search target prompt by command string
    ChatPrompt target = prompts.stream()
      .filter(prompt -> prompt.getActions().containsKey(command))
      .findFirst()
      .orElse(null);

    // Command didn't target an existing prompt
    if (target == null)
      return;

    // Prompt already expired
    if (target.isExpired()) {
      if (target.getExpiredMessage() != null)
        p.sendMessage(target.getExpiredMessage());
      return;
    }

    // Dispatch the callback and expire the prompt
    target.getActions().get(command).run();
    target.setExpired(true);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(AsyncPlayerChatEvent e) {
    String message = e.getMessage();
    Player p = e.getPlayer();

    // Has no active prompts yet
    List<ChatPrompt> prompts = chatPrompts.get(p);
    if (prompts == null)
      return;

    // Search the first non-expired chat prompt
    ChatPrompt target = prompts.stream()
      .filter(prompt -> !prompt.isExpired() && prompt.getChat() != null)
      .findFirst().orElse(null);

    // No pending prompt remaining
    if (target == null || target.getChat() == null)
      return;

    // Dispatch the callback and expire the prompt
    e.setCancelled(true);
    target.setExpired(true);

    // Dispatch synchronously
    plugin.runTask(() -> target.getChat().accept(message));
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    // Unregister all prompts on quit
    chatPrompts.remove(e.getPlayer());
  }
}
