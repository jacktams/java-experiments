/*
 * The MIT License
 *
 * Copyright 2016 Jack Tams.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package sh.jack.utils;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


/**
 *
 * @author Jack Tams <dev@jack.sh>
 */
public class MathTest
{
    @DataProvider
    public static Object[][] Log2Input() 
    {
        return new Object[][] 
        {
            new Object[] { 42 },
            new Object[] { 11 },
            new Object[] { 131 },
            new Object[] { 254 },
            new Object[] { Integer.MAX_VALUE }
        };
    }

    
    @Test(dataProvider = "Log2Input")
    public void log2Test( final int val )
    {
        int test = (int) (java.lang.Math.log(val) / java.lang.Math.log(2));
        assertEquals(sh.jack.utils.Math.log2(val), test);       
    }
    
    @Test
    public void log2ZeroTest()
    {
        assertEquals(sh.jack.utils.Math.log2(0), 0);
    }
}
