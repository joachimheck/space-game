package org.heckcorp.spacegame;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    @Override
    public String format(LogRecord record)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(record.getLevel()).append(" ");
        sb.append(record.getMessage());
        sb.append("\n  ");
        String pattern = "yyyy-MM-dd HH:mm:ss.SS";
        DateFormat df = new SimpleDateFormat(pattern);
        sb.append(df.format(new Date(record.getMillis()))).append(" ");
        sb.append("Thread: ").append(record.getLongThreadID()).append(" ");
        sb.append(record.getSourceClassName()).append(" ");
        sb.append(record.getSourceMethodName());
        sb.append("\n");
        return sb.toString();
    }

}
