package com.trader.utils;

import com.trader.market.def.DepthLevel;
import com.trader.market.entity.MarketDepthInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yjt
 * @since 2020/9/22 下午1:41
 */
public class MarketDepthUtils {
    /**
     * 处理深度 (使用流)
     *
     * @param unProcessDataList 需要处理的数据
     * @param depth             深度
     * @param limit             总条数
     * @param comparator        比较器
     * @return 深度数据
     */
    public static List<MarketDepthInfo> fastRender(List<MarketDepthInfo> unProcessDataList,
                                                   DepthLevel depth,
                                                   int limit,
                                                   Comparator<MarketDepthInfo> comparator) {
        return unProcessDataList.stream()
                .collect(Collectors.groupingBy(d -> calcTrx(d.getPrice(), depth)))
                .entrySet()
                .stream()
                .flatMap((entry) -> Stream.of(combineTrx(entry.getKey(), entry.getValue())))

                // 过滤剩余量 > 0
                .filter(e -> e.getLeaves().setScale(6, RoundingMode.DOWN).compareTo(BigDecimal.ZERO) > 0 &&
                        // 过滤价格 > 0
                        e.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .sorted(comparator)
                .limit(limit)
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
     * @param bigDecimal decimal
     * @return 小数位数
     */
     private static int getNumberOfDecimalPlaces(BigDecimal bigDecimal) {
        String string = bigDecimal.stripTrailingZeros().toPlainString();
        int index = string.indexOf(".");
        return index < 0 ? 0 : string.length() - index - 1;
    }
}
