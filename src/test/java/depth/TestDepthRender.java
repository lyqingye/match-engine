package depth;

import com.trader.market.def.DepthLevel;
import com.trader.market.entity.MarketDepthInfo;
import com.trader.utils.MarketDepthUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestDepthRender {
    BigDecimal[][] priceAndLeaves;
    List<MarketDepthInfo> marketDepthInfoList = new ArrayList<>();

    @Before
    public void initData () {
        priceAndLeaves = new BigDecimal[][]{
                {BigDecimal.valueOf(2.09000000), BigDecimal.valueOf(10.00000000)},
                {BigDecimal.valueOf(2.08500000), BigDecimal.valueOf(20.83200299)},
                {BigDecimal.valueOf(2.08400000), BigDecimal.valueOf(50.00000000)},
                {BigDecimal.valueOf(2.08300000), BigDecimal.valueOf(1.00000000)},
                {BigDecimal.valueOf(2.08200000), BigDecimal.valueOf(50.00000000)},
                {BigDecimal.valueOf(2.08200000), BigDecimal.valueOf(300.00000000)},
                {BigDecimal.valueOf(2.08100000), BigDecimal.valueOf(10.00000000)},
                {BigDecimal.valueOf(2.08100000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(2.08000000), BigDecimal.valueOf(1.00000000)},
                {BigDecimal.valueOf(2.07900000), BigDecimal.valueOf(5.00000000)},
                {BigDecimal.valueOf(2.07900000), BigDecimal.valueOf(100.00000000)},
                {BigDecimal.valueOf(2.07500000), BigDecimal.valueOf(200.00000000)},
                {BigDecimal.valueOf(2.07200000), BigDecimal.valueOf(302.00000000)},
                {BigDecimal.valueOf(2.07200000), BigDecimal.valueOf(205.00000000)},
                {BigDecimal.valueOf(2.07180000), BigDecimal.valueOf(34.80000000)},
                {BigDecimal.valueOf(2.07000000), BigDecimal.valueOf(35.00000000)},
                {BigDecimal.valueOf(2.07000000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(2.06900000), BigDecimal.valueOf(69.84684600)},
                {BigDecimal.valueOf(2.06800000), BigDecimal.valueOf(100.00000000)},
                {BigDecimal.valueOf(2.06500000), BigDecimal.valueOf(56.00000000)},
                {BigDecimal.valueOf(2.06000000), BigDecimal.valueOf(48.54368900)},
                {BigDecimal.valueOf(2.05800000), BigDecimal.valueOf(50.00000000)},
                {BigDecimal.valueOf(2.05000000), BigDecimal.valueOf(255.00000000)},
                {BigDecimal.valueOf(2.05000000), BigDecimal.valueOf(500.00000000)},

                {BigDecimal.valueOf(2.05000000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(2.03500000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(2.02500000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(2.02450000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(2.02000000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(2.01550000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(2.00000000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(2.00000000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(1.90000000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(1.90000000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(1.88900000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(1.86000000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(1.80000000), BigDecimal.valueOf(500.00000000)},
                {BigDecimal.valueOf(1.80000000), BigDecimal.valueOf(500.00000000)},
        };
        for (BigDecimal[] el : priceAndLeaves) {
            MarketDepthInfo info = MarketDepthInfo.empty();
            info.setPrice(el[0]);
            info.setLeaves(el[1]);
            marketDepthInfoList.add(info);
        }
    }

    @Test
    public void testRender () {
    }

    @Test
    public void testFastRender () {
        List<String> ss = new ArrayList<>();
        ss.add("2.02000000");
        ss.add("2.02500000");
        ss.add("2.03500000");
        ss.add("2.05000000");
        ss.add("2.02450000");
        ss.add("2.01550000");
        ss.add("2.05800000");
        ss.add("2.06800000");
        ss.add("2.07000000");
        ss.add("2.08200000");
        ss.add("2.05000000");
        ss.add("2.00000000");
        ss.add("1.90000000");
        ss.add("2.06000000");
        ss.add("2.06500000");
        ss.add("2.06900000");
        ss.add("2.07000000");
        ss.add("1.50000000");
        ss.add("1.80000000");
        ss.add("1.55000000");
        ss.add("1.68000000");
        ss.add("1.78000000");
        ss.add("1.88900000");
        ss.add("1.90000000");
        ss.add("2.07180000");
        ss.add("2.00000000");
        ss.add("2.08500000");
        ss.add("2.07200000");
        ss.add("2.07500000");
        ss.add("1.80000000");
        ss.add("1.86000000");
        ss.add("1.50000000");
        ss.add("1.50000000");
        ss.add("1.78000000");
        ss.add("2.05000000");
        ss.add("2.07200000");
        ss.add("2.07900000");
        ss.add("2.08100000");
        ss.add("2.08400000");
        ss.add("2.08100000");
        ss.add("2.07900000");
        ss.add("2.08200000");
        ss.add("2.09000000");
        ss.add("2.08000000");

        List<MarketDepthInfo> s = new ArrayList<>();
        for (String p : ss) {
            MarketDepthInfo info = new MarketDepthInfo();
            info.setPrice(new BigDecimal(p));
            info.setExecuted(new BigDecimal("1"));
            info.setLeaves(new BigDecimal("1"));
            info.setTotal(new BigDecimal("2"));
            s.add(info);
        }
        List<MarketDepthInfo> result = MarketDepthUtils.fastRender(s, DepthLevel.step0, 20, MarketDepthInfo::reverseCompare);
        MarketDepthInfo find = null;
        for (MarketDepthInfo info : result) {
            if (info.getPrice().compareTo(new BigDecimal("2.08000000")) == 0) {
                find = info;
            }
        }
        Assert.assertNotNull(find);
    }
}
