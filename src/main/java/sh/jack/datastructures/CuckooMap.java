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

import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Provides an implementation of Map that uses Cuckoo Hashing.
 * 
 * Implementation for curiosity, I doubt the implementation is robust enough
 * for production environments so caveat emptor. 
 * 
 * This implementation is in no way thread safe.
 * 
 * @url https://en.wikipedia.org/wiki/Cuckoo_hashing
 * 
 * @author Jack Tams <dev@jack.sh>
 */
public class CuckooMap<K,V> extends AbstractMap<K,V> {
    
    private static final int DEFAULT_SIZE = 10;
    private static final float DEFAULT_LOADFACTOR = 0.4f;
    
    private Entry<K,V> entryStore[][];
    
    private CuckooHash hashFunctions[] = new CuckooHash[2];
    
    private int currentSize, assignedSize;
    
    /**
     * Construct a CuckooMap of default size and load factor, currently
     * Size = 10, Load Factor = 0.4
     */
    public CuckooMap(){
        this(DEFAULT_SIZE);
    }
    
    /**
     * Construct a new CuckooMap with a given sized and loadfactor. 
     * 
     * The underlying implementation will attempt to keep within the load factor
     * implementation details tbd.
     * 
     * @param loadFactor
     * @param startSize 
     */
    public CuckooMap(int startSize){
        this.entryStore = new Entry[2][startSize];
        this.currentSize = startSize;
        this.assignedSize = 0;
             
        generateHashFunctions(this.currentSize);
    }
    
    private void generateHashFunctions(int size){
        try {
            this.hashFunctions[0] = CuckooHashFactory.getHash(size);
            this.hashFunctions[1] = CuckooHashFactory.getHash(size);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CuckooMap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public int size()
    {
        return this.currentSize;
    }
    
    public float getLoadFactor(){
        return DEFAULT_LOADFACTOR;
    }
    
    public float getCurrentLoadFactor(){
        return (this.currentSize - this.assignedSize) / this.currentSize;
    }
    
    public int getAssigned()
    {
        return this.assignedSize;
    }
    
    public V put(K key, V value)
    {
        //check load factor
        /*System.out.println("Load: " + getCurrentLoadFactor());
        if ( getCurrentLoadFactor() <= DEFAULT_LOADFACTOR )
            growMap();*/
        
        // If key is already in the map, update its value, 
        // and return its value.
        for( int i = 0; i < this.hashFunctions.length; i++)
        {
            int hash = this.hashFunctions[i].getHash(key);
            Entry<K,V> currentEntry = this.entryStore[i][hash];
            
            if ( key.equals(currentEntry) ){
                return this.entryStore[i][hash].setValue(value);
            } 
        }
        
        //haven't been able to complete a simple insert, on to more complex
        //stuff.
        Entry<K,V> entryForInsert = new SimpleEntry(key, value);
        
        while( entryForInsert != null ){
            entryForInsert = insertEntry(entryForInsert);
            
            if(entryForInsert != null )
                this.rehashMap();
        }
        
        
        //We should have inserted at this point so no entry was already
        //associated with key.
        this.assignedSize++;
        return null;
    }
    
    private Entry<K,V> insertEntry(Entry<K,V> entryToInsert)
    {   
        
        Entry<K,V> displacedEntry = null;
        for ( int i = 0; i <= this.assignedSize; i++ )
        {
            int hash = this.hashFunctions[i % 2].getHash(entryToInsert.getKey());
            
            if ( this.entryStore[i%2][hash] == null )
            {
                this.entryStore[i%2][hash] = entryToInsert;
                return null;
            }
            
            displacedEntry = this.entryStore[i%2][hash];
            //displace current value, and return old one.
            this.entryStore[i%2][hash].setValue(entryToInsert.getValue());
        }
        return displacedEntry;
    }
    
    private void growMap(){
        Set<Entry<K, V>> entrySet = this.entrySet();
        this.currentSize = currentSize * 2;
        this.entryStore = new Entry[2][currentSize * 2];
        
        rehashMap(entrySet);    
    }
        
    private void rehashMap(){
        this.rehashMap(this.entrySet());
    }
    /**
     * Re-integrates the entry Set provided into the current
     * map, this should not be used as a intializer, i.e. passing
     * a entry set bigger than current Map, resize aint cheap.
     * 
     * @param entries 
     */
    private void rehashMap(Set<Entry<K,V>> entries)
    {
        System.out.println("Rehashing!");
        //Regenrate hash based on new size
        generateHashFunctions(this.currentSize);
                
        for( Entry<K,V> entry : entries ){
            //TODO: More implementation here, what if it conflicts and 
            //evicts a member
            this.put(entry.getKey(), entry.getValue());
        }
        
    }
    
    /**
     * Builds a flattened entry set with all current entries of the
     * CuckooMap.
     * @return Set containing all current elements of CuckooMap
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K,V>> returnSet = new LinkedHashSet();
        
        for ( int i = 0; i < 2; i++ )
        {
            for ( Entry<K,V> entry : this.entryStore[i] )
            {
                if ( entry != null )
                    returnSet.add(entry);
            }
        }
        
        return returnSet;
    }
    
    @Override
    public String toString(){
        StringBuilder bld = new StringBuilder();
        
        for(int i = 0; i < size(); i++){
            bld.append("[ ").append(this.entryStore[0][i]).append(" ]").append("\t");
            bld.append("[ ").append(this.entryStore[1][i]).append(" ]").append("\n");
        }
        
        return bld.toString();
    }
}
