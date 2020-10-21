package com.trader.helper;

import com.trader.market.def.DepthLevel;
import com.trader.market.entity.MarketDepthChart;
import com.trader.market.entity.MarketDepthInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yjt
 * @since 2020/9/22 下午1:41
 */
public class MarketDepthHelper {
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
        return unProcessDataList.stream()
                                .collect(Collectors.groupingBy(d -> calcTrx(d.getPrice(), depth)))
                                .entrySet()
                                .stream()
                                .flatMap((entry) -> Stream.of(combineTrx(entry.getKey(), entry.getValue())))
                                .filter(e -> {
                                    return e.getLeaves()
                                            .setScale(6, RoundingMode.DOWN)
                                            .compareTo(BigDecimal.ZERO) > 0;
                                })
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
