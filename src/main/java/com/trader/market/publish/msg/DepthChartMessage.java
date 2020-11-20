package com.trader.market.publish.msg;

import com.trader.market.def.DepthLevel;
import com.trader.market.entity.MarketDepthChart;
import com.trader.market.entity.MarketDepthChartSeries;
import com.trader.market.entity.MarketDepthInfo;
import com.trader.utils.SymbolUtils;
import io.vertx.core.buffer.Buffer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author yjt
 * @since 2020/10/11 12:56
 */
public class DepthChartMessage {
    public static Message<MarketDepthChartSeries> of(MarketDepthChartSeries series) {
        final Message<MarketDepthChartSeries> msg = new Message<>();
        msg.setType(MessageType.DEPTH_CHART);
        msg.setData(series);
        msg.setTs(System.currentTimeMillis());
        return msg;
    }

    public static Buffer toBuf(MarketDepthChartSeries series) {
        // | msg size (4byte) | msg type (1byte) | ts (8byte) |
        // | symbol.size [4byte] data[bytes] | numOfStep (1byte)  |
        // repeated | depth (1byte) | numOfBid (1byte) | repeated bids (32byte) |
        // | numOfAsk (1Byte) | repeated asks (32byte)
        byte[] symbolBytes = SymbolUtils.toGenericSymbol(series.getSymbol()).getBytes();
        int msgSize = symbolBytes.length;
        byte numOfStep = (byte) series.getSeries().size();
        for (MarketDepthChart chart : series.getSeries()) {
            msgSize += 1 + chart.getAsks().size() * 32 + chart.getBids().size() * 32;
        }
        // store msg type
        msgSize += 1;
        // store msg ts
        msgSize += 8;
        // store num of step
        msgSize += 1;
        // store num of bid/ask
        msgSize += numOfStep * 8;
        // symbol size
        msgSize += 4;

        Buffer buf = Buffer.buffer(msgSize)
                // msg header
                .appendInt(msgSize)
                .appendByte((byte) MessageType.DEPTH_CHART.ordinal())
                .appendLong(System.currentTimeMillis());


        // symbol and numOfStep
        buf.appendInt(symbolBytes.length)
                .appendBytes(symbolBytes)
                .appendByte(numOfStep);
        for (MarketDepthChart chart : series.getSeries()) {
            // depth
            buf.appendByte((byte) chart.getDepth().ordinal());
            depthToBuf(buf, chart.getBids());
            depthToBuf(buf, chart.getAsks());
        }
        return buf;
    }

    public static void depthToBuf(Buffer buf, List<MarketDepthInfo> depthInfo) {
        buf.appendInt(depthInfo.size());
        for (MarketDepthInfo bid : depthInfo) {
            buf.appendDouble(bid.getPrice().doubleValue());
            buf.appendDouble(bid.getTotal().doubleValue());
            buf.appendDouble(bid.getExecuted().doubleValue());
            buf.appendDouble(bid.getLeaves().doubleValue());
        }
    }

    public static MarketDepthChartSeries of(Buffer buf, int readOffset, int msgSize) {
        // | msg size (4byte) | msg type (1byte) | ts (8byte) |
        // | symbol.size [4byte] data[bytes] | numOfStep (1byte)  |
        // repeated | depth (1byte) | numOfBid (1byte) | repeated bids (32byte) |
        // | numOfAsk (1Byte) | repeated asks (32byte)
        int offset = readOffset;
        MarketDepthChartSeries cs = new MarketDepthChartSeries();
        int symbolLength = buf.getInt(offset);
        if (symbolLength <= 0) {
            return null;
        }
        offset += 4;
        byte[] symbolBytes = buf.getBytes(offset, offset + symbolLength);
        offset += symbolLength;
        cs.setSymbol(new String(symbolBytes));
        // num of step
        byte numOfStep = buf.getByte(offset);
        if (numOfStep > DepthLevel.values().length) {
            return null;
        }
        offset += 1;
        List<MarketDepthChart> charts = new ArrayList<>(numOfStep);
        for (byte step = 0; step < numOfStep; step++) {
            MarketDepthChart chart = new MarketDepthChart();
            charts.add(chart);
            DepthLevel depth = DepthLevel.of(buf.getByte(offset));
            if (depth == null) {
                return null;
            }
            offset += 1;
            chart.setDepth(depth);
            // bids
            int numOfBids = buf.getInt(offset);
            offset += 4;
            List<MarketDepthInfo> bids = Collections.emptyList();
            if (numOfBids > 0) {
                bids = new ArrayList<>(numOfBids);
                for (int i = 0; i < numOfBids; i++) {
                    bids.add(buildDepth(buf, offset));
                    offset += 32;
                }
            }
            chart.setBids(bids);
            // asks
            int numOfAsks = buf.getInt(offset);
            offset += 4;
            List<MarketDepthInfo> asks = Collections.emptyList();
            if (numOfAsks > 0) {
                asks = new ArrayList<>(numOfAsks);
                for (int i = 0; i < numOfAsks; i++) {
                    asks.add(buildDepth(buf, offset));
                    offset += 32;
                }
            }
            chart.setAsks(asks);
        }
        cs.setSeries(charts);
        return cs;
    }

    private static MarketDepthInfo buildDepth(Buffer buf, int readOffset) {
        int offset = readOffset;
        MarketDepthInfo depth = new MarketDepthInfo();
        depth.setPrice(BigDecimal.valueOf(buf.getDouble(offset)));
        offset += 8;
        depth.setTotal(BigDecimal.valueOf(buf.getDouble(offset)));
        offset += 8;
        depth.setExecuted(BigDecimal.valueOf(buf.getDouble(offset)));
        offset += 8;
        depth.setLeaves(BigDecimal.valueOf(buf.getDouble(offset)));
        return depth;
    }
}
