/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.contrib;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LoggingOutputStream extends OutputStream {

    /**
     * Default number of bytes in the buffer.
     */
    private static final int DEFAULT_BUFFER_LENGTH = 2048;

    /**
     * Indicates stream state.
     */
    private boolean hasBeenClosed = false;

    /**
     * Internal buffer where data is stored.
     */
    private byte[] buf;

    /**
     * Number of valid bytes in the buffer.
     */
    private int count;

    /**
     * Remembers the size of the buffer.
     */
    private int curBufLength;

    /**
     * The logger to write to.
     */
    private Logger log;

    /**
     * The log level.
     */
    private Level level;

    /**
     * Marker to note when we are in a method
     */
    private boolean in_write;

    /**
     * Marker to note when we are in a method
     */
    private boolean in_flush;

    /**
     * Lock
     */
    private Object lock;

    /**
     * Creates the Logging instance to flush to the given logger.
     *
     * @param log         the Logger to write to
     * @param level       the log level
     * @throws IllegalArgumentException in case if one of arguments
     *                                  is  null.
     */
    public LoggingOutputStream(final Logger log,
                               final Level level)
            throws IllegalArgumentException {
        if (log == null || level == null) {
            throw new IllegalArgumentException(
                    "Logger or log level must be not null");
        }
        this.log = log;
        this.level = level;
        curBufLength = DEFAULT_BUFFER_LENGTH;
        buf = new byte[curBufLength];
        count = 0;
        in_write = in_flush = false;
        lock = new Object();
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * @param b the byte to write
     * @throws IOException if an I/O error occurs.
     */
    public void write(final int b) throws IOException {
        if (hasBeenClosed) {
            // XXX that might create a loop:-(
            // throw new IOException("The stream has been closed.");
            return;
        }
        // don't log nulls
        if (b == 0) {
            return;
        }

        synchronized (lock) {
            if (in_write) {
                return;
            }
            in_write = true;
         
            // would this be writing past the buffer?
            if (count == curBufLength) {
                // grow the buffer
                final int newBufLength = curBufLength +
                        DEFAULT_BUFFER_LENGTH;
                final byte[] newBuf = new byte[newBufLength];
                System.arraycopy(buf, 0, newBuf, 0, curBufLength);
                buf = newBuf;
                curBufLength = newBufLength;
            }

            buf[count] = (byte) b;
            count++;

            in_write = false;
        }
    }

    /**
     * Flushes this output stream and forces any buffered output
     * bytes to be written out.
     */
    public void flush() {
        synchronized (lock) {
            if (in_flush) {
                return;
            }
         
            if (count == 0) {
                return;
            }
            in_flush = true;

            final byte[] bytes = new byte[count];
            System.arraycopy(buf, 0, bytes, 0, count);
            String str = new String(bytes);

            if (!str.matches("^[ \\t\\n\\r]*$")) {
                log.log(level, str);
            }

            count = 0;
            in_flush = false;
        }
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream.
     */
    public void close() {
        flush();
        hasBeenClosed = true;
    }
}
