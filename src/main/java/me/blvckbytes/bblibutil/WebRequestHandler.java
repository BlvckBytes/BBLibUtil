package me.blvckbytes.bblibutil;

import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;
import me.blvckbytes.bblibutil.logger.ILogger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/22/2022

  Handles dispatching web requests asynchronously.
*/
@AutoConstruct
public class WebRequestHandler {

  private final APlugin plugin;
  private final ILogger logger;

  public WebRequestHandler(
    @AutoInject APlugin plugin,
    @AutoInject ILogger logger
  ) {
    this.plugin = plugin;
    this.logger = logger;
  }

  /**
   * Perform a GET request on a given URL
   * @param url URL to request
   * @param result Result callback, returns a status of -1 on internal errors
   * @param synchronize Whether to synchronize the callback with the main thread
   */
  public void get(URL url, BiConsumer<Integer, @Nullable String> result, boolean synchronize) {
    plugin.runTaskAsynchronously(() -> {
      try {
        // Create a new GET request
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        // Check that the status code actually represents success in order to avoid exceptions
        int code = connection.getResponseCode();
        if (code != 200)
          optionalSynchronize(() -> result.accept(code, null), synchronize);

        // Read the body contents into a string
        InputStreamReader sr = new InputStreamReader(connection.getInputStream());
        BufferedReader br = new BufferedReader(sr);
        String body = br.lines().collect(Collectors.joining());

        br.close();
        sr.close();

        optionalSynchronize(() -> result.accept(code, body), synchronize);
      } catch (Exception e) {
        logger.logError(e);
        optionalSynchronize(() -> result.accept(-1, null), synchronize);
      }
    });
  }

  /**
   * Optionally synchronizes the given runnable with the main thread
   * @param r Runnable to synchronize
   * @param synchronize Whether to synchronize
   */
  private void optionalSynchronize(Runnable r, boolean synchronize) {
    if (synchronize) {
      plugin.runTask(r);
      return;
    }

    r.run();
  }
}
