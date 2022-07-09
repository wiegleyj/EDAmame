package com.cyte.edamame;

import java.text.MessageFormat;
import java.util.logging.LogRecord;
import javafx.scene.control.TextArea;

/**
 *
 * @author jeffw
 */
public class TextAreaHandler extends java.util.logging.Handler {
    private TextArea textArea;

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
