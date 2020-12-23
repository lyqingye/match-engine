package com.trader.market.publish.config.dto;

import io.vertx.core.json.JsonObject;
import lombok.Data;

import java.util.List;

@Data
public class CollectorStatusDto {

    public CollectorStatusDto() {
    }

    public CollectorStatusDto(JsonObject object) {
        CollectorStatusDto t = object.mapTo(CollectorStatusDto.class);
        this.name = t.name;
        this.desc = t.desc;
        this.isDeployed = t.isDeployed;
        this.isRunning = t.isRunning;
        this.subscribedSymbols = t.subscribedSymbols;
    }

    /**
     * 收集器名称
     */
    private String name;

    /**
     * 收集器描述
     */
    private String desc;

    /**
     * 是否正在运行
     */
    private boolean isRunning;

    /**
     * 是否已经部署
     */
    private boolean isDeployed;

    /**
     * 已经订阅的交易对
     */
    private List<String> subscribedSymbols;

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
