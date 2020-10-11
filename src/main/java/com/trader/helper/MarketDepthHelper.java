package com.trader.helper;

import com.trader.market.def.DepthLevel;
import com.trader.market.entity.MarketDepthChart;
import com.trader.market.entity.MarketDepthInfo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yjt
 * @since 2020/9/22 下午1:41
 */
public class MarketDepthHelper {

    /**
     * 深度图
     *
     * @param chart
     *         买卖盘
     *
     * @return 深度数据
     */
    public static MarketDepthChart render(MarketDepthChart chart, DepthLevel depth, int limit) {
        // 卖盘价格低到高
        MarketDepthChart result = new MarketDepthChart();
        result.setAsks(render(chart.getAsks(), depth, limit, BigDecimal::compareTo));
        result.setBids(render(chart.getBids(), depth, limit, Comparator.reverseOrder()));
        return result;
    }

    /**
     * 处理深度
     *
     * @param unProcessDataList
     *         需要处理的数据
     * @param depth
     *         深度
     * @param limit
     *         总条数
     * @param comparator
     *         比较器
     *
     * @return 深度数据
     */
    public static Collection<MarketDepthInfo> render(Collection<MarketDepthInfo> unProcessDataList,
                                                     DepthLevel depth,
                                                     int limit,
                                                     Comparator<BigDecimal> comparator) {
        TreeMap<BigDecimal, MarketDepthInfo> trxMap = new TreeMap<>(comparator);
        for (MarketDepthInfo unProcess : unProcessDataList) {
            BigDecimal trx = calcTrx(unProcess.getPrice(), depth);
            MarketDepthInfo info = trxMap.get(trx);
            if (info == null) {
                MarketDepthInfo newAsk = unProcess.clone();
                newAsk.setPrice(trx);
                trxMap.put(trx, newAsk);
            } else {
                info.setPrice(trx);
                info.setTotal(info.getTotal().add(unProcess.getTotal()));
                info.setLeaves(info.getLeaves().add(unProcess.getLeaves()));
                info.setExecuted(info.getExecuted().add(unProcess.getExecuted()));
            }
        }
        return trxMap.values()
                     .stream()
                     .limit(limit)
                     .collect(Collectors.toList());
    }

    /**
     * 处理深度 (使用流)
     *
     * @param unProcessDataList
     *         需要处理的数据
     * @param depth
     *         深度
     * @param limit
     *         总条数
     * @param comparator
     *         比较器
     *
     * @return 深度数据
     */
    public static Collection<MarketDepthInfo> fastRender(Collection<MarketDepthInfo> unProcessDataList,
                                                         DepthLevel depth,
                                                         int limit,
                                                         Comparator<MarketDepthInfo> comparator) {
        return unProcessDataList.parallelStream()
                                .collect(Collectors.groupingBy(d -> calcTrx(d.getPrice(), depth)))
                                .entrySet()
                                .parallelStream()
                                .flatMap((entry) -> Stream.of(combineTrx(entry.getKey(), entry.getValue())))
                                .limit(limit)
                                .sorted(comparator)
                                .collect(Collectors.toList());
    }

    private static MarketDepthInfo combineTrx(BigDecimal trx, List<MarketDepthInfo> ls) {
        MarketDepthInfo result = MarketDepthInfo.empty();
        ls.forEach(result::add);
        result.setPrice(trx);
        return result;
    }

    private static BigDecimal calcTrx(BigDecimal price, DepthLevel depth) {
        return price.setScale(getNumberOfDecimalPlaces(price) - depth.ordinal(), BigDecimal.ROUND_DOWN);
    }

    /**
     * 获取小数位数
     *
     * @param bigDecimal
     *         decimal
     *
     * @return 小数位数
     */
    private static int getNumberOfDecimalPlaces(BigDecimal bigDecimal) {
        String string = bigDecimal.stripTrailingZeros().toPlainString();
        int index = string.indexOf(".");
        return index < 0 ? 0 : string.length() - index - 1;
    }
}
