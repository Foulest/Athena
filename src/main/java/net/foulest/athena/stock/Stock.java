package net.foulest.athena.stock;

import lombok.Getter;
import lombok.Setter;
import net.foulest.athena.histquotes.HistoricalQuote;
import net.foulest.athena.histquotes.HistoricalQuotesRequest;
import net.foulest.athena.histquotes.QueryInterval;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

@Getter
@Setter
public class Stock {

    private final String symbol;

    private String currency;
    private String currencySymbol;
    private String marketState;

    private Double averageMargins;
    private Double cashToDebtRatio;
    private Double open;
    private Double previousClose;
    private Double dayLow;
    private Double dayHigh;
    private Double beta;
    private Double trailingPE;
    private Double forwardPE;
    private Double volume;
    private Double averageVolume;
    private Double averageVolume10Days;
    private Double averageDailyVolume10Days;
    private Double bid;
    private Double ask;
    private Double bidSize;
    private Double askSize;
    private Double marketCap;
    private Double fiftyTwoWeekLow;
    private Double fiftyTwoWeekHigh;
    private Double priceToSalesTrailing12Months;
    private Double fiftyDayAverage;
    private Double twoHundredDayAverage;

    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phoneNumber;
    private String website;
    private String industry;
    private String sector;
    private String businessSummary;
    private Integer fullTimeEmployees;
    private Integer auditRisk;
    private Integer boardRisk;
    private Integer compensationRisk;
    private Integer shareHolderRightsRisk;
    private Integer overallRisk;

    private Double close;
    private Double targetHighPrice;
    private Double targetLowPrice;
    private Double targetMeanPrice;
    private Double targetMedianPrice;
    private Double analystScore;
    private String analystRating;
    private Integer analystCount;
    private Double totalCash;
    private Double totalCashPerShare;
    private Double ebitda;
    private Double totalDebt;
    private Double quickRatio;
    private Double currentRatio;
    private Double totalRevenue;
    private Double debtToEquity;
    private Double revenuePerShare;
    private Double returnOnAssets;
    private Double returnOnEquity;
    private Double grossProfits;
    private Double freeCashflow;
    private Double operatingCashflow;
    private Double earningsGrowth;
    private Double revenueGrowth;
    private Double grossMargins;
    private Double ebitdaMargins;
    private Double operatingMargins;
    private Double profitMargins;

    private Double enterpriseValue;
    private Double floatShares;
    private Double sharesOutstanding;
    private Double sharesShort;
    private Double sharesShortPriorMonth;
    private Long sharesShortPreviousMonthDate;
    private Long dateShortInterest;
    private Double sharesPercentSharesOut;
    private Double heldPercentInsiders;
    private Double heldPercentInstitutions;
    private Double shortRatio;
    private Integer impliedSharesOutstanding;
    private Double bookValue;
    private Double priceToBook;
    private Long lastFiscalYearEnd;
    private Long nextFiscalYearEnd;
    private Long mostRecentQuarter;
    private Double earningsQuarterlyGrowth;
    private Integer netIncomeToCommon;
    private Double trailingEPS;
    private Double forwardEPS;
    private Double pegRatio;
    private String lastSplitFactor;
    private Long lastSplitDate;
    private Double enterpriseToRevenue;
    private Double enterpriseToEbitda;
    private Double change52Weeks;
    private Double change52WeeksSP500;

    private String shortName;
    private String name;
    private String timezone;

    private List<HistoricalQuote> history;

    public Stock(String symbol) {
        this.symbol = symbol.toUpperCase();
    }

    public List<HistoricalQuote> getHistory() throws IOException {
        if (history != null) {
            return history;
        }

        return getHistory(HistoricalQuotesRequest.DEFAULT_FROM);
    }

    public List<HistoricalQuote> getHistory(QueryInterval interval) throws IOException {
        return getHistory(HistoricalQuotesRequest.DEFAULT_FROM, interval);
    }

    public List<HistoricalQuote> getHistory(Calendar from) throws IOException {
        return getHistory(from, HistoricalQuotesRequest.DEFAULT_TO);
    }

    public List<HistoricalQuote> getHistory(Calendar from, QueryInterval interval) throws IOException {
        return getHistory(from, HistoricalQuotesRequest.DEFAULT_TO, interval);
    }

    public List<HistoricalQuote> getHistory(Calendar from, Calendar to) throws IOException {
        return getHistory(from, to, QueryInterval.MONTHLY);
    }

    public List<HistoricalQuote> getHistory(Calendar from, Calendar to, QueryInterval interval) throws IOException {
        HistoricalQuotesRequest hist = new HistoricalQuotesRequest(symbol, from, to, interval);
        setHistory(hist.getResult());
        return history;
    }
}
