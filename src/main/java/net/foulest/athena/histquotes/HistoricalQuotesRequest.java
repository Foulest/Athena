package net.foulest.athena.histquotes;

import lombok.AllArgsConstructor;
import net.foulest.athena.util.RedirectableRequest;
import net.foulest.athena.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

@AllArgsConstructor
public class HistoricalQuotesRequest {

    public static final Calendar DEFAULT_FROM = Calendar.getInstance();
    public static final Calendar DEFAULT_TO = Calendar.getInstance();

    static {
        DEFAULT_FROM.add(Calendar.YEAR, -1);
    }

    public final String symbol;
    public final Calendar from;
    public final Calendar to;
    public final QueryInterval interval;

    public List<HistoricalQuote> getResult() throws IOException {
        if (from.after(to)) {
            return Collections.emptyList();
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("period1", String.valueOf(from.getTimeInMillis() / 1000));
        params.put("period2", String.valueOf(to.getTimeInMillis() / 1000));
        params.put("interval", interval.getTag());

        String urlBuilder = "https://query1.finance.yahoo.com/v7/finance/download/"
                + URLEncoder.encode(symbol, "UTF-8") + "?" + Utils.getURLParameters(params);

        URL request = new URL(urlBuilder);
        RedirectableRequest redirectableRequest = new RedirectableRequest(request, 5);
        redirectableRequest.setConnectTimeout(10000);
        redirectableRequest.setReadTimeout(10000);

        URLConnection connection = redirectableRequest.openConnection();

        try (InputStreamReader is = new InputStreamReader(connection.getInputStream());
             BufferedReader br = new BufferedReader(is)) {

            br.readLine();
            List<HistoricalQuote> result = new ArrayList<>(365); // Approximation of daily quotes in a year

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                HistoricalQuote quote = parseCSVLine(line);
                result.add(quote);
            }
            return result;
        }
    }

    private HistoricalQuote parseCSVLine(String line) {
        String[] data = line.split(",");
        return new HistoricalQuote(symbol,
                Utils.parseHistDate(data[0]),
                Utils.getBigDecimal(data[1]),
                Utils.getBigDecimal(data[3]),
                Utils.getBigDecimal(data[2]),
                Utils.getBigDecimal(data[4]),
                Utils.getBigDecimal(data[5]),
                Utils.getLong(data[6])
        );
    }
}
