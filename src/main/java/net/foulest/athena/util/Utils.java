package net.foulest.athena.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    public static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
    public static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);
    public static final BigDecimal BILLION = BigDecimal.valueOf(1_000_000_000);
    private static final NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance();
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.##");

    public static String formatInteger(double number) {
        return INTEGER_FORMAT.format(new BigDecimal(number).toBigInteger());
    }

    public static double formatDouble(double number) {
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            return 0.0;
        }
        return Double.parseDouble(DOUBLE_FORMAT.format(number));
    }

    public static String join(String[] data, String d) {
        return String.join(d, data);
    }

    private static String cleanNumberString(String data) {
        return join(data.trim().split(","), "");
    }

    private static boolean isParseable(String data) {
        return !(data == null || data.equals("N/A") || data.equals("-") || data.isEmpty() || data.equals("nan"));
    }

    public static BigDecimal getBigDecimal(String data) {
        if (!isParseable(data)) {
            return null;
        }

        try {
            data = cleanNumberString(data);
            char lastChar = data.charAt(data.length() - 1);
            BigDecimal multiplier = BigDecimal.ONE;

            switch (lastChar) {
                case 'B':
                    data = data.substring(0, data.length() - 1);
                    multiplier = BILLION;
                    break;
                case 'M':
                    data = data.substring(0, data.length() - 1);
                    multiplier = MILLION;
                    break;
                case 'K':
                    data = data.substring(0, data.length() - 1);
                    multiplier = THOUSAND;
                    break;
                default:
                    break;
            }
            return new BigDecimal(data).multiply(multiplier);
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public static Long getLong(String data) {
        if (!isParseable(data)) {
            return null;
        }

        try {
            data = cleanNumberString(data);
            return Long.parseLong(data);
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public static BigDecimal getPercent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || numerator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return numerator.divide(denominator, 4, RoundingMode.HALF_EVEN)
                .multiply(HUNDRED).setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Calendar parseHistDate(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        try {
            if (isParseable(date)) {
                Calendar c = Calendar.getInstance();
                c.setTime(format.parse(date));
                return c;
            }
        } catch (ParseException ignored) {
        }
        return null;
    }

    public static boolean isFilePath(String input) {
        return input.contains(".") || input.contains("\\") || input.contains("/");
    }

    public static String getURLParameters(Map<String, String> params) {
        return params.entrySet().stream().map(entry -> {
            try {
                String key = URLEncoder.encode(entry.getKey(), "UTF-8");
                String value = URLEncoder.encode(entry.getValue(), "UTF-8");
                return key + "=" + value;
            } catch (UnsupportedEncodingException e) {
                // Handle the exception or rethrow it as a RuntimeException
                throw new RuntimeException("UTF-8 encoding not supported", e);
            }
        }).collect(Collectors.joining("&"));
    }

}
