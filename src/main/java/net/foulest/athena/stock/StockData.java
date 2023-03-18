package net.foulest.athena.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.foulest.athena.util.RedirectableRequest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class StockData {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Sends the request to Yahoo Finance and parses the result.
     */
    public static Stock getStockData(String symbol) throws IOException {
        Stock stock = new Stock(symbol);
        String urlBase = "https://query1.finance.yahoo.com/v11/finance/quoteSummary/" + symbol + "?modules=";
        String modules = "price,summaryDetail,assetProfile,financialData,defaultKeyStatistics,quoteType";
        URL url = new URL(urlBase + modules);
        JsonNode data = grabJson(url);

        if (data != null && data.has("quoteSummary") && data.get("quoteSummary").has("result")) {
            JsonNode result = data.get("quoteSummary").get("result").get(0);

            // Price
            JsonNode price = result.get("price");
            stock.setCurrency(price.get("currency") == null ? null : price.get("currency").asText());
            stock.setCurrencySymbol(price.get("currencySymbol") == null ? null : price.get("currencySymbol").asText());
            stock.setMarketState(price.get("marketState") == null ? null : price.get("marketState").asText());

            // Summary Detail
            JsonNode summaryDetail = result.get("summaryDetail");
            stock.setAsk(summaryDetail.get("ask").get("raw") == null ? null : summaryDetail.get("ask").get("raw").asDouble());
            stock.setAskSize(summaryDetail.get("askSize").get("raw") == null ? null : summaryDetail.get("askSize").get("raw").asDouble());
            stock.setAverageDailyVolume10Days(summaryDetail.get("averageDailyVolume10Day").get("raw") == null ? null : summaryDetail.get("averageDailyVolume10Day").get("raw").asDouble());
            stock.setAverageVolume(summaryDetail.get("averageVolume").get("raw") == null ? null : summaryDetail.get("averageVolume").get("raw").asDouble());
            stock.setAverageVolume10Days(summaryDetail.get("averageVolume10days").get("raw") == null ? null : summaryDetail.get("averageVolume10days").get("raw").asDouble());
            stock.setBeta(summaryDetail.get("beta").get("raw") == null ? null : summaryDetail.get("beta").get("raw").asDouble());
            stock.setBid(summaryDetail.get("bid").get("raw") == null ? null : summaryDetail.get("bid").get("raw").asDouble());
            stock.setBidSize(summaryDetail.get("bidSize").get("raw") == null ? null : summaryDetail.get("bidSize").get("raw").asDouble());
            stock.setDayHigh(summaryDetail.get("dayHigh").get("raw") == null ? null : summaryDetail.get("dayHigh").get("raw").asDouble());
            stock.setDayLow(summaryDetail.get("dayLow").get("raw") == null ? null : summaryDetail.get("dayLow").get("raw").asDouble());
            stock.setFiftyDayAverage(summaryDetail.get("fiftyDayAverage").get("raw") == null ? null : summaryDetail.get("fiftyDayAverage").get("raw").asDouble());
            stock.setFiftyTwoWeekHigh(summaryDetail.get("fiftyTwoWeekHigh").get("raw") == null ? null : summaryDetail.get("fiftyTwoWeekHigh").get("raw").asDouble());
            stock.setFiftyTwoWeekLow(summaryDetail.get("fiftyTwoWeekLow").get("raw") == null ? null : summaryDetail.get("fiftyTwoWeekLow").get("raw").asDouble());
            stock.setForwardPE(summaryDetail.get("forwardPE").get("raw") == null ? null : summaryDetail.get("forwardPE").get("raw").asDouble());
            stock.setMarketCap(summaryDetail.get("marketCap").get("raw") == null ? null : summaryDetail.get("marketCap").get("raw").asDouble());
            stock.setOpen(summaryDetail.get("open").get("raw") == null ? null : summaryDetail.get("open").get("raw").asDouble());
            stock.setPreviousClose(summaryDetail.get("previousClose").get("raw") == null ? null : summaryDetail.get("previousClose").get("raw").asDouble());
            stock.setPriceToSalesTrailing12Months(summaryDetail.get("priceToSalesTrailing12Months").get("raw") == null ? null : summaryDetail.get("priceToSalesTrailing12Months").get("raw").asDouble());
            stock.setTrailingPE(summaryDetail.get("trailingPE") == null ? null : summaryDetail.get("trailingPE").asDouble());
            stock.setTwoHundredDayAverage(summaryDetail.get("twoHundredDayAverage").get("raw") == null ? null : summaryDetail.get("twoHundredDayAverage").get("raw").asDouble());
            stock.setVolume(summaryDetail.get("volume").get("raw") == null ? null : summaryDetail.get("volume").get("raw").asDouble());

            // Asset Profile
            JsonNode assetProfile = result.get("assetProfile");
            stock.setAddress(assetProfile.get("address1") == null ? null : assetProfile.get("address1").asText());
            stock.setCity(assetProfile.get("city") == null ? null : assetProfile.get("city").asText());
            stock.setState(assetProfile.get("state") == null ? null : assetProfile.get("state").asText());
            stock.setZip(assetProfile.get("zip") == null ? null : assetProfile.get("zip").asText());
            stock.setCountry(assetProfile.get("country") == null ? null : assetProfile.get("country").asText());
            stock.setPhoneNumber(assetProfile.get("phone") == null ? null : assetProfile.get("phone").asText());
            stock.setWebsite(assetProfile.get("website") == null ? null : assetProfile.get("website").asText());
            stock.setIndustry(assetProfile.get("industry") == null ? null : assetProfile.get("industry").asText());
            stock.setSector(assetProfile.get("sector") == null ? null : assetProfile.get("sector").asText());
            stock.setBusinessSummary(assetProfile.get("longBusinessSummary") == null ? null : assetProfile.get("longBusinessSummary").asText());
            stock.setFullTimeEmployees(assetProfile.get("fullTimeEmployees") == null ? null : assetProfile.get("fullTimeEmployees").asInt());
            stock.setAuditRisk(assetProfile.get("auditRisk") == null ? null : assetProfile.get("auditRisk").asInt());
            stock.setBoardRisk(assetProfile.get("boardRisk") == null ? null : assetProfile.get("boardRisk").asInt());
            stock.setCompensationRisk(assetProfile.get("compensationRisk") == null ? null : assetProfile.get("compensationRisk").asInt());
            stock.setShareHolderRightsRisk(assetProfile.get("shareHolderRightsRisk") == null ? null : assetProfile.get("shareHolderRightsRisk").asInt());
            stock.setOverallRisk(assetProfile.get("overallRisk") == null ? null : assetProfile.get("overallRisk").asInt());

            // Financial Data
            JsonNode financialData = result.get("financialData");
            stock.setClose(financialData.get("currentPrice").get("raw") == null ? null : financialData.get("currentPrice").get("raw").asDouble());
            stock.setTargetHighPrice(financialData.get("targetHighPrice").get("raw") == null ? null : financialData.get("targetHighPrice").get("raw").asDouble());
            stock.setTargetLowPrice(financialData.get("targetLowPrice").get("raw") == null ? null : financialData.get("targetLowPrice").get("raw").asDouble());
            stock.setTargetMeanPrice(financialData.get("targetMeanPrice").get("raw") == null ? null : financialData.get("targetMeanPrice").get("raw").asDouble());
            stock.setTargetMedianPrice(financialData.get("targetMedianPrice").get("raw") == null ? null : financialData.get("targetMedianPrice").get("raw").asDouble());
            stock.setAnalystScore(financialData.get("recommendationMean").get("raw") == null ? null : financialData.get("recommendationMean").get("raw").asDouble());
            stock.setAnalystRating(financialData.get("recommendationKey") == null ? null : financialData.get("recommendationKey").asText().substring(0, 1).toUpperCase() + financialData.get("recommendationKey").asText().substring(1));
            stock.setAnalystCount(financialData.get("numberOfAnalystOpinions") == null ? null : financialData.get("numberOfAnalystOpinions").asInt());
            stock.setTotalCash(financialData.get("totalCash").get("raw") == null ? null : financialData.get("totalCash").get("raw").asDouble());
            stock.setTotalCashPerShare(financialData.get("totalCashPerShare").get("raw") == null ? null : financialData.get("totalCashPerShare").get("raw").asDouble());
            stock.setEbitda(financialData.get("ebitda").get("raw") == null ? null : financialData.get("ebitda").get("raw").asDouble());
            stock.setTotalDebt(financialData.get("totalDebt").get("raw") == null ? null : financialData.get("totalDebt").get("raw").asDouble());
            stock.setQuickRatio(financialData.get("quickRatio").get("raw") == null ? null : financialData.get("quickRatio").get("raw").asDouble());
            stock.setCurrentRatio(financialData.get("currentRatio").get("raw") == null ? null : financialData.get("currentRatio").get("raw").asDouble());
            stock.setTotalRevenue(financialData.get("totalRevenue").get("raw") == null ? null : financialData.get("totalRevenue").get("raw").asDouble());
            stock.setDebtToEquity(financialData.get("debtToEquity").get("raw") == null ? null : financialData.get("debtToEquity").get("raw").asDouble());
            stock.setRevenuePerShare(financialData.get("revenuePerShare").get("raw") == null ? null : financialData.get("revenuePerShare").get("raw").asDouble());
            stock.setReturnOnAssets(financialData.get("returnOnAssets").get("raw") == null ? null : financialData.get("returnOnAssets").get("raw").asDouble());
            stock.setReturnOnEquity(financialData.get("returnOnEquity").get("raw") == null ? null : financialData.get("returnOnEquity").get("raw").asDouble());
            stock.setGrossProfits(financialData.get("grossProfits").get("raw") == null ? null : financialData.get("grossProfits").get("raw").asDouble());
            stock.setFreeCashflow(financialData.get("freeCashflow").get("raw") == null ? null : financialData.get("freeCashflow").get("raw").asDouble());
            stock.setOperatingCashflow(financialData.get("operatingCashflow").get("raw") == null ? null : financialData.get("operatingCashflow").get("raw").asDouble());
            stock.setEarningsGrowth(financialData.get("earningsGrowth").get("raw") == null ? null : financialData.get("earningsGrowth").get("raw").asDouble());
            stock.setRevenueGrowth(financialData.get("revenueGrowth").get("raw") == null ? null : financialData.get("revenueGrowth").get("raw").asDouble());
            stock.setGrossMargins(financialData.get("grossMargins").get("raw") == null ? null : financialData.get("grossMargins").get("raw").asDouble());
            stock.setEbitdaMargins(financialData.get("ebitdaMargins").get("raw") == null ? null : financialData.get("ebitdaMargins").get("raw").asDouble());
            stock.setOperatingMargins(financialData.get("operatingMargins").get("raw") == null ? null : financialData.get("operatingMargins").get("raw").asDouble());
            stock.setProfitMargins(financialData.get("profitMargins").get("raw") == null ? null : financialData.get("profitMargins").get("raw").asDouble());

            // Default Key Statistics
            JsonNode defaultKeyStatistics = result.get("defaultKeyStatistics");
            stock.setEnterpriseValue(defaultKeyStatistics.get("enterpriseValue").get("raw") == null ? null : defaultKeyStatistics.get("enterpriseValue").get("raw").asDouble());
            stock.setFloatShares(defaultKeyStatistics.get("floatShares") == null ? null : defaultKeyStatistics.get("floatShares").asDouble());
            stock.setSharesOutstanding(defaultKeyStatistics.get("sharesOutstanding") == null ? null : defaultKeyStatistics.get("sharesOutstanding").asDouble());
            stock.setSharesShort(defaultKeyStatistics.get("sharesShort") == null ? null : defaultKeyStatistics.get("sharesShort").asDouble());
            stock.setSharesShortPriorMonth(defaultKeyStatistics.get("sharesShortPriorMonth") == null ? null : defaultKeyStatistics.get("sharesShortPriorMonth").asDouble());
            stock.setSharesShortPreviousMonthDate(defaultKeyStatistics.get("sharesShortPreviousMonthDate") == null ? null : defaultKeyStatistics.get("sharesShortPreviousMonthDate").asLong());
            stock.setDateShortInterest(defaultKeyStatistics.get("dateShortInterest") == null ? null : defaultKeyStatistics.get("dateShortInterest").asLong());
            stock.setSharesPercentSharesOut(defaultKeyStatistics.get("sharesPercentSharesOut") == null ? null : defaultKeyStatistics.get("sharesPercentSharesOut").asDouble());
            stock.setHeldPercentInsiders(defaultKeyStatistics.get("heldPercentInsiders") == null ? null : defaultKeyStatistics.get("heldPercentInsiders").asDouble());
            stock.setHeldPercentInstitutions(defaultKeyStatistics.get("heldPercentInstitutions") == null ? null : defaultKeyStatistics.get("heldPercentInstitutions").asDouble());
            stock.setShortRatio(defaultKeyStatistics.get("shortRatio") == null ? null : defaultKeyStatistics.get("shortRatio").asDouble());
            stock.setImpliedSharesOutstanding(defaultKeyStatistics.get("impliedSharesOutstanding") == null ? null : defaultKeyStatistics.get("impliedSharesOutstanding").asInt());
            stock.setBookValue(defaultKeyStatistics.get("bookValue").get("raw") == null ? null : defaultKeyStatistics.get("bookValue").get("raw").asDouble());
            stock.setPriceToBook(defaultKeyStatistics.get("priceToBook").get("raw") == null ? null : defaultKeyStatistics.get("priceToBook").get("raw").asDouble());
            stock.setLastFiscalYearEnd(defaultKeyStatistics.get("lastFiscalYearEnd") == null ? null : defaultKeyStatistics.get("lastFiscalYearEnd").asLong());
            stock.setNextFiscalYearEnd(defaultKeyStatistics.get("nextFiscalYearEnd") == null ? null : defaultKeyStatistics.get("nextFiscalYearEnd").asLong());
            stock.setMostRecentQuarter(defaultKeyStatistics.get("mostRecentQuarter") == null ? null : defaultKeyStatistics.get("mostRecentQuarter").asLong());
            stock.setEarningsQuarterlyGrowth(defaultKeyStatistics.get("earningsQuarterlyGrowth").get("raw") == null ? null : defaultKeyStatistics.get("earningsQuarterlyGrowth").get("raw").asDouble());
            stock.setNetIncomeToCommon(defaultKeyStatistics.get("netIncomeToCommon") == null ? null : defaultKeyStatistics.get("netIncomeToCommon").asInt());
            stock.setTrailingEPS(defaultKeyStatistics.get("trailingEps").get("raw") == null ? null : defaultKeyStatistics.get("trailingEps").get("raw").asDouble());
            stock.setForwardEPS(defaultKeyStatistics.get("forwardEps").get("raw") == null ? null : defaultKeyStatistics.get("forwardEps").get("raw").asDouble());
            stock.setPEGRatio(defaultKeyStatistics.get("pegRatio").get("raw") == null ? null : defaultKeyStatistics.get("pegRatio").get("raw").asDouble());
            stock.setLastSplitFactor(defaultKeyStatistics.get("lastSplitFactor") == null ? null : defaultKeyStatistics.get("lastSplitFactor").asText());
            stock.setLastSplitDate(defaultKeyStatistics.get("lastSplitDate") == null ? null : defaultKeyStatistics.get("lastSplitDate").asLong());
            stock.setEnterpriseToRevenue(defaultKeyStatistics.get("enterpriseToRevenue").get("raw") == null ? null : defaultKeyStatistics.get("enterpriseToRevenue").get("raw").asDouble());
            stock.setEnterpriseToEbitda(defaultKeyStatistics.get("enterpriseToEbitda").get("raw") == null ? null : defaultKeyStatistics.get("enterpriseToEbitda").get("raw").asDouble());
            stock.setChange52Weeks(defaultKeyStatistics.get("52WeekChange") == null ? null : defaultKeyStatistics.get("52WeekChange").asDouble());
            stock.setChange52WeeksSP500(defaultKeyStatistics.get("SandP52WeekChange").get("raw") == null ? null : defaultKeyStatistics.get("SandP52WeekChange").get("raw").asDouble());

            // Quote Type
            JsonNode quoteType = result.get("quoteType");
            stock.setShortName(quoteType.get("shortName") == null ? null : quoteType.get("shortName").asText());
            stock.setName(quoteType.get("longName") == null ? null : quoteType.get("longName").asText());
            stock.setTimezone(quoteType.get("timeZoneFullName") == null ? null : quoteType.get("timeZoneFullName").asText());

        } else {
            throw new IOException("Invalid response");
        }

        return stock;
    }

    public static JsonNode grabJson(URL url) {
        try {
            RedirectableRequest redirectableRequest = new RedirectableRequest(url, 5);
            redirectableRequest.setConnectTimeout(10000);
            redirectableRequest.setReadTimeout(10000);
            URLConnection connection = redirectableRequest.openConnection();

            try (InputStreamReader is = new InputStreamReader(connection.getInputStream())) {
                return objectMapper.readTree(is);
            }

        } catch (Exception e) {
            System.err.println("Error fetching JSON data from: " + url);
            e.printStackTrace();
            return null;
        }
    }
}
