package net.foulest.athena.util;

import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

    public static final BigDecimal HUNDRED = new BigDecimal(100);
    public static final BigDecimal THOUSAND = new BigDecimal(1000);
    public static final BigDecimal MILLION = new BigDecimal(1000000);
    public static final BigDecimal BILLION = new BigDecimal(1000000000);

    public static AnsiFormat getColorBeta(Double number) {
        AnsiFormat formatting;

        if (number > 1.0) {
            formatting = new AnsiFormat(Attribute.RED_TEXT());
        } else {
            formatting = new AnsiFormat(Attribute.GREEN_TEXT());
        }

        return formatting;
    }

    public static AnsiFormat getColorPBRatio(Double number) {
        AnsiFormat formatting;

        if (number > 0.0 && number <= 3.0) {
            formatting = new AnsiFormat(Attribute.GREEN_TEXT());
        } else if (number > 0.0) {
            formatting = new AnsiFormat(Attribute.YELLOW_TEXT());
        } else {
            formatting = new AnsiFormat(Attribute.RED_TEXT());
        }

        return formatting;
    }
    public static Attribute getColorDebtToEquity(Double number) {
        Attribute formatting;

        if (number > 1.0 && number < 2.0) {
            formatting = Attribute.GREEN_TEXT();
        } else if (number <= 3.0) {
            formatting = Attribute.YELLOW_TEXT();
        } else {
            formatting = Attribute.RED_TEXT();
        }

        return formatting;
    }

    public static AnsiFormat getColorEnterpriseToEbitda(Double number) {
        AnsiFormat formatting;

        if (number > 0.0 && number <= 10.0) {
            formatting = new AnsiFormat(Attribute.GREEN_TEXT());
        } else if (number > 0.0 && number <= 15.0) {
            formatting = new AnsiFormat(Attribute.YELLOW_TEXT());
        } else {
            formatting = new AnsiFormat(Attribute.RED_TEXT());
        }

        return formatting;
    }

    public static AnsiFormat getColorCurrentRatio(Double currentRatio) {
        AnsiFormat formatting = new AnsiFormat(Attribute.RED_TEXT());

        if (currentRatio >= 1.2 && currentRatio <= 2.5) {
            formatting = new AnsiFormat(Attribute.GREEN_TEXT());
        } else if ((currentRatio >= 1.0 && currentRatio <= 1.2) || currentRatio > 2.5) {
            formatting = new AnsiFormat(Attribute.YELLOW_TEXT());
        } else if (currentRatio < 1.0) {
            formatting = new AnsiFormat(Attribute.RED_TEXT());
        }

        return formatting;
    }

    public static String formatInteger(Double number) {
        return String.format("%,d", new BigDecimal(number).toBigInteger());
    }

    public static AnsiFormat getColorOverall(double percent) {
        AnsiFormat formatting;

        if (percent >= 90.0) {
            formatting = new AnsiFormat(Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK());
        } else if (percent >= 80.0) {
            formatting = new AnsiFormat(Attribute.BLACK_TEXT(), Attribute.GREEN_BACK());
        } else if (percent >= 75.0) {
            formatting = new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT());
        } else if (percent >= 60.0) {
            formatting = new AnsiFormat(Attribute.GREEN_TEXT());
        } else if (percent >= 40.0) {
            formatting = new AnsiFormat(Attribute.YELLOW_TEXT());
        } else {
            formatting = new AnsiFormat(Attribute.RED_TEXT());
        }

        return formatting;
    }

    public static AnsiFormat getColorMargins(double percent) {
        AnsiFormat formatting;

        if (percent >= 50.0) {
            formatting = new AnsiFormat(Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK());
        } else if (percent >= 30.0) {
            formatting = new AnsiFormat(Attribute.BLACK_TEXT(), Attribute.GREEN_BACK());
        } else if (percent >= 20.0) {
            formatting = new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT());
        } else if (percent >= 10.0) {
            formatting = new AnsiFormat(Attribute.GREEN_TEXT());
        } else if (percent > 0.0) {
            formatting = new AnsiFormat(Attribute.YELLOW_TEXT());
        } else if (percent >= -10.0) {
            formatting = new AnsiFormat(Attribute.RED_TEXT());
        } else {
            formatting = new AnsiFormat(Attribute.BLACK_TEXT(), Attribute.RED_BACK());
        }

        return formatting;
    }

    public static AnsiFormat getColorShort(double percent) {
        AnsiFormat formatting;

        if (percent >= 20.0) {
            formatting = new AnsiFormat(Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK());
        } else if (percent >= 15.0) {
            formatting = new AnsiFormat(Attribute.BLACK_TEXT(), Attribute.GREEN_BACK());
        } else if (percent >= 10.0) {
            formatting = new AnsiFormat(Attribute.BRIGHT_GREEN_TEXT());
        } else if (percent >= 5.0) {
            formatting = new AnsiFormat(Attribute.GREEN_TEXT());
        } else if (percent > 0.0) {
            formatting = new AnsiFormat(Attribute.YELLOW_TEXT());
        } else if (percent >= -10.0) {
            formatting = new AnsiFormat(Attribute.RED_TEXT());
        } else {
            formatting = new AnsiFormat(Attribute.BLACK_TEXT(), Attribute.RED_BACK());
        }

        return formatting;
    }

    public static AnsiFormat getColorBasic(double number) {
        AnsiFormat formatting;

        if (number > 0.0) {
            formatting = new AnsiFormat(Attribute.GREEN_TEXT());
        } else if (number == 0.0) {
            formatting = new AnsiFormat(Attribute.YELLOW_TEXT());
        } else {
            formatting = new AnsiFormat(Attribute.RED_TEXT());
        }

        return formatting;
    }
//
//    public static AnsiFormat getColorAnalyst(String rating) {
//        AnsiFormat formatting;
//
//        if (rating > 0.0) {
//            formatting = new AnsiFormat(Attribute.GREEN_TEXT());
//        } else if (number == 0.0) {
//            formatting = new AnsiFormat(Attribute.YELLOW_TEXT());
//        } else {
//            formatting = new AnsiFormat(Attribute.RED_TEXT());
//        }
//
//        return formatting;
//    }

    public static void addComponents(JPanel panel, Component... components) {
        for (Component component : components) {
            panel.add(component);
        }
    }

    public static double formatDouble(double number) {
        DecimalFormat df = new DecimalFormat("#.##");

        if (Double.isNaN(number) || Double.isInfinite(number)) {
            return 0.0;
        }

        return Double.parseDouble(df.format(number));
    }

    public static String join(String[] data, String d) {
        if (data.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        int i;
        for (i = 0; i < (data.length - 1); i++) {
            sb.append(data[i]).append(d);
        }

        return sb.append(data[i]).toString();
    }

    private static String cleanNumberString(String data) {
        return join(data.trim().split(","), "");
    }

    private static boolean isParseable(String data) {
        return !(data == null || data.equals("N/A") || data.equals("-") || data.equals("") || data.equals("nan"));
    }

    public static BigDecimal getBigDecimal(String data) {
        BigDecimal result = null;

        if (!isParseable(data)) {
            return null;
        }

        try {
            data = cleanNumberString(data);
            char lastChar = data.charAt(data.length() - 1);
            BigDecimal multiplier = BigDecimal.ONE;

            switch (lastChar) {
                case 'B' -> {
                    data = data.substring(0, data.length() - 1);
                    multiplier = BILLION;
                }

                case 'M' -> {
                    data = data.substring(0, data.length() - 1);
                    multiplier = MILLION;
                }

                case 'K' -> {
                    data = data.substring(0, data.length() - 1);
                    multiplier = THOUSAND;
                }

                default -> {
                }
            }

            result = new BigDecimal(data).multiply(multiplier);

        } catch (NumberFormatException ignored) {
        }

        return result;
    }

    public static Long getLong(String data) {
        Long result = null;

        if (!isParseable(data)) {
            return result;
        }

        try {
            data = cleanNumberString(data);
            result = Long.parseLong(data);
        } catch (NumberFormatException ignored) {
        }

        return result;
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

    public static String getURLParameters(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }

            String key = entry.getKey();
            String value = entry.getValue();

            key = URLEncoder.encode(key, StandardCharsets.UTF_8);
            value = URLEncoder.encode(value, StandardCharsets.UTF_8);

            sb.append(String.format("%s=%s", key, value));
        }

        return sb.toString();
    }
}
