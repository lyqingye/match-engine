package comparator;

import com.trader.comprator.BidComparator;
import com.trader.def.OrderType;
import com.trader.entity.Order;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author yjt
 * @since 2020/9/2 上午8:58
 */
//@RunWith(Parameterized.class)
public class TestBidComparator {
//    private String nameOfO1;
//    private String nameOfO2;
//    private BigDecimal priceOfO1;
//    private BigDecimal priceOfO2;
//    private OrderType typeOfO1;
//    private OrderType typeOfO2;
//    private Date timeOfO1;
//    private Date timeOfO2;
//    private String exceptedFirstObjName;
//    private String exceptedSecondObjName;
//
//    public TestBidComparator(String nameOfO1, String nameOfO2,
//                             OrderType typeOfO1, OrderType typeOfO2,
//                             BigDecimal priceOfO1, BigDecimal priceOfO2,
//
//                             Date timeOfO1, Date timeOfO2,
//                             String exceptedFirstObjName, String exceptedSecondObjName) {
//        this.nameOfO1 = nameOfO1;
//        this.nameOfO2 = nameOfO2;
//        this.priceOfO1 = priceOfO1;
//        this.priceOfO2 = priceOfO2;
//        this.typeOfO1 = typeOfO1;
//        this.typeOfO2 = typeOfO2;
//        this.timeOfO1 = timeOfO1;
//        this.timeOfO2 = timeOfO2;
//        this.exceptedFirstObjName = exceptedFirstObjName;
//        this.exceptedSecondObjName = exceptedSecondObjName;
//    }
//
//    @Parameterized.Parameters
//    public static Collection prepareData() throws InterruptedException {
//        ArrayList<Object[]> rs = new ArrayList<>();
//
//        Date date1 = new Date();
//        Thread.sleep(100);
//        Date date2 = new Date();
//
//        // 控制变量法测试
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.MARKET,
//                BigDecimal.valueOf(10),BigDecimal.valueOf(1),
//                date1,date2,
//                "o1","o2"});
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.MARKET,
//                BigDecimal.valueOf(10),BigDecimal.valueOf(1),
//                date1,date1,
//                "o1","o2"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.MARKET,
//                BigDecimal.valueOf(10),BigDecimal.valueOf(1),
//                date2,date1,
//                "o1","o2"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.MARKET,
//                BigDecimal.valueOf(1),BigDecimal.valueOf(1),
//                date1,date2,
//                "o1","o2"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.MARKET,
//                BigDecimal.valueOf(1),BigDecimal.valueOf(1),
//                date1,date1,
//                "o1","o2"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.MARKET,
//                BigDecimal.valueOf(1),BigDecimal.valueOf(1),
//                date2,date1,
//                "o2","o1"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.MARKET,
//                BigDecimal.valueOf(1),BigDecimal.valueOf(10),
//                date1,date2,
//                "o2","o1"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.MARKET,
//                BigDecimal.valueOf(1),BigDecimal.valueOf(10),
//                date1,date1,
//                "o2","o1"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.MARKET,
//                BigDecimal.valueOf(1),BigDecimal.valueOf(10),
//                date2,date1,
//                "o2","o1"});
//
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.LIMIT,
//                BigDecimal.valueOf(1),BigDecimal.valueOf(10),
//                date2,date1,
//                "o1","o2"});
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.MARKET,OrderType.STOP,
//                BigDecimal.valueOf(1),BigDecimal.valueOf(10),
//                date2,date1,
//                "o1","o2"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.STOP,OrderType.MARKET,
//                BigDecimal.valueOf(1),BigDecimal.valueOf(10),
//                date2,date1,
//                "o2","o1"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.LIMIT,OrderType.MARKET,
//                BigDecimal.valueOf(10),BigDecimal.valueOf(10),
//                date2,date1,
//                "o2","o1"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.LIMIT,OrderType.STOP,
//                BigDecimal.valueOf(10),BigDecimal.valueOf(10),
//                date2,date1,
//                "o1","o2"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.LIMIT,OrderType.LIMIT,
//                BigDecimal.valueOf(10),BigDecimal.valueOf(10),
//                date2,date1,
//                "o2","o1"});
//
//        rs.add(new Object[]{
//                "o1","o2",
//                OrderType.STOP,OrderType.STOP,
//                BigDecimal.valueOf(10),BigDecimal.valueOf(10),
//                date2,date1,
//                "o2","o1"});
//
//        return rs;
//    }
//
//    @Test
//    public void testComp() throws InterruptedException {
//        Order o1 = new Order();
//        Order o2 = new Order();
//
//        o2.setId(nameOfO1);
//        o2.setPrice(priceOfO1);
//        o2.setCreateDateTime(timeOfO1);
//        o2.setType(typeOfO1);
//
//        o1.setId(nameOfO2);
//        o1.setPrice(priceOfO2);
//        o1.setCreateDateTime(timeOfO2);
//        o1.setType(typeOfO2);
//
//        TreeSet<Order> set = new TreeSet<>(new BidComparator());
//        set.add(o2);
//        set.add(o1);
//
//        Assert.assertTrue(set.first().getId().equals(exceptedFirstObjName));
//        Assert.assertTrue(set.last().getId().equals(exceptedSecondObjName));
//    }


}
