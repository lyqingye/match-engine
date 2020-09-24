package com.trader.support;

import com.trader.entity.Product;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author yjt
 * @since 2020/9/20 上午10:58
 */
public class ProductManager {

    /**
     * 商品映射MAP
     */
    private Map<String, Product> productMap = new HashMap<>(16);

    /**
     * 添加一个商品
     *
     * @param product
     *         商品对象
     */
    public void addProduct(Product product) {
        Objects.requireNonNull(product);
        productMap.put(product.getId(), product);
    }

    /**
     * 根据商品ID获取商品对象
     *
     * @param productId
     *         商品ID
     *
     * @return 商品对象
     */
    public Product getProduct(String productId) {
        return this.productMap.get(productId);
    }
}
