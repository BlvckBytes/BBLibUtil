package me.blvckbytes.bblibutil;

import me.blvckbytes.bblibdi.AutoConstruct;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 05/13/2022

  Provides utilities to handle time in human readable formats.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published
  by the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
@AutoConstruct
public class TimeUtil {

  // Conversion constants for quick access
  private static final long MIN_S   = 60,
    HOUR_S  = MIN_S  * 60,
    DAY_S   = HOUR_S * 24,
    WEEK_S  = DAY_S  * 7,
    MONTH_S = WEEK_S * 30;

  // Lookup "table" for timespan characters and timespan durations, in ascending order
  private static final char[] spanC = new char[] { 's', 'm', 'h', 'd', 'w', 'm' };
  private static final long[] spanD = new long[] { 1, MIN_S, HOUR_S, DAY_S, WEEK_S, MONTH_S };

  /**
   * Parse a duration with the format of: (multiplier)(span), where multiple
   * entries of those tuples may be in the same duration string
   * @param duration Duration string to parse
   * @return The parsed duration in seconds on success, -1 on malformed inputs
   */
  public int parseDuration(String duration) {
    int res = 0;

    StringBuilder numBuf = new StringBuilder();
    for (int i = 0; i < duration.length(); i++) {
      char c = duration.charAt(i);

      // Collect digits into numbers
      if (c >= '0' && c <= '9') {

        // Durations cannot end with a digit
        if (i == duration.length() - 1)
          return -1;

        numBuf.append(c);
        continue;
      }

      int multiplier;
      try {
        multiplier = Integer.parseInt(numBuf.toString());
        numBuf.setLength(0);
      }

      // Invalid non-integer multiplier specified
      catch (NumberFormatException e) {
        return -1;
      }

      // Check if this char is a valid span character, ignore casing
      int spanCharInd = -1;
      for (int j = 0; j < spanC.length; j++) {
        if (Character.toLowerCase(spanC[j]) == Character.toLowerCase(c)) {
          spanCharInd = j;
          break;
        }
      }

      // Invalid character encountered
      if (spanCharInd < 0)
        return -1;

      // Add this duration to the result sum
      res += multiplier * spanD[spanCharInd];
    }

    return res;
  }

  /**
   * Format a duration in seconds to a time string
   * containing months, weeks, days, hours, minutes and seconds
   * @param duration Duration in seconds
   * @return Formatted duration string
   */
  public String formatDuration(long duration) {
    return formatDuration(duration, false, false);
  }

  /**
   * Format a duration in seconds to a time string
   * containing months, weeks, days, hours, minutes and seconds
   * @param duration Duration in seconds
   * @param skipSeconds Whether to skip displaying seconds
   * @param colon Whether to use colons (:) as a separator and skip span characters
   * @return Formatted duration string
   */
  public String formatDuration(long duration, boolean skipSeconds, boolean colon) {
    StringBuilder sb = new StringBuilder();
    long durS = duration;

    // Use min to not crash on uneven numbers of array entries
    for (int i = Math.min(spanC.length, spanD.length) - 1; i >= 0; i--) {
      // Duration of the current span in seconds
      long currD = spanD[i];

      // Get the current quotient and store the remainder for the next iteration
      long currQuot = durS / currD;
      durS = durS % currD;

      // Skip seconds
      if (skipSeconds && i == 0)
        continue;

      // Do not display zero quotients, except for seconds, but
      // don't display zero seconds if any other span has been >0
      if (currQuot == 0) {
        // Skipping seconds and minutes are empty but there's
        // nothing else in the builder, so print 0m, else skip
        if (!(skipSeconds && sb.length() == 0 && i == 1))
          continue;
      }

      sb.append(colon ? ':' : ' ');
      sb.append(currQuot);
      sb.append(colon ? "" : spanC[i]);
    }

    // Remove leading colon and trim whitespace, if applicable
    String res = sb.toString().trim();
    return res.startsWith(":") ? res.substring(1) : res;
  }
}
