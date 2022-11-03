package net.foulest.athena;

import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import net.foulest.athena.histquotes.HistoricalQuote;
import net.foulest.athena.histquotes.QueryInterval;
import net.foulest.athena.stock.Stock;
import net.foulest.athena.stock.StockData;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static net.foulest.athena.util.Utils.*;

public class Athena {

    public static Calendar from = Calendar.getInstance();
    public static Calendar to = Calendar.getInstance();
    public static List<String> goodStocks = new ArrayList<>();

    public static void main(String[] args) {
        from.add(Calendar.YEAR, -5);

        System.out.println();
        System.out.println(Ansi.colorize("Athena (Early Access)", Attribute.BOLD(), Attribute.BRIGHT_GREEN_TEXT()));
        System.out.println(Ansi.colorize("Disclaimer:", Attribute.BOLD()) + " This utility may not be accurate. Use at your own risk.");
        System.out.println();

        System.out.print(Ansi.colorize("Enter a stock name or file path: ", Attribute.BOLD()));
        Scanner scanner = new Scanner(System.in);
        String input = (scanner.nextLine()).trim();

        // Ignores empty inputs.
        if (input.isEmpty()) {
            System.out.println(Ansi.colorize("Stock name '' is invalid.", Attribute.RED_TEXT()));
            return;
        }

        // If input contains file extension characters, process as file.
        if (input.contains(".") || input.contains("\\") || input.contains("/")) {
            File file = new File(input);

            try {
                System.out.println();
                checkStockSymbolFile(file);
            } catch (FileNotFoundException ignored) {
                System.out.println(Ansi.colorize("File '" + file.getPath() + "' not found.", Attribute.RED_TEXT()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else if (input.equalsIgnoreCase("NASDAQ")) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream is = classLoader.getResourceAsStream("nasdaq.txt");

            if (is == null) {
                System.out.println(Ansi.colorize("NASDAQ file not found.", Attribute.RED_TEXT()));
                return;
            }

            System.out.println();
            checkStockSymbolStream(is);

        } else {
            System.out.println();
            checkStockSymbol(input);
        }
    }

    public static void checkStockSymbolStream(InputStream stream) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String symbol;

            while ((symbol = br.readLine()) != null) {
                if (!symbol.isEmpty() && !symbol.contains("/")) {
                    Stock stock = StockData.getStockData(symbol);
                    analyzeStock(stock, stock.getHistory(from, to, QueryInterval.DAILY));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println(goodStocks);
    }

    public static void checkStockSymbolFile(File file) throws IOException {
        InputStream stream = Files.newInputStream(file.toPath());

        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String symbol;

            while ((symbol = br.readLine()) != null) {
                if (!symbol.isEmpty() && !symbol.contains("/")) {
                    Stock stock = StockData.getStockData(symbol);
                    analyzeStock(stock, stock.getHistory(from, to, QueryInterval.DAILY));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println(goodStocks);
    }

    public static void checkStockSymbol(String symbol) {
        try {
            Stock stock = StockData.getStockData(symbol);
            analyzeStock(stock, stock.getHistory(from, to, QueryInterval.DAILY));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println(goodStocks);
    }

    private static void analyzeStock(Stock stock, List<HistoricalQuote> stockHistory) {
        boolean warning = false;

        try {
            List<Double> stockOpens = new ArrayList<>();
            List<Double> stockCloses = new ArrayList<>();

            // Filters out stocks that have zero price information.
            if (stockHistory.isEmpty()) {
                printStockError(stock);
                TimeUnit.MILLISECONDS.sleep(2500);
                return;
            }

            // Grabs the stock price history.
            for (HistoricalQuote quote : stockHistory) {
                // Filters out stocks that have invalid opening prices.
                if (quote.getOpen() == null) {
                    printStockError(stock);
                    TimeUnit.MILLISECONDS.sleep(2500);
                    return;
                }

                stockOpens.add(formatDouble(quote.getOpen().doubleValue()));
                stockCloses.add(formatDouble(quote.getAdjClose().doubleValue()));
            }

            // Filters out stocks that have missing data.
            if (stock.getTotalDebt() == null || stock.getEbitda() == null || stock.getBookValue() == null
                    || stockOpens.size() < 253 || stock.getCurrentRatio() == null || stock.getDebtToEquity() == null
                    || stock.getPriceToBook() == null || stock.getOperatingCashflow() == null
                    || stock.getRevenueGrowth() == null) {
                printStockError(stock);
                TimeUnit.MILLISECONDS.sleep(2500);
                return;
            }

            // Calculates the stock's market changes.
            double change1d = formatDouble(((stock.getClose() - stockOpens.get(stockOpens.size() - 1)) / stockOpens.get(stockOpens.size() - 1)) * 100);
            double change5d = formatDouble(((stock.getClose() - stockOpens.get(stockOpens.size() - 5)) / stockOpens.get(stockOpens.size() - 5)) * 100);
            double change1m = formatDouble(((stock.getClose() - stockCloses.get(stockCloses.size() - 21)) / stockCloses.get(stockCloses.size() - 22)) * 100);
            double change6m = formatDouble(((stock.getClose() - stockCloses.get(stockCloses.size() - 124)) / stockCloses.get(stockCloses.size() - 124)) * 100);
            double change12m = formatDouble((stock.getClose() - stockCloses.get(stockCloses.size() - 253)) / stockCloses.get(stockCloses.size() - 253) * 100);

            // Stock Header
            System.out.println(Ansi.colorize("[" + stock.getSymbol(), Attribute.BOLD())
                    + Ansi.colorize(" " + stock.getCurrencySymbol() + stock.getClose(), Attribute.SATURATED())
                    + Ansi.colorize("]", Attribute.BOLD()));

            // Name
            System.out.println();
            System.out.println(Ansi.colorize("Name: ", Attribute.BOLD()) + stock.getName());

            // Industry
            if (stock.getIndustry() != null) {
                System.out.println(Ansi.colorize("Industry: ", Attribute.BOLD()) + stock.getIndustry());
            }

            // Sector
            if (stock.getSector() != null) {
                System.out.println(Ansi.colorize("Sector: ", Attribute.BOLD()) + stock.getSector());
            }

            // Full Time Employees
            if (stock.getFullTimeEmployees() != null) {
                System.out.println(Ansi.colorize("Full Time Employees: ", Attribute.BOLD())
                        + String.format("%,d", new BigDecimal(stock.getFullTimeEmployees()).toBigInteger()));
            }

            // Market Cap
            if (stock.getMarketCap() != null) {
                System.out.println(Ansi.colorize("Market Cap: ", Attribute.BOLD())
                        + formatInteger(stock.getMarketCap()));
            }

            // Enterprise Value
            if (stock.getEnterpriseValue() != null) {
                System.out.println(Ansi.colorize("Enterprise Value: ", Attribute.BOLD())
                        + Ansi.colorize("$" + formatInteger(stock.getEnterpriseValue()), getColorBasic(stock.getEnterpriseValue())));
            }

            // Analyst Rating
            if (stock.getAnalystRating() != null) {
                System.out.println(Ansi.colorize("Analyst Rating: ", Attribute.BOLD())
                                + stock.getAnalystRating()
                        /*                        + Ansi.colorize(stock.getAnalystRating(), getColorAnalyst(stock.getAnalystRating()))*/);
            }

            // Analyst Score
            if (stock.getAnalystScore() != null) {
                System.out.println(Ansi.colorize("Analyst Score: ", Attribute.BOLD())
                        + Ansi.colorize(String.valueOf(stock.getAnalystScore()), Attribute.BOLD()));
            }

            // Analyst Count
            if (stock.getAnalystScore() != null) {
                System.out.println(Ansi.colorize("Analyst Count: ", Attribute.BOLD())
                        + Ansi.colorize(String.valueOf(stock.getAnalystCount()), Attribute.BOLD()));
            }

            System.out.println();

            // Free Cashflow
            if (stock.getFreeCashflow() != null) {
                System.out.println(Ansi.colorize("Free Cashflow: ", Attribute.BOLD())
                        + Ansi.colorize("$" + formatInteger(stock.getFreeCashflow()), getColorBasic(stock.getFreeCashflow())));
            }

            // Operating Cashflow
            if (stock.getOperatingCashflow() != null) {
                System.out.println(Ansi.colorize("Operating Cashflow: ", Attribute.BOLD())
                        + Ansi.colorize("$" + formatInteger(stock.getOperatingCashflow()), getColorBasic(stock.getOperatingCashflow())));
            }

            // Total Cash
            if (stock.getTotalCash() != null) {
                System.out.println(Ansi.colorize("Total Cash: ", Attribute.BOLD())
                        + Ansi.colorize("$" + formatInteger(stock.getTotalCash()), getColorBasic(stock.getTotalCash()))
                        + Ansi.colorize(" (Per Share: $" + formatDouble(stock.getTotalCashPerShare()) + ")", Attribute.ITALIC()));
            }

            // Total Debt
            if (stock.getTotalDebt() != null) {
                System.out.println(Ansi.colorize("Total Debt: ", Attribute.BOLD())
                        + Ansi.colorize("$" + formatInteger(stock.getTotalDebt()), Attribute.RED_TEXT())
                        + Ansi.colorize(" (D/E Ratio: ", Attribute.ITALIC())
                        + Ansi.colorize(String.valueOf(formatDouble(stock.getDebtToEquity())), getColorDebtToEquity(stock.getDebtToEquity()), Attribute.ITALIC())
                        + Ansi.colorize(")", Attribute.ITALIC()));
            }

            // Total Revenue
            if (stock.getTotalRevenue() != null) {
                System.out.println(Ansi.colorize("Total Revenue: ", Attribute.BOLD())
                        + Ansi.colorize("$" + formatInteger(stock.getTotalRevenue()), getColorBasic(stock.getTotalRevenue()))
                        + Ansi.colorize(" (Per Share: $" + formatDouble(stock.getRevenuePerShare()) + ")", Attribute.ITALIC()));
            }

            // Gross Profits
            if (stock.getGrossProfits() != null) {
                System.out.println(Ansi.colorize("Gross Profits: ", Attribute.BOLD())
                        + Ansi.colorize("$" + formatInteger(stock.getGrossProfits()), getColorBasic(stock.getGrossProfits())));
            }

            // EBITDA
            if (stock.getEbitda() != null) {
                System.out.println(Ansi.colorize("EBITDA: ", Attribute.BOLD())
                        + Ansi.colorize("$" + formatInteger(stock.getEbitda()), getColorBasic(stock.getEbitda())));
            }

            // Beta
            if (stock.getBeta() != null) {
                System.out.println(Ansi.colorize("Beta: ", Attribute.BOLD())
                        + Ansi.colorize(String.valueOf(formatDouble(stock.getBeta())), getColorBeta(stock.getBeta()))
                        + Ansi.colorize(" (Avg Volume: " + formatInteger(stock.getAverageVolume10Days()) + ")", Attribute.ITALIC()));
            }

            System.out.println();

            /* Change (1 Day) */
            {
                System.out.print(Ansi.colorize("Change (1 Day): ", Attribute.BOLD()));

                if (change1d >= 20.0) {
                    System.out.println(Ansi.colorize(change1d + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (change1d >= 15.0) {
                    System.out.println(Ansi.colorize(change1d + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (change1d >= 10.0) {
                    System.out.println(Ansi.colorize(change1d + "%", Attribute.BRIGHT_GREEN_TEXT()));

                } else if (change1d >= 5.0) {
                    System.out.println(Ansi.colorize(change1d + "%", Attribute.GREEN_TEXT()));

                } else if (change1d > 0.0) {
                    System.out.println(Ansi.colorize(change1d + "%", Attribute.YELLOW_TEXT()));

                } else if (change1d > -10.0) {
                    System.out.println(Ansi.colorize(change1d + "%", Attribute.RED_TEXT()));

                } else if (change1d >= -15.0) {
                    System.out.println(Ansi.colorize(change1d + "%", Attribute.BLACK_TEXT(), Attribute.RED_BACK()));

                } else {
                    System.out.println(Ansi.colorize(change1d + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK()));
                }
            }

            /* Change (1 Week) */
            {
                System.out.print(Ansi.colorize("Change (5 Days): ", Attribute.BOLD()));

                if (change5d >= 20.0) {
                    System.out.println(Ansi.colorize(change5d + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (change5d >= 15.0) {
                    System.out.println(Ansi.colorize(change5d + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (change5d >= 10.0) {
                    System.out.println(Ansi.colorize(change5d + "%", Attribute.BRIGHT_GREEN_TEXT()));

                } else if (change5d >= 5.0) {
                    System.out.println(Ansi.colorize(change5d + "%", Attribute.GREEN_TEXT()));

                } else if (change5d > 0.0) {
                    System.out.println(Ansi.colorize(change5d + "%", Attribute.YELLOW_TEXT()));

                } else if (change5d > -10.0) {
                    System.out.println(Ansi.colorize(change5d + "%", Attribute.RED_TEXT()));

                } else if (change5d >= -15.0) {
                    System.out.println(Ansi.colorize(change5d + "%", Attribute.BLACK_TEXT(), Attribute.RED_BACK()));

                } else {
                    System.out.println(Ansi.colorize(change5d + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK()));
                }
            }

            /* Change (1 Month) */
            {
                System.out.print(Ansi.colorize("Change (1 Month): ", Attribute.BOLD()));

                if (change1m >= 20.0) {
                    System.out.println(Ansi.colorize(change1m + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (change1m >= 15.0) {
                    System.out.println(Ansi.colorize(change1m + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (change1m >= 10.0) {
                    System.out.println(Ansi.colorize(change1m + "%", Attribute.BRIGHT_GREEN_TEXT()));

                } else if (change1m >= 5.0) {
                    System.out.println(Ansi.colorize(change1m + "%", Attribute.GREEN_TEXT()));

                } else if (change1m > 0.0) {
                    System.out.println(Ansi.colorize(change1m + "%", Attribute.YELLOW_TEXT()));

                } else if (change1m > -10.0) {
                    System.out.println(Ansi.colorize(change1m + "%", Attribute.RED_TEXT()));

                } else if (change1m >= -15.0) {
                    System.out.println(Ansi.colorize(change1m + "%", Attribute.BLACK_TEXT(), Attribute.RED_BACK()));

                } else {
                    System.out.println(Ansi.colorize(change1m + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK()));
                }
            }

            /* Change (6 Month) */
            {
                System.out.print(Ansi.colorize("Change (6 Month): ", Attribute.BOLD()));

                if (change6m >= 20.0) {
                    System.out.println(Ansi.colorize(change6m + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (change6m >= 15.0) {
                    System.out.println(Ansi.colorize(change6m + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (change6m >= 10.0) {
                    System.out.println(Ansi.colorize(change6m + "%", Attribute.BRIGHT_GREEN_TEXT()));

                } else if (change6m >= 5.0) {
                    System.out.println(Ansi.colorize(change6m + "%", Attribute.GREEN_TEXT()));

                } else if (change6m > 0.0) {
                    System.out.println(Ansi.colorize(change6m + "%", Attribute.YELLOW_TEXT()));

                } else if (change6m > -10.0) {
                    System.out.println(Ansi.colorize(change6m + "%", Attribute.RED_TEXT()));

                } else if (change6m >= -15.0) {
                    System.out.println(Ansi.colorize(change6m + "%", Attribute.BLACK_TEXT(), Attribute.RED_BACK()));

                } else {
                    System.out.println(Ansi.colorize(change6m + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK()));
                }
            }

            /* Change (1 Year) */
            {
                System.out.print(Ansi.colorize("Change (1 Year): ", Attribute.BOLD()));

                if (change12m >= 20.0) {
                    System.out.println(Ansi.colorize(change12m + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (change12m >= 15.0) {
                    System.out.println(Ansi.colorize(change12m + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (change12m >= 10.0) {
                    System.out.println(Ansi.colorize(change12m + "%", Attribute.BRIGHT_GREEN_TEXT()));

                } else if (change12m >= 5.0) {
                    System.out.println(Ansi.colorize(change12m + "%", Attribute.GREEN_TEXT()));

                } else if (change12m > 0.0) {
                    System.out.println(Ansi.colorize(change12m + "%", Attribute.YELLOW_TEXT()));

                } else if (change12m > -10.0) {
                    System.out.println(Ansi.colorize(change12m + "%", Attribute.RED_TEXT()));

                } else if (change12m >= -15.0) {
                    System.out.println(Ansi.colorize(change12m + "%", Attribute.BLACK_TEXT(), Attribute.RED_BACK()));

                } else {
                    System.out.println(Ansi.colorize(change12m + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK()));
                }
            }

            System.out.println();

            // Anti-Debt Ratio
            if (stock.getQuickRatio() != null && stock.getCurrentRatio() != null) {
                stock.setAntiDebtRatio(formatDouble((stock.getQuickRatio() + stock.getCurrentRatio()) / 2));
                System.out.print(Ansi.colorize("Anti-Debt Ratio: ", Attribute.BOLD()));

                if (stock.getAntiDebtRatio() >= 3.0) {
                    System.out.println(Ansi.colorize(String.valueOf(stock.getAntiDebtRatio()), Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (stock.getAntiDebtRatio() >= 2.0) {
                    System.out.println(Ansi.colorize(String.valueOf(stock.getAntiDebtRatio()), Attribute.BLACK_TEXT(), Attribute.GREEN_BACK()));

                } else if (stock.getAntiDebtRatio() >= 1.2) {
                    System.out.println(Ansi.colorize(String.valueOf(stock.getAntiDebtRatio()), Attribute.GREEN_TEXT()));

                } else if (stock.getAntiDebtRatio() >= 1.0) {
                    System.out.println(Ansi.colorize(String.valueOf(stock.getAntiDebtRatio()), Attribute.BRIGHT_YELLOW_TEXT()));

                } else if (stock.getAntiDebtRatio() >= 0.9) {
                    System.out.println(Ansi.colorize(String.valueOf(stock.getAntiDebtRatio()), Attribute.RED_TEXT()));

                } else if (stock.getAntiDebtRatio() >= 0.85) {
                    System.out.println(Ansi.colorize(String.valueOf(stock.getAntiDebtRatio()), Attribute.BLACK_TEXT(), Attribute.RED_BACK()));

                } else if (stock.getAntiDebtRatio() < 0.85) {
                    System.out.println(Ansi.colorize(String.valueOf(stock.getAntiDebtRatio()), Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK()));
                }
            }

            // Average Margins
            if (stock.getProfitMargins() != null && stock.getGrossMargins() != null
                    && stock.getEbitdaMargins() != null && stock.getOperatingMargins() != null) {
                stock.setProfitMargins(formatDouble(stock.getProfitMargins() * 100));
                stock.setGrossMargins(formatDouble(stock.getGrossMargins() * 100));
                stock.setEbitdaMargins(formatDouble(stock.getEbitdaMargins() * 100));
                stock.setOperatingMargins(formatDouble(stock.getOperatingMargins() * 100));
                stock.setAverageMargins(formatDouble((stock.getProfitMargins() + stock.getGrossMargins()
                        + stock.getEbitdaMargins() + stock.getOperatingMargins()) / 4));
                System.out.print(Ansi.colorize("Average Margins: ", Attribute.BOLD()));

                if (stock.getAverageMargins() >= 50.0) {
                    System.out.println(Ansi.colorize(stock.getAverageMargins() + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (stock.getAverageMargins() >= 30.0) {
                    System.out.println(Ansi.colorize(stock.getAverageMargins() + "%", Attribute.BLACK_TEXT(), Attribute.GREEN_BACK()));

                } else if (stock.getAverageMargins() >= 20.0) {
                    System.out.println(Ansi.colorize(stock.getAverageMargins() + "%", Attribute.BRIGHT_GREEN_TEXT()));

                } else if (stock.getAverageMargins() >= 10.0) {
                    System.out.println(Ansi.colorize(stock.getAverageMargins() + "%", Attribute.GREEN_TEXT()));

                } else if (stock.getAverageMargins() > 0.0) {
                    System.out.println(Ansi.colorize(stock.getAverageMargins() + "%", Attribute.BRIGHT_YELLOW_TEXT()));

                } else if (stock.getAverageMargins() >= -5.0) {
                    System.out.println(Ansi.colorize(stock.getAverageMargins() + "%", Attribute.RED_TEXT()));

                } else if (stock.getAverageMargins() >= -10.0) {
                    System.out.println(Ansi.colorize(stock.getAverageMargins() + "%", Attribute.BLACK_TEXT(), Attribute.RED_BACK()));

                } else {
                    System.out.println(Ansi.colorize(stock.getAverageMargins() + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK()));
                }
            }

            // Return On Assets
            if (stock.getReturnOnAssets() != null) {
                stock.setReturnOnAssets(formatDouble(stock.getReturnOnAssets() * 100));
                System.out.print(Ansi.colorize("Return On Assets: ", Attribute.BOLD()));

                if (stock.getReturnOnAssets() >= 50.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnAssets() + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (stock.getReturnOnAssets() >= 30.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnAssets() + "%", Attribute.BLACK_TEXT(), Attribute.GREEN_BACK()));

                } else if (stock.getReturnOnAssets() >= 20.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnAssets() + "%", Attribute.BRIGHT_GREEN_TEXT()));

                } else if (stock.getReturnOnAssets() >= 10.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnAssets() + "%", Attribute.GREEN_TEXT()));

                } else if (stock.getReturnOnAssets() > 0.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnAssets() + "%", Attribute.BRIGHT_YELLOW_TEXT()));

                } else if (stock.getReturnOnAssets() >= -5.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnAssets() + "%", Attribute.RED_TEXT()));

                } else if (stock.getReturnOnAssets() >= -10.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnAssets() + "%", Attribute.BLACK_TEXT(), Attribute.RED_BACK()));

                } else {
                    System.out.println(Ansi.colorize(stock.getReturnOnAssets() + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK()));
                }
            }

            // Return On Equity
            if (stock.getReturnOnEquity() != null) {
                stock.setReturnOnEquity(formatDouble(stock.getReturnOnEquity() * 100));
                System.out.print(Ansi.colorize("Return On Equity: ", Attribute.BOLD()));

                if (stock.getReturnOnEquity() >= 50.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnEquity() + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (stock.getReturnOnEquity() >= 30.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnEquity() + "%", Attribute.BLACK_TEXT(), Attribute.GREEN_BACK()));

                } else if (stock.getReturnOnEquity() >= 20.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnEquity() + "%", Attribute.BRIGHT_GREEN_TEXT()));

                } else if (stock.getReturnOnEquity() >= 10.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnEquity() + "%", Attribute.GREEN_TEXT()));

                } else if (stock.getReturnOnEquity() > 0.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnEquity() + "%", Attribute.BRIGHT_YELLOW_TEXT()));

                } else if (stock.getReturnOnEquity() >= -5.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnEquity() + "%", Attribute.RED_TEXT()));

                } else if (stock.getReturnOnEquity() >= -10.0) {
                    System.out.println(Ansi.colorize(stock.getReturnOnEquity() + "%", Attribute.BLACK_TEXT(), Attribute.RED_BACK()));

                } else {
                    System.out.println(Ansi.colorize(stock.getReturnOnEquity() + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK()));
                }
            }

            // Revenue Growth
            if (stock.getRevenueGrowth() != null) {
                stock.setRevenueGrowth(formatDouble(stock.getRevenueGrowth() * 100));
                System.out.print(Ansi.colorize("Revenue Growth: ", Attribute.BOLD()));

                if (stock.getRevenueGrowth() >= 50.0) {
                    System.out.println(Ansi.colorize(stock.getRevenueGrowth() + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_GREEN_BACK()));

                } else if (stock.getRevenueGrowth() >= 30.0) {
                    System.out.println(Ansi.colorize(stock.getRevenueGrowth() + "%", Attribute.BLACK_TEXT(), Attribute.GREEN_BACK()));

                } else if (stock.getRevenueGrowth() >= 20.0) {
                    System.out.println(Ansi.colorize(stock.getRevenueGrowth() + "%", Attribute.BRIGHT_GREEN_TEXT()));

                } else if (stock.getRevenueGrowth() >= 10.0) {
                    System.out.println(Ansi.colorize(stock.getRevenueGrowth() + "%", Attribute.GREEN_TEXT()));

                } else if (stock.getRevenueGrowth() > 0.0) {
                    System.out.println(Ansi.colorize(stock.getRevenueGrowth() + "%", Attribute.BRIGHT_YELLOW_TEXT()));

                } else if (stock.getRevenueGrowth() >= -5.0) {
                    System.out.println(Ansi.colorize(stock.getRevenueGrowth() + "%", Attribute.RED_TEXT()));

                } else if (stock.getRevenueGrowth() >= -10.0) {
                    System.out.println(Ansi.colorize(stock.getRevenueGrowth() + "%", Attribute.BLACK_TEXT(), Attribute.RED_BACK()));

                } else {
                    System.out.println(Ansi.colorize(stock.getRevenueGrowth() + "%", Attribute.BLACK_TEXT(), Attribute.BRIGHT_RED_BACK()));
                }
            }

            // Prints a Google link to the stock for more information.
            System.out.println();
            System.out.println(Ansi.colorize("https://google.com/search?q=" + stock.getSymbol() + "+stock", Attribute.CYAN_TEXT()));
            System.out.println();

            if (stock.getAntiDebtRatio() < 0.85) {
                double grossProfits = BigDecimal.valueOf(stock.getGrossProfits()).doubleValue();
                double totalDebt = BigDecimal.valueOf(stock.getTotalDebt()).doubleValue();

                if (grossProfits < totalDebt) {
                    warning = true;
                    System.out.println(Ansi.colorize("Warning: Bad debt management.", Attribute.RED_TEXT(), Attribute.BOLD()));
                }
            }

            if (stock.getPriceToBook() < 0.0) {
                warning = true;
                System.out.println(Ansi.colorize("Warning: Price-to-Book ratio is negative.", Attribute.RED_TEXT(), Attribute.BOLD()));
            }

            if (stock.getOperatingCashflow() < 0.0) {
                warning = true;
                System.out.println(Ansi.colorize("Warning: Operating Cashflow is negative.", Attribute.RED_TEXT(), Attribute.BOLD()));
            }

            if (stock.getFreeCashflow() < 0.0) {
                warning = true;
                System.out.println(Ansi.colorize("Warning: Free Cashflow is negative.", Attribute.RED_TEXT(), Attribute.BOLD()));
            }

            if (stock.getEnterpriseValue() < 0.0) {
                warning = true;
                System.out.println(Ansi.colorize("Warning: Enterprise Value is negative.", Attribute.RED_TEXT(), Attribute.BOLD()));
            }

            if (!(stock.getProfitMargins() > 0 && stock.getGrossMargins() > 0 && stock.getEbitdaMargins() > 0
                    && stock.getOperatingMargins() > 0 && stock.getReturnOnAssets() > 0 && stock.getReturnOnEquity() > 0
                    && stock.getRevenueGrowth() > 0)) {
                warning = true;
                System.out.println(Ansi.colorize("Warning: Bad profit margins.", Attribute.RED_TEXT(), Attribute.BOLD()));
            }

            if (change1d < -20 || change5d < -15 || change1m < -10) {
                warning = true;
                System.out.println(Ansi.colorize("Warning: Bad short-term stock performance.", Attribute.RED_TEXT(), Attribute.BOLD()));
            }

            if (change6m < 0 || change12m < 0) {
                warning = true;
                System.out.println(Ansi.colorize("Warning: Bad long-term stock performance.", Attribute.RED_TEXT(), Attribute.BOLD()));
            }

            if (change6m <= 5 || change12m <= 10) {
                warning = true;
                System.out.println(Ansi.colorize("Warning: Low stock growth.", Attribute.RED_TEXT(), Attribute.BOLD()));
            }

            if (!warning) {
                goodStocks.add(stock.getSymbol());
                //System.out.println();
                //System.out.println("Press the \"ENTER\" key to continue...");
                //Scanner s = new Scanner(System.in);
                //s.nextLine();
            } else {
                TimeUnit.MILLISECONDS.sleep(2500);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void printStockError(Stock stock) {
        System.out.println();
        System.out.println(Ansi.colorize("This stock, " + stock.getName() + ", is missing necessary information.", Attribute.BOLD(), Attribute.RED_TEXT()));
        System.out.println(Ansi.colorize("https://google.com/search?q=" + stock.getSymbol() + "+stock", Attribute.CYAN_TEXT()));
        System.out.println();
    }
}
