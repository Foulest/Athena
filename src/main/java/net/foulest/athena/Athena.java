package net.foulest.athena;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import net.foulest.athena.histquotes.HistoricalQuote;
import net.foulest.athena.histquotes.QueryInterval;
import net.foulest.athena.stock.Stock;
import net.foulest.athena.stock.StockData;
import net.foulest.athena.util.ColorCondition;
import net.foulest.athena.util.Utils;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.foulest.athena.util.Utils.*;

public class Athena {

    public static Calendar from = Calendar.getInstance();
    public static Calendar to = Calendar.getInstance();
    public static List<Stock> goodStocks = new ArrayList<>();

    /**
     * The main method.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        from.add(Calendar.YEAR, -5);

        System.out.println();
        System.out.println(Ansi.colorize("Athena (Early Access)", Attribute.BOLD(), Attribute.BRIGHT_GREEN_TEXT()));
        System.out.println(Ansi.colorize("Disclaimer:", Attribute.BOLD()) + " This utility may not be accurate. Use at your own risk.");
        System.out.println();

        System.out.print(Ansi.colorize("Enter a stock name or file path: ", Attribute.BOLD()));
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();

        // Ignores empty inputs.
        if (input.isEmpty()) {
            System.out.println(Ansi.colorize("Stock name '' is invalid.", Attribute.RED_TEXT()));
            return;
        }

        processInput(input);
    }

    /**
     * Checks a list of stock symbols for good stocks.
     *
     * @param input The list of stock symbols.
     */
    private static void processInput(String input) {
        if (isFilePath(input)) {
            processFile(input);
        } else if (input.equalsIgnoreCase("NASDAQ")) {
            processNASDAQ();
        } else {
            checkStockSymbols(() -> Stream.of(input));
        }
    }

    /**
     * Checks a file of stock symbols for good stocks.
     *
     * @param filePath The path to the file.
     */
    private static void processFile(String filePath) {
        System.out.println();

        checkStockSymbols(() -> {
            File file = new File(filePath);

            try {
                return Files.lines(file.toPath());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    /**
     * Checks the NASDAQ stock symbols for good stocks.
     */
    private static void processNASDAQ() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("nasdaq.txt");

        if (is == null) {
            System.out.println(Ansi.colorize("NASDAQ file not found.", Attribute.RED_TEXT()));
            return;
        }

        System.out.println();
        checkStockSymbols(() -> new BufferedReader(new InputStreamReader(is)).lines());
    }

    /**
     * Checks a list of stock symbols for good stocks.
     *
     * @param symbolSupplier A supplier of a stream of stock symbols.
     */
    public static void checkStockSymbols(Supplier<Stream<String>> symbolSupplier) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Stock>> futures = new ArrayList<>();

        try (Stream<String> symbols = symbolSupplier.get()) {
            symbols.forEach(symbol -> {
                if (!symbol.isEmpty() && !symbol.contains("/")) {
                    futures.add(executor.submit(() -> {
                        Stock stock = StockData.getStockData(symbol);
                        analyzeStock(stock, stock.getHistory(from, to, QueryInterval.DAILY));
                        return stock;
                    }));
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<Stock> goodStocks = futures.stream().map(future -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        executor.shutdown();
        System.out.println(goodStocks);
    }

    /**
     * Analyzes a stock's data and prints out the results.
     *
     * @param stock        The stock to analyze.
     * @param stockHistory The stock's price history.
     */
    private static void analyzeStock(Stock stock, List<HistoricalQuote> stockHistory) {
        try {
            if (stockHistory.isEmpty() || stock.getName() == null || stock.getIndustry() == null
                    || stock.getSector() == null || stock.getFullTimeEmployees() == null
                    || stock.getAverageVolume10Days() == null || stock.getMarketCap() == null
                    || stock.getEnterpriseValue() == null || stock.getAnalystScore() == null
                    || stock.getTotalCashPerShare() == null || stock.getRevenuePerShare() == null
                    || stock.getFreeCashflow() == null || stock.getOperatingCashflow() == null
                    || stock.getTotalCash() == null || stock.getTotalDebt() == null
                    || stock.getTotalRevenue() == null || stock.getGrossProfits() == null
                    || stock.getEbitda() == null || stock.getRevenueGrowth() == null
                    || stock.getEarningsGrowth() == null || stock.getEarningsQuarterlyGrowth() == null
                    || stock.getCurrentRatio() == null || stock.getQuickRatio() == null
                    || stock.getDebtToEquity() == null || stock.getBeta() == null || stock.getForwardPE() == null
                    || stock.getPEGRatio() == null || stock.getEnterpriseToEbitda() == null
                    || stock.getEnterpriseToRevenue() == null || stock.getEbitdaMargins() == null
                    || stock.getGrossMargins() == null || stock.getOperatingMargins() == null
                    || stock.getProfitMargins() == null || stock.getReturnOnAssets() == null
                    || stock.getReturnOnEquity() == null || stock.getBookValue() == null
                    || stock.getPriceToBook() == null || stock.getForwardEPS() == null
                    || stock.getTrailingEPS() == null || stockHistory.size() < 253) {
                printStockError(stock);
                TimeUnit.MILLISECONDS.sleep(2500);
                return;
            }

            double[] stockOpens = new double[stockHistory.size()];
            double[] stockCloses = new double[stockHistory.size()];

            int idx = 0;
            for (HistoricalQuote quote : stockHistory) {
                if (quote.getOpen() == null) {
                    printStockError(stock);
                    TimeUnit.MILLISECONDS.sleep(2500);
                    return;
                }

                stockOpens[idx] = formatDouble(quote.getOpen().doubleValue());
                stockCloses[idx] = formatDouble(quote.getAdjClose().doubleValue());
                idx++;
            }

            // Calculate the stock's market changes directly while iterating over the stock history
            stock.setChange1d(formatDouble(((stock.getClose() - stockOpens[stockOpens.length - 1]) / stockOpens[stockOpens.length - 1]) * 100));
            stock.setChange5d(formatDouble(((stock.getClose() - stockOpens[stockOpens.length - 5]) / stockOpens[stockOpens.length - 5]) * 100));
            stock.setChange1m(formatDouble(((stock.getClose() - stockCloses[stockCloses.length - 21]) / stockCloses[stockCloses.length - 22]) * 100));
            stock.setChange3m(formatDouble(((stock.getClose() - stockCloses[stockCloses.length - 63]) / stockCloses[stockCloses.length - 124]) * 100));
            stock.setChange6m(formatDouble(((stock.getClose() - stockCloses[stockCloses.length - 124]) / stockCloses[stockCloses.length - 124]) * 100));
            stock.setChange12m(formatDouble((stock.getClose() - stockCloses[stockCloses.length - 253]) / stockCloses[stockCloses.length - 253] * 100));
            stock.setAverageChange(formatDouble(calculateWeightedAverageChangePercentage(stock)));

            // Stock Header
            System.out.println();
            System.out.println(Ansi.colorize("[" + stock.getSymbol(), Attribute.BOLD())
                    + Ansi.colorize(" " + stock.getCurrencySymbol() + stock.getClose(), Attribute.SATURATED())
                    + Ansi.colorize("]", Attribute.BOLD()));

            printColoredStockValue("Name", stock.getName(), Attribute.CLEAR(), Collections.emptyList());
            printColoredStockValue("Industry", stock.getIndustry(), Attribute.CLEAR(), Collections.emptyList());
            printColoredStockValue("Sector", stock.getSector(), Attribute.CLEAR(), Collections.emptyList());

            printColoredStockValue("Full Time Employees", stock.getFullTimeEmployees(), Collections.emptyList(),
                    v -> String.format("%,d", stock.getFullTimeEmployees()), Attribute.CLEAR());

            printColoredStockValue("Average Volume", stock.getAverageVolume10Days(), Collections.emptyList(),
                    Utils::formatInteger, Attribute.CLEAR());

            System.out.println();

            printColoredStockValue("Market Cap", stock.getMarketCap(), List.of(
                    new ColorCondition(stock.getMarketCap() > 0.0, Attribute.GREEN_TEXT(), Attribute.BOLD())
            ), v -> "$" + formatInteger(v), Attribute.RED_TEXT());

            printColoredStockValue("Enterprise Value", stock.getEnterpriseValue(), List.of(
                    new ColorCondition(stock.getEnterpriseValue() > 0.0, Attribute.GREEN_TEXT(), Attribute.BOLD())
            ), v -> "$" + formatInteger(v), Attribute.RED_TEXT());

            Function<Double, String> analystScoreFormatter = v -> {
                if (v >= 1.0 && v <= 1.9) {
                    return v + " (Strong Buy)";
                } else if (v >= 2.0 && v <= 2.4) {
                    return v + " (Buy)";
                } else if (v >= 2.5 && v <= 2.9) {
                    return v + " (Hold)";
                } else if (v >= 3.0 && v <= 3.4) {
                    return v + " (Sell)";
                } else if (v >= 3.5) {
                    return v + " (Strong Sell)";
                }
                return "";
            };

            printColoredStockValue("Analyst Score", stock.getAnalystScore(), Arrays.asList(
                    new ColorCondition(stock.getAnalystScore() >= 1.0 && stock.getAnalystScore() <= 1.9, Attribute.BOLD(), Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getAnalystScore() >= 2.0 && stock.getAnalystScore() <= 2.4, Attribute.BOLD(), Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getAnalystScore() >= 2.5 && stock.getAnalystScore() <= 2.9, Attribute.BOLD(), Attribute.YELLOW_TEXT()),
                    new ColorCondition(stock.getAnalystScore() >= 3.0 && stock.getAnalystScore() <= 3.4, Attribute.BOLD(), Attribute.RED_TEXT()),
                    new ColorCondition(stock.getAnalystScore() >= 3.5, Attribute.BOLD(), Attribute.RED_TEXT())
            ), analystScoreFormatter, Attribute.CLEAR());

            printColoredStockValue("Total Cash Per Share", stock.getTotalCashPerShare(), List.of(
                    new ColorCondition(stock.getTotalCashPerShare() > 0.0, Attribute.GREEN_TEXT())
            ), v -> "$" + formatDouble(v), Attribute.RED_TEXT());

            printColoredStockValue("Revenue Per Share", stock.getRevenuePerShare(), List.of(
                    new ColorCondition(stock.getRevenuePerShare() > 0.0, Attribute.GREEN_TEXT())
            ), v -> "$" + formatDouble(v), Attribute.RED_TEXT());

            System.out.println();

            printColoredStockValue("Free Cashflow", stock.getFreeCashflow(), List.of(
                    new ColorCondition(stock.getFreeCashflow() > 0.0, Attribute.GREEN_TEXT())
            ), v -> "$" + formatInteger(v), Attribute.RED_TEXT());

            printColoredStockValue("Operating Cashflow", stock.getOperatingCashflow(), List.of(
                    new ColorCondition(stock.getOperatingCashflow() > 0.0, Attribute.GREEN_TEXT())
            ), v -> "$" + formatInteger(v), Attribute.RED_TEXT());

            printColoredStockValue("Total Cash", stock.getTotalCash(), List.of(
                    new ColorCondition(stock.getTotalCash() > 0.0, Attribute.GREEN_TEXT())
            ), v -> "$" + formatInteger(v), Attribute.RED_TEXT());

            printColoredStockValue("Total Debt", stock.getTotalDebt(), Collections.emptyList()
                    , v -> "$" + formatInteger(v), Attribute.RED_TEXT());

            printColoredStockValue("Total Revenue", stock.getTotalRevenue(), List.of(
                    new ColorCondition(stock.getTotalRevenue() > 0.0, Attribute.GREEN_TEXT())
            ), v -> "$" + formatInteger(v), Attribute.RED_TEXT());

            printColoredStockValue("Gross Profits", stock.getGrossProfits(), List.of(
                    new ColorCondition(stock.getGrossProfits() > 0.0, Attribute.GREEN_TEXT())
            ), v -> "$" + formatInteger(v), Attribute.RED_TEXT());

            printColoredStockValue("EBITDA", stock.getEbitda(), List.of(
                    new ColorCondition(stock.getEbitda() > 0.0, Attribute.GREEN_TEXT())
            ), v -> "$" + formatInteger(v), Attribute.RED_TEXT());

            System.out.println();

            printChange("Change (1 Day)", stock.getChange1m());
            printChange("Change (5 Days)", stock.getChange5d());
            printChange("Change (1 Month)", stock.getChange1m());
            printChange("Change (3 Month)", stock.getChange3m());
            printChange("Change (6 Month)", stock.getChange6m());
            printChange("Change (1 Year)", stock.getChange12m());
            printChange("Average Change", calculateWeightedAverageChangePercentage(stock));

            System.out.println();

            printColoredStockValue("Revenue Growth", stock.getRevenueGrowth() * 100, Arrays.asList(
                    new ColorCondition(stock.getRevenueGrowth() * 100 >= 15.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getRevenueGrowth() * 100 >= 5.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getRevenueGrowth() * 100 >= 0.0, Attribute.YELLOW_TEXT())
            ), v -> formatDouble(v) + "%", Attribute.RED_TEXT());

            if (stock.getEarningsGrowth() != null) {
                printColoredStockValue("Earnings Growth", stock.getEarningsGrowth() * 100, Arrays.asList(
                        new ColorCondition(stock.getEarningsGrowth() * 100 >= 15.0, Attribute.BRIGHT_GREEN_TEXT()),
                        new ColorCondition(stock.getEarningsGrowth() * 100 >= 5.0, Attribute.GREEN_TEXT()),
                        new ColorCondition(stock.getEarningsGrowth() * 100 >= 0.0, Attribute.YELLOW_TEXT())
                ), v -> formatDouble(v) + "%", Attribute.RED_TEXT());
            }

            if (stock.getEarningsQuarterlyGrowth() != null) {
                printColoredStockValue("Earnings Quarterly Growth", stock.getEarningsQuarterlyGrowth() * 100, Arrays.asList(
                        new ColorCondition(stock.getEarningsQuarterlyGrowth() * 100 >= 15.0, Attribute.BRIGHT_GREEN_TEXT()),
                        new ColorCondition(stock.getEarningsQuarterlyGrowth() * 100 >= 5.0, Attribute.GREEN_TEXT()),
                        new ColorCondition(stock.getEarningsQuarterlyGrowth() * 100 >= 0.0, Attribute.YELLOW_TEXT())
                ), v -> formatDouble(v) + "%", Attribute.RED_TEXT());
            }

            System.out.println();

            printColoredStockValue("Current Ratio", stock.getCurrentRatio(), Arrays.asList(
                    new ColorCondition(stock.getCurrentRatio() >= 3.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getCurrentRatio() >= 2.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getCurrentRatio() >= 1.0, Attribute.YELLOW_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.RED_TEXT());

            printColoredStockValue("Quick Ratio", stock.getQuickRatio(), Arrays.asList(
                    new ColorCondition(stock.getQuickRatio() >= 3.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getQuickRatio() >= 1.5, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getQuickRatio() >= 1.0, Attribute.YELLOW_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.RED_TEXT());

            printColoredStockValue("Debt-to-Equity Ratio", stock.getDebtToEquity() / 100, Arrays.asList(
                    new ColorCondition(stock.getDebtToEquity() / 100 > 2.0, Attribute.RED_TEXT()),
                    new ColorCondition(stock.getDebtToEquity() / 100 >= 1.0, Attribute.YELLOW_TEXT()),
                    new ColorCondition(stock.getDebtToEquity() / 100 >= 0.5, Attribute.GREEN_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.BRIGHT_GREEN_TEXT());

            System.out.println();

            if (stock.getBeta() != null) {
                printColoredStockValue("Beta", stock.getBeta(), Arrays.asList(
                        new ColorCondition(stock.getBeta() >= 1.1, Attribute.RED_TEXT()),
                        new ColorCondition(stock.getBeta() >= 0.9, Attribute.YELLOW_TEXT()),
                        new ColorCondition(stock.getBeta() >= 0.8, Attribute.GREEN_TEXT())
                ), v -> String.valueOf(formatDouble(v)), Attribute.BRIGHT_GREEN_TEXT());
            }

            printColoredStockValue("Forward PE", stock.getForwardPE(), Arrays.asList(
                    new ColorCondition(stock.getForwardPE() >= 20.0, Attribute.RED_TEXT()),
                    new ColorCondition(stock.getForwardPE() >= 10.0, Attribute.YELLOW_TEXT()),
                    new ColorCondition(stock.getForwardPE() >= 5.0, Attribute.GREEN_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.BRIGHT_GREEN_TEXT());

            printColoredStockValue("PEG Ratio", stock.getPEGRatio(), Arrays.asList(
                    new ColorCondition(stock.getPEGRatio() >= 3.0, Attribute.RED_TEXT()),
                    new ColorCondition(stock.getPEGRatio() >= 2.0, Attribute.YELLOW_TEXT()),
                    new ColorCondition(stock.getPEGRatio() >= 1.0, Attribute.GREEN_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.BRIGHT_GREEN_TEXT());

            printColoredStockValue("Enterprise Value to EBITDA", stock.getEnterpriseToEbitda(), Arrays.asList(
                    new ColorCondition(stock.getEnterpriseToEbitda() >= 20.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getEnterpriseToEbitda() >= 10.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getEnterpriseToEbitda() >= 5.0, Attribute.YELLOW_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.RED_TEXT());

            printColoredStockValue("Enterprise Value to Revenue", stock.getEnterpriseToRevenue(), Arrays.asList(
                    new ColorCondition(stock.getEnterpriseToRevenue() >= 3.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getEnterpriseToRevenue() >= 1.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getEnterpriseToRevenue() >= 0.5, Attribute.YELLOW_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.RED_TEXT());

            System.out.println();

            printColoredStockValue("EBITDA Margins", stock.getEbitdaMargins() * 100, Arrays.asList(
                    new ColorCondition(stock.getEbitdaMargins() * 100 >= 20.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getEbitdaMargins() * 100 >= 10.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getEbitdaMargins() * 100 > 0.0, Attribute.YELLOW_TEXT())
            ), v -> formatDouble(v) + "%", Attribute.RED_TEXT());

            printColoredStockValue("Gross Margins", stock.getGrossMargins() * 100, Arrays.asList(
                    new ColorCondition(stock.getGrossMargins() * 100 >= 60.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getGrossMargins() * 100 >= 40.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getGrossMargins() * 100 > 0.0, Attribute.YELLOW_TEXT())
            ), v -> formatDouble(v) + "%", Attribute.RED_TEXT());

            printColoredStockValue("Operating Margins", stock.getOperatingMargins() * 100, Arrays.asList(
                    new ColorCondition(stock.getOperatingMargins() * 100 >= 20.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getOperatingMargins() * 100 >= 10.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getOperatingMargins() * 100 > 0.0, Attribute.YELLOW_TEXT())
            ), v -> formatDouble(v) + "%", Attribute.RED_TEXT());

            printColoredStockValue("Profit Margins", stock.getProfitMargins() * 100, Arrays.asList(
                    new ColorCondition(stock.getProfitMargins() * 100 >= 20.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getProfitMargins() * 100 >= 10.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getProfitMargins() * 100 > 0.0, Attribute.YELLOW_TEXT())
            ), v -> formatDouble(v) + "%", Attribute.RED_TEXT());

            printColoredStockValue("Return on Assets", stock.getReturnOnAssets() * 100, Arrays.asList(
                    new ColorCondition(stock.getReturnOnAssets() * 100 > 6.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getReturnOnAssets() * 100 >= 3.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getReturnOnAssets() * 100 >= 1.0, Attribute.YELLOW_TEXT())
            ), v -> formatDouble(v) + "%", Attribute.RED_TEXT());

            printColoredStockValue("Return on Equity", stock.getReturnOnEquity() * 100, Arrays.asList(
                    new ColorCondition(stock.getReturnOnEquity() * 100 > 20.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getReturnOnEquity() * 100 >= 15.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getReturnOnEquity() * 100 >= 10.0, Attribute.YELLOW_TEXT())
            ), v -> formatDouble(v) + "%", Attribute.RED_TEXT());

            System.out.println();

            printColoredStockValue("Book Value", stock.getBookValue(), Arrays.asList(
                    new ColorCondition(stock.getBookValue() >= 1.5, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getBookValue() >= 1.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getBookValue() >= 0.75, Attribute.YELLOW_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.RED_TEXT());

            printColoredStockValue("Price to Book", stock.getPriceToBook(), Arrays.asList(
                    new ColorCondition(stock.getPriceToBook() >= 3.0, Attribute.RED_TEXT()),
                    new ColorCondition(stock.getPriceToBook() >= 2.0, Attribute.YELLOW_TEXT()),
                    new ColorCondition(stock.getPriceToBook() >= 1.0, Attribute.GREEN_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.BRIGHT_GREEN_TEXT());

            printColoredStockValue("Forward EPS", stock.getForwardEPS(), Arrays.asList(
                    new ColorCondition(stock.getForwardEPS() >= 2.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getForwardEPS() >= 1.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getForwardEPS() >= 0.5, Attribute.YELLOW_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.RED_TEXT());

            printColoredStockValue("Trailing EPS", stock.getTrailingEPS(), Arrays.asList(
                    new ColorCondition(stock.getTrailingEPS() >= 2.0, Attribute.BRIGHT_GREEN_TEXT()),
                    new ColorCondition(stock.getTrailingEPS() >= 1.0, Attribute.GREEN_TEXT()),
                    new ColorCondition(stock.getTrailingEPS() >= 0.5, Attribute.YELLOW_TEXT())
            ), v -> String.valueOf(formatDouble(v)), Attribute.RED_TEXT());

            System.out.println();

            calculateInvestmentQuality(stock);

            // Prints a Google link to the stock for more information.
            System.out.println();
            System.out.println(Ansi.colorize("https://google.com/search?q=" + stock.getSymbol() + "+stock", Attribute.CYAN_TEXT()));
            System.out.println();

            if (stock.getInvestmentQuality() >= 70) {
                goodStocks.add(stock);
            }

            TimeUnit.MILLISECONDS.sleep(2500);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static <T> void printColoredStockValue(String label, T value, Attribute defaultAttribute,
                                                   List<ColorCondition> conditions) {
        if (value != null) {
            String formattedValue = value.toString();

            System.out.print(Ansi.colorize(label + ": ", Attribute.BOLD()));

            for (ColorCondition condition : conditions) {
                if (condition.isApplicable()) {
                    System.out.println(condition.colorize(formattedValue));
                    return;
                }
            }

            System.out.println(Ansi.colorize(formattedValue, defaultAttribute));
        }
    }

    private static <T> void printColoredStockValue(String label, T value, List<ColorCondition> conditions,
                                                   Function<T, String> valueFormatter, Attribute... defaultAttribute) {
        if (value != null) {
            String formattedValue = valueFormatter.apply(value);

            System.out.print(Ansi.colorize(label + ": ", Attribute.BOLD()));

            for (ColorCondition condition : conditions) {
                if (condition.isApplicable()) {
                    System.out.println(condition.colorize(formattedValue));
                    return;
                }
            }

            System.out.println(Ansi.colorize(formattedValue, defaultAttribute));
        }
    }

    private static void printChange(String label, double value) {
        Function<Double, String> changeFormatter = v -> formatDouble(v) + "%";

        printColoredStockValue(label, value, Arrays.asList(
                new ColorCondition(value >= 20.0, Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()),
                new ColorCondition(value >= 15.0, Attribute.BLACK_TEXT(), Attribute.GREEN_BACK()),
                new ColorCondition(value >= 10.0, Attribute.BRIGHT_GREEN_TEXT()),
                new ColorCondition(value >= 5.0, Attribute.GREEN_TEXT()),
                new ColorCondition(value > 0.0, Attribute.YELLOW_TEXT()),
                new ColorCondition(value > -10.0, Attribute.RED_TEXT()),
                new ColorCondition(value >= -15.0, Attribute.BLACK_TEXT(), Attribute.RED_BACK())
        ), changeFormatter, Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK());
    }

    public static void calculateInvestmentQuality(Stock stock) {
        int totalScore = 0;
        int maxScore = 10;

        double weightedAverageChange = calculateWeightedAverageChangePercentage(stock);

        totalScore += (stock.getAverageVolume10Days() >= 1000000) ? 1 : 0;
        totalScore += (stock.getMarketCap() >= 1000000000) ? 1 : 0;
        totalScore += (stock.getEnterpriseValue() > 0 && stock.getEnterpriseToEbitda() >= 10.0 && stock.getEnterpriseToRevenue() >= 1.0) ? 1 : 0;
        totalScore += (stock.getFreeCashflow() > 0 && stock.getOperatingCashflow() > 0 && stock.getTotalCash() > 0
                && stock.getTotalRevenue() > 0 && stock.getGrossProfits() > 0 && stock.getEbitda() > 0
                && stock.getTotalDebt() < stock.getFreeCashflow() && stock.getTotalDebt() < stock.getOperatingCashflow()
                && stock.getTotalDebt() < stock.getTotalCash() && stock.getTotalDebt() < stock.getTotalRevenue()
                && stock.getTotalDebt() < stock.getGrossProfits() && stock.getTotalDebt() < stock.getEbitda()
                && stock.getCurrentRatio() >= 1.5 && stock.getQuickRatio() >= 1.5 && stock.getDebtToEquity() / 100 <= 0.5) ? 1 : 0;
        totalScore += (weightedAverageChange > 0) ? 1 : 0;
        totalScore += (stock.getRevenueGrowth() > 0 && stock.getEarningsGrowth() > 0 && stock.getEarningsQuarterlyGrowth() > 0) ? 1 : 0;
        totalScore += (stock.getBeta() >= 0.5 && stock.getBeta() <= 1.3) ? 1 : 0;
        totalScore += (stock.getForwardPE() <= 20.0) ? 1 : 0;
        totalScore += (stock.getPEGRatio() <= 2.0) ? 1 : 0;
        totalScore += (stock.getEbitdaMargins() * 100 >= 10.0 && stock.getGrossMargins() * 100 >= 40.0
                && stock.getOperatingMargins() * 100 >= 10.0 && stock.getProfitMargins() * 100 >= 10.0
                && stock.getReturnOnAssets() * 100 >= 3.0 && stock.getReturnOnEquity() * 100 >= 15.0) ? 1 : 0;

        double totalScorePercent = (totalScore / (double) maxScore) * 100;
        printColoredStockValue("Investment Quality", totalScorePercent, Arrays.asList(
                new ColorCondition(totalScorePercent >= 80.0, Attribute.BRIGHT_GREEN_TEXT()),
                new ColorCondition(totalScorePercent >= 70.0, Attribute.GREEN_TEXT()),
                new ColorCondition(totalScorePercent >= 60.0, Attribute.YELLOW_TEXT())
        ), v -> String.format("%.2f", v) + "%", Attribute.RED_TEXT());

        System.out.println();

        if (totalScore < maxScore) {
            System.out.println(Ansi.colorize("Investment Concerns:", Attribute.BOLD()));

            if (stock.getAverageVolume10Days() < 1000000) {
                System.out.println("- Bad Average Volume");
            }

            if (stock.getMarketCap() < 1000000000) {
                System.out.println("- Bad Market Cap");
            }

            if (stock.getEnterpriseValue() <= 0 || stock.getEnterpriseToEbitda() < 10.0 || stock.getEnterpriseToRevenue() < 1.0) {
                System.out.println("- Bad Enterprise Value");
            }

            if (stock.getFreeCashflow() <= 0 || stock.getOperatingCashflow() <= 0 || stock.getTotalCash() <= 0
                    || stock.getTotalRevenue() <= 0 || stock.getGrossProfits() <= 0 || stock.getEbitda() <= 0
                    || stock.getTotalDebt() >= stock.getFreeCashflow()
                    || stock.getTotalDebt() >= stock.getOperatingCashflow()
                    || stock.getTotalDebt() >= stock.getTotalCash()
                    || stock.getTotalDebt() >= stock.getTotalRevenue()
                    || stock.getTotalDebt() >= stock.getGrossProfits()
                    || stock.getTotalDebt() >= stock.getEbitda()
                    || stock.getCurrentRatio() < 1.5
                    || stock.getQuickRatio() < 1.5
                    || stock.getDebtToEquity() / 100 >= 0.5) {
                System.out.println("- Bad Debt Management");
            }

            if (weightedAverageChange <= 0) {
                System.out.println("- Bad Stock Performance");
            }

            if (stock.getRevenueGrowth() <= 0 || stock.getEarningsGrowth() <= 0 || stock.getEarningsQuarterlyGrowth() <= 0) {
                System.out.println("- Bad Recent Earnings");
            }

            if (stock.getBeta() < 0.5 || stock.getBeta() > 1.3) {
                System.out.println("- Bad Beta");
            }

            if (stock.getForwardPE() > 20.0) {
                System.out.println("- Bad Forward PE");
            }

            if (stock.getPEGRatio() > 2.0) {
                System.out.println("- Bad PEG Ratio");
            }

            if (stock.getEbitdaMargins() * 100 < 10.0 || stock.getGrossMargins() * 100 < 40.0
                    || stock.getOperatingMargins() * 100 < 10.0 || stock.getProfitMargins() * 100 < 10.0
                    || stock.getReturnOnAssets() * 100 < 3.0 || stock.getReturnOnEquity() * 100 < 15.0) {
                System.out.println("- Bad Margins");
            }
        }

        stock.setInvestmentQuality(totalScorePercent);
    }

    public static double calculateWeightedAverageChangePercentage(Stock stock) {
        double weight1Day = 0.05;
        double weight5Days = 0.1;
        double weight1Month = 0.15;
        double weight3Months = 0.2;
        double weight6Months = 0.25;
        double weight1Year = 0.25;

        return weight1Day * stock.getChange1d()
                + weight5Days * stock.getChange5d()
                + weight1Month * stock.getChange1m()
                + weight3Months * stock.getChange3m()
                + weight6Months * stock.getChange6m()
                + weight1Year * stock.getChange12m();
    }

    public static void printStockError(Stock stock) {
        System.out.println();
        System.out.println(Ansi.colorize("This stock, " + stock.getName()
                + ", is missing necessary information.", Attribute.BOLD(), Attribute.RED_TEXT()));
        System.out.println(Ansi.colorize("https://google.com/search?q="
                + stock.getSymbol() + "+stock", Attribute.CYAN_TEXT()));
        System.out.println();
    }
}

