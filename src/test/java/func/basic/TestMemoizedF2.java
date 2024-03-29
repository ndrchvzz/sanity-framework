package func.basic;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TestMemoizedF2 {
    private final F2<Integer,Integer,Integer> prodPlus1;
    private final F2<Integer,Integer,Integer> memoizedProdPlus1;

    public TestMemoizedF2() {
        prodPlus1 = new F2<Integer,Integer,Integer>() {
            public Integer execute(Integer i1, Integer i2) {
                return (i1 == null ? 0 : i1) * (i2 == null ? 0 : i2) + 1;
            }
        };
        memoizedProdPlus1 = new MemoizedF2<>(prodPlus1);
    }

    @Test
    public void test() {
        assertEquals(memoizedProdPlus1.execute(3, 4), prodPlus1.execute(3, 4));
        assertEquals(memoizedProdPlus1.execute(-3, 0), prodPlus1.execute(-3, 0));
        assertEquals(memoizedProdPlus1.execute(5, 6), prodPlus1.execute(5, 6));
        assertEquals(memoizedProdPlus1.execute(null, 6), prodPlus1.execute(null, 6));
        assertEquals(memoizedProdPlus1.execute(6, null), prodPlus1.execute(6, null));
    }
}
