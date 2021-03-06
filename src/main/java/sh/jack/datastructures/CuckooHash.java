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

/**
 * Provides a simply hash generation function for objects.
 * 
 * @author Jack Tams <dev@jack.sh>
 */
public class CuckooHash<T> {
  
    private int hashsize;
    private int coefficientHigh, coeffecientLow;
    
    public CuckooHash(int high, int low, int hashSize)
    {
        this.hashsize = sh.jack.utils.Math.log2(hashSize);
        this.coefficientHigh = high;
        this.coeffecientLow = low;
    }
    
    /**
     * Returns a integer hash based on default hashcode of object. 
     * 
     * Assumes that the implementor of the object has done a decent job
     * at implementing hash.
     * 
     * @param t object to be hashed.
     * @return new hash based on hashCode of t, 
     *         and size of container being hashed into.
     */
    public int getHash(Object t)
    {
        if ( t == null )
            return 0;
        
        int objectHash = t.hashCode();
        int upper = ( objectHash >>> 16 ) * this.coefficientHigh;
        int lower = ( objectHash & 0xFFFF ) * this.coeffecientLow;
                
        return (upper + lower) >>> ( 32 - hashsize );
    }
}
