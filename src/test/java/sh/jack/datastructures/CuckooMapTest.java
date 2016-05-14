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
package sh.jack.datastructures;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;


/**
 *
 * @author Jack Tams <dev@jack.sh>
 */
public class CuckooMapTest {
       
    /**
     * Checks in-place rehash of map does not cause elements to be lost.
     * Does not use an input that would trigger a grow.
     */
    @Test
    public void inplaceRehashTest(){
        Set<Entry<String,String>> testSet = new HashSet<>();
        
        CuckooMap<String, String> map = new CuckooMap<>();
        
        for(int i =0; i < 5; i++)
        {
            map.put("key"+i, "value"+i);
            testSet.add(new SimpleEntry<>("key"+i, "value"+i));
        }
        
        for( int i = 0; i < 10; i++){
            map.rehash();
            assertEquals(map.entrySet(), testSet);
        }
    }
    
    /**
     * Adds sufficient elements to cause at least one rehash, checks
     * no elements have been lost.
     */
    @Test
    public void growTest(){
       Set<Entry<String,String>> testSet = new HashSet<>();
       CuckooMap<String, String> map = new CuckooMap<>();
        
       for( int i = 0; i < 50; i++ )
       {
           testSet.add(new SimpleEntry<>("key"+i, "value"+i));
           map.put("key"+i, "value"+i);
       }
       
       assertEquals(map.entrySet(), testSet);
    }
    
}
