package com.jasonhong.yoyu;

import com.jasonhong.yoyu.data.model.remote.GetInquireDetailResponse.RawTransactionDto;
import com.jasonhong.yoyu.domain.model.CategoryUIModel;
import com.jasonhong.yoyu.domain.model.RetailTransaction;
import com.jasonhong.yoyu.domain.model.TransactionAnalysis;
import com.jasonhong.yoyu.domain.model.TransitTransaction;
import com.jasonhong.yoyu.domain.model.YoyuTransaction;
import com.jasonhong.yoyu.domain.service.ExpenseCalculator;
import com.jasonhong.yoyu.domain.service.TransactionAggregator;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionDomainTest {

    private RawTransactionDto createRaw(String traceNo, long date, String xtype, String partner, double val, String loc, String ds, String amt) throws Exception {
        RawTransactionDto dto = new RawTransactionDto();
        setField(dto, "traceNo", traceNo);
        setField(dto, "transactionDate", date);
        setField(dto, "xtype", xtype);
        setField(dto, "partnerName", partner);
        setField(dto, "electronicValue", val);
        setField(dto, "locationName", loc);
        setField(dto, "dataSource", ds);
        setField(dto, "amt", amt);
        return dto;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    public void testTransitPairing() throws Exception {
        List<RawTransactionDto> rawList = new ArrayList<>();
        // In-station at t=100
        rawList.add(createRaw("001", 100, "進站", "臺北捷運", 100.0, "台北車站", "F", "0"));
        // Out-station at t=200
        rawList.add(createRaw("002", 200, "出站", "臺北捷運", 80.0, "大橋頭", "F", "-20"));

        TransactionAggregator aggregator = new TransactionAggregator();
        List<YoyuTransaction> result = aggregator.aggregate(rawList);

        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof TransitTransaction);
        TransitTransaction t = (TransitTransaction) result.get(0);
        assertEquals("-20", String.valueOf(t.getAmount()));
        assertEquals("台北車站", t.getInLocation());
        assertEquals("大橋頭", t.getOutLocation());
        assertEquals(80.0, t.getBalance(), 0.001);
    }

    @Test
    public void testRetailParsing() throws Exception {
        List<RawTransactionDto> rawList = new ArrayList<>();
        rawList.add(createRaw("003", 300, "小額消費", "7-ELEVEN", 60.0, "門市A", "4", "-20"));

        TransactionAggregator aggregator = new TransactionAggregator();
        List<YoyuTransaction> result = aggregator.aggregate(rawList);

        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof RetailTransaction);
        RetailTransaction r = (RetailTransaction) result.get(0);
        assertEquals(-20, r.getAmount());
        assertEquals("門市A", r.getLocation());
    }

    @Test
    public void testExpenseCalculator() {
        List<TransactionAnalysis> analysis = new ArrayList<>();
        analysis.add(new TransactionAnalysis("捷運", 0, 0, 5, 100, 0, 0));
        analysis.add(new TransactionAnalysis("公車", 0, 0, 2, 30, 0, 0));

        ExpenseCalculator calc = new ExpenseCalculator();
        int total = calc.calculateTotal(analysis);
        assertEquals(130, total);

        List<CategoryUIModel> categories = calc.extractCategories(analysis);
        assertEquals(2, categories.size());
        assertEquals("捷運", categories.get(0).getScopeName());
        assertEquals(100, categories.get(0).getAmount());
    }
}
