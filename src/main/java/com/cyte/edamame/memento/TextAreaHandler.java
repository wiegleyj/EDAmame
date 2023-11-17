/*
 * Copyright (c) 2022-2023. Jeff Wiegley
 * This work is distributed and licensed under the AGPLv3 license.
 * Terms and conditions of the AGPLv3 are documented
 * in the LICENSE.TXT file included in the EDAmame sources.
 */

package com.cyte.edamame.memento;

import java.text.MessageFormat;
import java.util.logging.LogRecord;

import com.cyte.edamame.EDAmame;
import javafx.scene.control.TextArea;

/**
 * Logging in {@link EDAmame} is performed primarily through a specific {@link javafx.scene.control.TabPane}
 * {@link javafx.scene.control.Tab} named "Log". Any information logged is directed to a {@link TextArea} in
 * the log Editor_Tab instead of to stdout. {@link TextAreaHandler} is the class that provides the necessary
 * functionality for a given {@link TextArea}.
 *
 * @author Jeff Wiegley, Ph.D.
 * @author jeffrey.wiegley@gmail.com
 */
public class TextAreaHandler extends java.util.logging.Handler {
    /**
     * The TextArea that this handler appends log requests to.
     */
    private TextArea textArea;

    /**
     * Construct a {@link TextAreaHandler} suitable for appending log requests to the given {@link TextArea}.
     *
     * @param target The {@link TextArea} to append the messages to.
     */
    public TextAreaHandler(TextArea target) {
        textArea = target;
    }

    @Override
    public void publish(final LogRecord record) {
        if (textArea != null) {
            textArea.appendText(MessageFormat.format(record.getMessage(), record.getParameters()));
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        textArea = null;
    }
}
