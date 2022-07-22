package me.blvckbytes.bblibutil.logger;

import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 04/24/2022

  Stringify all declared fields of an object to make it easily logable in
  a recursive fashion while limiting the maximum depth.
*/
@AutoConstruct
public class ObjectStringifier {

  private final ILogger logger;
  private final String otherColor;
  private final String valueColor;
  private final String symbolColor;
  private final String errorColor;

  public ObjectStringifier(
    @AutoInject ILogger logger,
    @AutoInject ILogColorSupplier colorSupplier
  ) {
    this.logger = logger;
    this.otherColor = colorSupplier.getLogColor(LogColor.OSTR_OTHER);
    this.valueColor = colorSupplier.getLogColor(LogColor.OSTR_VALUE);
    this.symbolColor = colorSupplier.getLogColor(LogColor.OSTR_SYMBOL);
    this.errorColor = colorSupplier.getLogColor(LogColor.OSTR_ERROR);
  }

  //=========================================================================//
  //                                   API                                   //
  //=========================================================================//

  /**
   * Turn an object into a human readable string, if possible
   * @param o Object to stringify
   * @param depth How deep to stringify list or array elements if they're objects
   *
   * @return String representation or null if it's an object
   */
  public String stringifyObject(Object o, int depth) {
    // Directly stringify null values
    if (o == null)
      return "null";

    Class<?> c = o.getClass();

    // Return the string wrapped in quotes
    // Add the value color after the actual value to account for colored strings
    // Also escape backslashes for better readability
    if (o instanceof String)
      return "\"" + stripEscapeSequences(o.toString()) + valueColor + "\"";

    // Just stringify primitives (and their wrappers) or enums
    if (
      c.isPrimitive()
        || c.isEnum()
        || o instanceof Integer
        || o instanceof Long
        || o instanceof Double
        || o instanceof Float
        || o instanceof Boolean
        || o instanceof Byte
        || o instanceof Short
        || o instanceof Character
    )
      return o.toString();

    // Is an array or a list, format as [...]
    boolean isList = List.class.isAssignableFrom(c);
    if (c.isArray() || isList) {
      StringBuilder sb = new StringBuilder(otherColor + "[");

      // Try to get into the "iterable"
      List<?> list;
      try {
        list = (List<?>) (isList ? o : Arrays.asList((Object[]) o));
      } catch (Exception e) {
        list = List.of(
          otherColor + "<" +
          errorColor + "ERROR" +
          otherColor + ">"
        );
      }

      // Iterate list or list from array
      for (int i = 0; i < list.size(); i++) {
        Object curr = list.get(i);
        Object res = stringifyObject(curr, depth);

        // Could not stringify locally
        if (res == null) {
          String tarName = curr.getClass().getSimpleName();

          // Depth remains, try to reach out to the object stringifier
          if (depth > 0) {
            res = (
              symbolColor + tarName +
              otherColor + "(" +
              valueColor + stringifyObjectProperties(curr, depth - 1) +
              otherColor + ")"
            );
          }

          // No more depth, use placeholder
          else {
            res = (
              otherColor + "<" +
              symbolColor + tarName +
              otherColor + ">"
            );
          }
        }

        // Call recursively until a scalar value occurs
        sb
          .append(i == 0 ? "" : otherColor + ", ")
          .append(valueColor).append(res);
      }

      // Reset color at the end
      sb.append(otherColor).append("]").append("§r");
      return sb.toString();
    }

    // Is an optional
    if (o instanceof Optional<?>) {
      Optional<?> opt = (Optional<?>) o;

      // Empty, just write placeholder
      if (opt.isEmpty()) {
        return (
          otherColor + "<" +
          valueColor + "EMPTY" +
          otherColor + ">"
        );
      }

      // Stringify it's contents
      else
        o = opt.get();
    }

    return formatStringifiedObjectProperties(o, depth);
  }

  //=========================================================================//
  //                                Utilities                                //
  //=========================================================================//

  /**
   * Formats the result of the call to {@link #stringifyObjectProperties(Object, int)}
   * @param o Object to stringify
   * @param depth Depth to stringify until
   * @return Formatted result
   */
  private String formatStringifiedObjectProperties(Object o, int depth) {
    String sub = stringifyObjectProperties(o, depth);

    // Couldn't stringify, use placeholder
    if (sub == null) {
      return (
        otherColor + "<" +
        symbolColor + o.getClass().getSimpleName() +
        otherColor + ">"
      );
    }

    return (
      symbolColor + o.getClass().getSimpleName() +
      otherColor + "(" +
      valueColor + sub +
      otherColor + ")"
    );
  }

  /**
   * Stringify an object's properties into a comma separated list
   * @param o Object to query
   * @param depth Levels of recursion to allow when stringifying object fields
   * @return Built comma separated list string or null if depth is used up
   */
  private String stringifyObjectProperties(Object o, int depth) {
    // Depth used up
    if (depth <= 0)
      return null;

    // Object is null
    if (o == null)
      return "null";

    StringBuilder props = new StringBuilder();

    try {
      Class<?> cl = o.getClass();
      Field[] fields = listFields(cl);

      // This class doesn't contain any fields, search for superclasses
      while (
        // No fields yet
        fields.length == 0 &&

        // Superclass available
        cl.getSuperclass() != null
      ) {
        // Navigate into superclass and list it's fields
        cl = cl.getSuperclass();

        // Skip static fields
        fields = listFields(cl);
      }

      // Loop all fields of this packet and add them to a comma separated list
      for (int i = 0; i < fields.length; i++) {
        Field f = fields[i];

        // Also access private fields, of course
        try {
          f.setAccessible(true);
        } catch (Exception e) {
          // Could not access this field, skip it
          // I am intentionally not logging exceptions here, as it may pollute fast logs
          continue;
        }

        // Call to resolve this object into a simple string (no object field walking)
        Object tar = f.get(o);
        String str = stringifyObject(tar, depth - 1);

        // Not an "easy" stringify
        if (str == null)
          return formatStringifiedObjectProperties(o, depth);

        // Stringify and append with leading comma, if applicable
        props
          .append(i == 0 ? "" : otherColor + ", ")
          .append(valueColor).append(str);
      }

    } catch (Exception e) {
      logger.logError(e);
    }

    // Re-set the colors at the end
    return props + "§r";
  }

  /**
   * List all fields that are to be printed from a class
   * @param c Target class
   * @return Fields to print
   */
  private Field[] listFields(Class<?> c) {
    return Arrays.stream(c.getDeclaredFields())
      .filter(f -> !Modifier.isStatic(f.getModifiers()))
      .toArray(Field[]::new);
  }

  /**
   * Strip all escape sequences from a string
   * @param input Input string
   * @return String without escape sequences
   */
  private String stripEscapeSequences(String input) {
    StringBuilder sb = new StringBuilder();

    // Filter characters
    for (char c : input.toCharArray()) {
      if (c == '\n' || c == '\t') {
        sb.append(otherColor).append("<")
          .append(valueColor).append(c == '\n' ? "nl" : "tab")
          .append(otherColor).append(">");
      }

      // Strip escape sequences
      if (c < 32)
        continue;

      // Append this char to the result
      sb.append(c);
    }

    return sb.toString();
  }
}
