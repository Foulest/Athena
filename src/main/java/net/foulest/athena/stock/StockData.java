package net.foulest.athena.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.foulest.athena.util.RedirectableRequest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class StockData {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Sends the request to Yahoo Finance and parses the result.
     */
    public static Stock getStockData(String symbol) throws IOException {
        Stock stock = new Stock(symbol);
        Map<String, JsonNode> dataPoints = new HashMap<>();
        String urlBase = "https://query1.finance.yahoo.com/v11/finance/quoteSummary/" + symbol + "?modules=";

        if (grabJson(new URL(urlBase + "price")) != null) {
            dataPoints.put("price", grabJson(new URL(urlBase + "price")));
        }

        if (grabJson(new URL(urlBase + "summaryDetail")) != null) {
            dataPoints.put("summaryDetail", grabJson(new URL(urlBase + "summaryDetail")));
        }

        if (grabJson(new URL(urlBase + "assetProfile")) != null) {
            dataPoints.put("assetProfile", grabJson(new URL(urlBase + "assetProfile")));
        }

        if (grabJson(new URL(urlBase + "financialData")) != null) {
            dataPoints.put("financialData", grabJson(new URL(urlBase + "financialData")));
        }

        if (grabJson(new URL(urlBase + "defaultKeyStatistics")) != null) {
            dataPoints.put("defaultKeyStatistics", grabJson(new URL(urlBase + "defaultKeyStatistics")));
        }

        if (grabJson(new URL(urlBase + "quoteType")) != null) {
            dataPoints.put("quoteType", grabJson(new URL(urlBase + "quoteType")));
        }

        for (Map.Entry<String, JsonNode> node : dataPoints.entrySet()) {
            String key = node.getKey();
            JsonNode value = node.getValue();

            if (value.has("quoteSummary") && value.get("quoteSummary").has("result")) {
                JsonNode json = value.get("quoteSummary").get("result").get(0).get(key);

                switch (key) {
                    case "price" -> {
                        stock.setCurrency(json.get("currency") == null ? null : json.get("currency").asText());
                        stock.setCurrencySymbol(json.get("currencySymbol") == null ? null : json.get("currencySymbol").asText());
                        stock.setMarketState(json.get("marketState") == null ? null : json.get("marketState").asText());
                    }

                    case "summaryDetail" -> {
                        stock.setAsk(json.get("ask").get("raw") == null ? null : json.get("ask").get("raw").asDouble());
                        stock.setAskSize(json.get("askSize").get("raw") == null ? null : json.get("askSize").get("raw").asDouble());
                        stock.setAverageDailyVolume10Days(json.get("averageDailyVolume10Day").get("raw") == null ? null : json.get("averageDailyVolume10Day").get("raw").asDouble());
                        stock.setAverageVolume(json.get("averageVolume").get("raw") == null ? null : json.get("averageVolume").get("raw").asDouble());
                        stock.setAverageVolume10Days(json.get("averageVolume10days").get("raw") == null ? null : json.get("averageVolume10days").get("raw").asDouble());
                        stock.setBeta(json.get("beta").get("raw") == null ? null : json.get("beta").get("raw").asDouble());
                        stock.setBid(json.get("bid").get("raw") == null ? null : json.get("bid").get("raw").asDouble());
                        stock.setBidSize(json.get("bidSize").get("raw") == null ? null : json.get("bidSize").get("raw").asDouble());
                        stock.setDayHigh(json.get("dayHigh").get("raw") == null ? null : json.get("dayHigh").get("raw").asDouble());
                        stock.setDayLow(json.get("dayLow").get("raw") == null ? null : json.get("dayLow").get("raw").asDouble());
                        stock.setFiftyDayAverage(json.get("fiftyDayAverage").get("raw") == null ? null : json.get("fiftyDayAverage").get("raw").asDouble());
                        stock.setFiftyTwoWeekHigh(json.get("fiftyTwoWeekHigh").get("raw") == null ? null : json.get("fiftyTwoWeekHigh").get("raw").asDouble());
                        stock.setFiftyTwoWeekLow(json.get("fiftyTwoWeekLow").get("raw") == null ? null : json.get("fiftyTwoWeekLow").get("raw").asDouble());
                        stock.setForwardPE(json.get("forwardPE").get("raw") == null ? null : json.get("forwardPE").get("raw").asDouble());
                        stock.setMarketCap(json.get("marketCap").get("raw") == null ? null : json.get("marketCap").get("raw").asDouble());
                        stock.setOpen(json.get("open").get("raw") == null ? null : json.get("open").get("raw").asDouble());
                        stock.setPreviousClose(json.get("previousClose").get("raw") == null ? null : json.get("previousClose").get("raw").asDouble());
                        stock.setPriceToSalesTrailing12Months(json.get("priceToSalesTrailing12Months").get("raw") == null ? null : json.get("priceToSalesTrailing12Months").get("raw").asDouble());
                        stock.setTrailingPE(json.get("trailingPE") == null ? null : json.get("trailingPE").asDouble());
                        stock.setTwoHundredDayAverage(json.get("twoHundredDayAverage").get("raw") == null ? null : json.get("twoHundredDayAverage").get("raw").asDouble());
                        stock.setVolume(json.get("volume").get("raw") == null ? null : json.get("volume").get("raw").asDouble());
                    }

                    case "assetProfile" -> {
                        stock.setAddress(json.get("address1") == null ? null : json.get("address1").asText());
                        stock.setCity(json.get("city") == null ? null : json.get("city").asText());
                        stock.setState(json.get("state") == null ? null : json.get("state").asText());
                        stock.setZip(json.get("zip") == null ? null : json.get("zip").asText());
                        stock.setCountry(json.get("country") == null ? null : json.get("country").asText());
                        stock.setPhoneNumber(json.get("phone") == null ? null : json.get("phone").asText());
                        stock.setWebsite(json.get("website") == null ? null : json.get("website").asText());
                        stock.setIndustry(json.get("industry") == null ? null : json.get("industry").asText());
                        stock.setSector(json.get("sector") == null ? null : json.get("sector").asText());
                        stock.setBusinessSummary(json.get("longBusinessSummary") == null ? null : json.get("longBusinessSummary").asText());
                        stock.setFullTimeEmployees(json.get("fullTimeEmployees") == null ? null : json.get("fullTimeEmployees").asInt());
                        stock.setAuditRisk(json.get("auditRisk") == null ? null : json.get("auditRisk").asInt());
                        stock.setBoardRisk(json.get("boardRisk") == null ? null : json.get("boardRisk").asInt());
                        stock.setCompensationRisk(json.get("compensationRisk") == null ? null : json.get("compensationRisk").asInt());
                        stock.setShareHolderRightsRisk(json.get("shareHolderRightsRisk") == null ? null : json.get("shareHolderRightsRisk").asInt());
                        stock.setOverallRisk(json.get("overallRisk") == null ? null : json.get("overallRisk").asInt());
                    }

                    case "financialData" -> {
                        stock.setClose(json.get("currentPrice").get("raw") == null ? null : json.get("currentPrice").get("raw").asDouble());
                        stock.setTargetHighPrice(json.get("targetHighPrice").get("raw") == null ? null : json.get("targetHighPrice").get("raw").asDouble());
                        stock.setTargetLowPrice(json.get("targetLowPrice").get("raw") == null ? null : json.get("targetLowPrice").get("raw").asDouble());
                        stock.setTargetMeanPrice(json.get("targetMeanPrice").get("raw") == null ? null : json.get("targetMeanPrice").get("raw").asDouble());
                        stock.setTargetMedianPrice(json.get("targetMedianPrice").get("raw") == null ? null : json.get("targetMedianPrice").get("raw").asDouble());
                        stock.setAnalystScore(json.get("recommendationMean").get("raw") == null ? null : json.get("recommendationMean").get("raw").asDouble());
                        stock.setAnalystRating(json.get("recommendationKey") == null ? null : json.get("recommendationKey").asText().substring(0, 1).toUpperCase() + json.get("recommendationKey").asText().substring(1));
                        stock.setAnalystCount(json.get("numberOfAnalystOpinions") == null ? null : json.get("numberOfAnalystOpinions").asInt());
                        stock.setTotalCash(json.get("totalCash").get("raw") == null ? null : json.get("totalCash").get("raw").asDouble());
                        stock.setTotalCashPerShare(json.get("totalCashPerShare").get("raw") == null ? null : json.get("totalCashPerShare").get("raw").asDouble());
                        stock.setEbitda(json.get("ebitda").get("raw") == null ? null : json.get("ebitda").get("raw").asDouble());
                        stock.setTotalDebt(json.get("totalDebt").get("raw") == null ? null : json.get("totalDebt").get("raw").asDouble());
                        stock.setQuickRatio(json.get("quickRatio").get("raw") == null ? null : json.get("quickRatio").get("raw").asDouble());
                        stock.setCurrentRatio(json.get("currentRatio").get("raw") == null ? null : json.get("currentRatio").get("raw").asDouble());
                        stock.setTotalRevenue(json.get("totalRevenue").get("raw") == null ? null : json.get("totalRevenue").get("raw").asDouble());
                        stock.setDebtToEquity(json.get("debtToEquity").get("raw") == null ? null : json.get("debtToEquity").get("raw").asDouble());
                        stock.setRevenuePerShare(json.get("revenuePerShare").get("raw") == null ? null : json.get("revenuePerShare").get("raw").asDouble());
                        stock.setReturnOnAssets(json.get("returnOnAssets").get("raw") == null ? null : json.get("returnOnAssets").get("raw").asDouble());
                        stock.setReturnOnEquity(json.get("returnOnEquity").get("raw") == null ? null : json.get("returnOnEquity").get("raw").asDouble());
                        stock.setGrossProfits(json.get("grossProfits").get("raw") == null ? null : json.get("grossProfits").get("raw").asDouble());
                        stock.setFreeCashflow(json.get("freeCashflow").get("raw") == null ? null : json.get("freeCashflow").get("raw").asDouble());
                        stock.setOperatingCashflow(json.get("operatingCashflow").get("raw") == null ? null : json.get("operatingCashflow").get("raw").asDouble());
                        stock.setEarningsGrowth(json.get("earningsGrowth").get("raw") == null ? null : json.get("earningsGrowth").get("raw").asDouble());
                        stock.setRevenueGrowth(json.get("revenueGrowth").get("raw") == null ? null : json.get("revenueGrowth").get("raw").asDouble());
                        stock.setGrossMargins(json.get("grossMargins").get("raw") == null ? null : json.get("grossMargins").get("raw").asDouble());
                        stock.setEbitdaMargins(json.get("ebitdaMargins").get("raw") == null ? null : json.get("ebitdaMargins").get("raw").asDouble());
                        stock.setOperatingMargins(json.get("operatingMargins").get("raw") == null ? null : json.get("operatingMargins").get("raw").asDouble());
                        stock.setProfitMargins(json.get("profitMargins").get("raw") == null ? null : json.get("profitMargins").get("raw").asDouble());
                    }

                    case "defaultKeyStatistics" -> {
                        stock.setEnterpriseValue(json.get("enterpriseValue").get("raw") == null ? null : json.get("enterpriseValue").get("raw").asDouble());
                        stock.setFloatShares(json.get("floatShares") == null ? null : json.get("floatShares").asDouble());
                        stock.setSharesOutstanding(json.get("sharesOutstanding") == null ? null : json.get("sharesOutstanding").asDouble());
                        stock.setSharesShort(json.get("sharesShort") == null ? null : json.get("sharesShort").asDouble());
                        stock.setSharesShortPriorMonth(json.get("sharesShortPriorMonth") == null ? null : json.get("sharesShortPriorMonth").asDouble());
                        stock.setSharesShortPreviousMonthDate(json.get("sharesShortPreviousMonthDate") == null ? null : json.get("sharesShortPreviousMonthDate").asLong());
                        stock.setDateShortInterest(json.get("dateShortInterest") == null ? null : json.get("dateShortInterest").asLong());
                        stock.setSharesPercentSharesOut(json.get("sharesPercentSharesOut") == null ? null : json.get("sharesPercentSharesOut").asDouble());
                        stock.setHeldPercentInsiders(json.get("heldPercentInsiders") == null ? null : json.get("heldPercentInsiders").asDouble());
                        stock.setHeldPercentInstitutions(json.get("heldPercentInstitutions") == null ? null : json.get("heldPercentInstitutions").asDouble());
                        stock.setShortRatio(json.get("shortRatio") == null ? null : json.get("shortRatio").asDouble());
                        stock.setImpliedSharesOutstanding(json.get("impliedSharesOutstanding") == null ? null : json.get("impliedSharesOutstanding").asInt());
                        stock.setBookValue(json.get("bookValue").get("raw") == null ? null : json.get("bookValue").get("raw").asDouble());
                        stock.setPriceToBook(json.get("priceToBook").get("raw") == null ? null : json.get("priceToBook").get("raw").asDouble());
                        stock.setLastFiscalYearEnd(json.get("lastFiscalYearEnd") == null ? null : json.get("lastFiscalYearEnd").asLong());
                        stock.setNextFiscalYearEnd(json.get("nextFiscalYearEnd") == null ? null : json.get("nextFiscalYearEnd").asLong());
                        stock.setMostRecentQuarter(json.get("mostRecentQuarter") == null ? null : json.get("mostRecentQuarter").asLong());
                        stock.setEarningsQuarterlyGrowth(json.get("earningsQuarterlyGrowth").get("raw") == null ? null : json.get("earningsQuarterlyGrowth").get("raw").asDouble());
                        stock.setNetIncomeToCommon(json.get("netIncomeToCommon") == null ? null : json.get("netIncomeToCommon").asInt());
                        stock.setTrailingEPS(json.get("trailingEps").get("raw") == null ? null : json.get("trailingEps").get("raw").asDouble());
                        stock.setForwardEPS(json.get("forwardEps").get("raw") == null ? null : json.get("forwardEps").get("raw").asDouble());
                        stock.setPegRatio(json.get("pegRatio").get("raw") == null ? null : json.get("pegRatio").get("raw").asDouble());
                        stock.setLastSplitFactor(json.get("lastSplitFactor") == null ? null : json.get("lastSplitFactor").asText());
                        stock.setLastSplitDate(json.get("lastSplitDate") == null ? null : json.get("lastSplitDate").asLong());
                        stock.setEnterpriseToRevenue(json.get("enterpriseToRevenue").get("raw") == null ? null : json.get("enterpriseToRevenue").get("raw").asDouble());
                        stock.setEnterpriseToEbitda(json.get("enterpriseToEbitda").get("raw") == null ? null : json.get("enterpriseToEbitda").get("raw").asDouble());
                        stock.setChange52Weeks(json.get("52WeekChange") == null ? null : json.get("52WeekChange").asDouble());
                        stock.setChange52WeeksSP500(json.get("SandP52WeekChange").get("raw") == null ? null : json.get("SandP52WeekChange").get("raw").asDouble());
                    }

                    case "quoteType" -> {
                        stock.setShortName(json.get("shortName") == null ? null : json.get("shortName").asText());
                        stock.setName(json.get("longName") == null ? null : json.get("longName").asText());
                        stock.setTimezone(json.get("timeZoneFullName") == null ? null : json.get("timeZoneFullName").asText());
                    }

                    default -> {
                    }
                }

            } else {
                throw new IOException("Invalid response");
            }
        }

        return stock;
    }

    public static JsonNode grabJson(URL url) {
        try {
            RedirectableRequest redirectableRequest = new RedirectableRequest(url, 5);
            redirectableRequest.setConnectTimeout(10000);
            redirectableRequest.setReadTimeout(10000);
            URLConnection connection = redirectableRequest.openConnection();
            InputStreamReader is = new InputStreamReader(connection.getInputStream());
            return objectMapper.readTree(is);

        } catch (Exception ignored) {
            return null;
        }
    }
}
