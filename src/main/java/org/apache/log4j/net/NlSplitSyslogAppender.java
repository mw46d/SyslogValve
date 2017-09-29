package org.apache.log4j.net;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.spi.LoggingEvent;

public class NlSplitSyslogAppender extends SyslogAppender {
  // append() mostly the super class version, except the packet is split into
  // lines and handled separately.
  public
  void append(LoggingEvent event) {
    if (!isAsSevereAsThreshold(event.getLevel())) {
      return;
    }

    // We must not attempt to append if sqw is null.
    if (sqw == null) {
      // XXX We might redirect stdout/stderr back here. In that case any outputs might
      // create a loop:-( So no error messages from here:-(
      // errorHandler.error("No syslog host is set for SyslogAppedender named \"" +
      //                    this.name + "\".");
      return;
    }

    // Split the message and filter out all empty lines etc.
    String packet = String.valueOf(event.getMessage());
    String lines[] = packet.split("\\r?\\n");
    List<String> filteredLines = new ArrayList<String>(); 

    for(int i = 0; i < lines.length; i++) {
      if (lines[i].length() > 0) {
        // Try to remove the useless ANSI color codes from the Rails messages.
        String newLine = lines[i].replaceAll("\\x1b\\[[0-9;]+m", "");

        if (!newLine.matches("^[\\t ]*$")) {
          // That's a line, we are actually interested in
          filteredLines.add(newLine);
        }
      }
    }

    // Now handle the interesting lines
    for (Iterator<String> it = filteredLines.iterator(); it.hasNext(); ) {
      String line = it.next();

      // Create a new event for each line.
      // Only the last one get's the ThrowableInformation!!
      LoggingEvent newEvent = new LoggingEvent(event.getFQNOfLoggerClass(),
          event.getLogger(), event.getTimeStamp(), event.getLevel(), line,
          event.getThreadName(),
          (!it.hasNext() ? event.getThrowableInformation() : null),
          event.getNDC(), event.getLocationInformation(),
          event.getProperties());

      super.append(newEvent);
    }
  }
}
