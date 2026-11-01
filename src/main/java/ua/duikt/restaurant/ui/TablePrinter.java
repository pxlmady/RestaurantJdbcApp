package ua.duikt.restaurant.ui;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Друкує список рядків у вигляді вирівняної текстової таблиці. */
public final class TablePrinter {

    private TablePrinter() {
    }

    public static void print(PrintStream out, List<String> headers, List<List<Object>> rows) {
        if (rows.isEmpty()) {
            out.println("  (немає записів)");
            return;
        }
        int[] width = new int[headers.size()];
        for (int i = 0; i < width.length; i++) width[i] = headers.get(i).length();
        String[][] cells = new String[rows.size()][];
        for (int r = 0; r < rows.size(); r++) {
            cells[r] = new String[width.length];
            for (int i = 0; i < width.length; i++) {
                cells[r][i] = format(rows.get(r).get(i));
                width[i] = Math.max(width[i], cells[r][i].length());
            }
        }
        out.println(line(headers.toArray(new String[0]), width));
        StringBuilder sep = new StringBuilder();
        for (int w : width) sep.append("-".repeat(w)).append("-+-");
        out.println(sep.substring(0, sep.length() - 3));
        for (String[] row : cells) out.println(line(row, width));
        out.println("  Рядків: " + rows.size());
    }

    private static String line(String[] values, int[] width) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(String.format("%-" + width[i] + "s", values[i]));
            if (i < values.length - 1) sb.append(" | ");
        }
        return sb.toString();
    }

    private static String format(Object v) {
        if (v == null) return "NULL";
        if (v instanceof LocalDateTime dt) return ConsoleIO.DATE_TIME.format(dt);
        if (v instanceof BigDecimal bd) return bd.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        return v.toString();
    }
}
