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
import java.util.Arrays;
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
public class CuckooMap<K,V> extends AbstractMap<K,V>
{
    
    private static final int DEFAULT_SIZE = 10;
    private static final double DEFAULT_LOADFACTOR = 0.4;
    
    private Entry<K,V> entryStore[][];
    
    private CuckooHash hashFunctions[] = new CuckooHash[2];
    
    private int currentSize, assignedSize;
    
    /**
     * Construct a CuckooMap of default size, currently 10.
     */
    public CuckooMap()
    {
        this(DEFAULT_SIZE);
    }
    
    /**
     * Construct a new CuckooMap with a given initial size. 
     * 
     * Generates two hash functions and a initial sized map.
     * 
     * @param initialSize initial size of map, note only 40% of this capacity is usable.
     */
    @SuppressWarnings("unchecked")
    public CuckooMap(int initialSize)
    {
        
        this.entryStore = (Entry<K,V>[][]) new Entry[2][initialSize];
        this.currentSize = initialSize;
        this.assignedSize = 0;
             
        generateHashFunctions(this.currentSize);
    }
    
    /**
     * Generates 2 new hash functions based on the provided size.
     * 
     * @param size size of array/bucket being hashed into.
     */
    private void generateHashFunctions(int size)
    {
        try {
            this.hashFunctions[0] = CuckooHashFactory.getHash(size);
            this.hashFunctions[1] = CuckooHashFactory.getHash(size);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(CuckooMap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Gives the current maximum size of the map, this does not take into consideration
     * the 40% load factor, so the usable capacity is 40% of this value.
     * 
     * @return maximum underlying size of the map.
     */
    public int maxSize()
    {
        return this.currentSize;
    }
    
    /**
     * Calculates the current load factor for the map, to remain functional
     * the CuckooMap should never exceed a 40% (0.4) utilisation.
     * 
     * @return percentage utilisation of map in decimal notation.
     */
    public double getCurrentLoad()
    {
        //Dataset is twice as large as currentSize as there are two arrays
        //in the entry store.
        return ((double)this.assignedSize) / (this.currentSize*2); 
    }
    
    /**
     * Returns the current number of items in the map.
     * @return current number of items in the map.
     */
    @Override
    public int size()
    {
        return this.assignedSize;
    }

    /**
     * Inserts a key value pair into the map, if the key already exists in the map,
     * the associated value will be updated and the old value returned.
     * 
     * If the pair is inserted cleanly returns null.
     * 
     * Both the Key and Value objects passed in, should implement a good hashCode
     * and equals method.
     * 
     * @param key key associated with entry.
     * @param value value associated with key.
     * @return old value for key, otherwise null.
     */
    public V put(K key, V value)
    {
        // Check for existing entry with same key, if found replace its value
        // and return the old value.
        for(int i = 0; i < 2; i++)
        {
            int hash = this.hashFunctions[i].getHash(key);
            Entry<K,V> entry = this.entryStore[i][hash];
            if( entry != null && key.equals(entry.getKey())){
                V displacedValue = entry.getValue();
                this.entryStore[i][hash].setValue(value);
                return displacedValue;
            }
        }

        //check load factor
        if ( this.getCurrentLoad() >= DEFAULT_LOADFACTOR )
        {
            grow();
        }
        //Otherwise its a new value so needs to be inserted.
        Entry<K,V> entryToInsert = new SimpleEntry<>(key,value);
        
        for(;;)
        {
            entryToInsert = insert(entryToInsert);
            
            if( entryToInsert == null )
                break;
            
            //Otherwise, we have something displaced - rehash
            //and attempt to reinsert.
            rehash();
            
        }
        
        //Increment counter
        this.assignedSize++;
                
        return null;
    }
    
    /**
     * Doubles the size underlying object store, and rehashes all the 
     * objects with new hashes.
     * 
     * This is reasonably costly, try to size the map appropriately initially.
     */
    @SuppressWarnings("unchecked")
    private void grow()
    {
        Entry<K,V> old[][] = this.entryStore;
        
        this.entryStore = (Entry<K,V>[][]) new Entry[2][this.currentSize*2];
        this.currentSize = this.entryStore[0].length;
        
        int insertPtr = 0;
        for(int i = 0; i < 2; i++)
        {
            for ( Entry<K,V> entry : old[i])
            {
                if( entry != null )
                    this.entryStore[0][insertPtr++] = entry;
            }
        }
        
        rehash();
    }
    
    /**
     * Rebuilds the Map with new hash functions, will repeatedly rehash if there
     * are any clashes of entries.
     */
    protected void rehash()
    {
        Set<Entry<K,V>> old = this.entrySet();
        
        // Keep rehashing until all entries go back cleanly.
        for(;;)
        {
            this.generateHashFunctions(currentSize);
            //clear array
            Arrays.fill(this.entryStore[0], null);
            Arrays.fill(this.entryStore[1], null);
            
            //repeat until Entries insert cleanly, theoretically should be rare
            //if hash function is sufficiently good.
            if ( rehashHelper(old) == null )
                break;
        }
        
    }
    
    /**
     * Takes array of old entries, and attempts to insert them all
     * into the current CuckooMap, it will return the first displaced entry
     * if unable to cleanly insert otherwise returns null.
     * 
     * @param oldEntries
     * @return first displaced entry or null
     */
    private Entry<K,V> rehashHelper(Set<Entry<K,V>> oldEntries)
    {                      
        Entry<K,V> currentEntry = null;
        for( Entry<K,V> entry : oldEntries )
        {
            currentEntry = this.insert(entry);
            if( currentEntry != null )
                return currentEntry;
        }       
        
        return currentEntry;
    }
    
    /**
     * Inserts entry into map, if insertion causes an existing entry to be
     * evicted, this evicted entry is returned. 
     * 
     * It is assumed that the caller, as already checked for trivial insertions.
     * i.e. entry updates an existing key. 
     * 
     * @param entry new entry to insert.
     * @return evicted entry otherwise null if cleanly inserted.
     */
    private Entry<K,V> insert(Entry<K,V> entry)
    {
        Entry<K,V> displaced = null;
        for(int i = 0; i < this.size() + 1; i++)
        {
            int hash = this.hashFunctions[ i % 2 ].getHash(entry.getKey());
            displaced = this.entryStore[ i % 2 ][hash];
            
            if( displaced == null )
            {
                //clean insert, add and return null.
                this.entryStore[ i % 2 ][hash] = entry;
                return null;
            }
            
            this.entryStore[ i % 2 ][hash] = entry;
            return displaced;            
        }
        return null;
    }
    
    
    /**
     * Flattens map into Set containing all current entries.
     * 
     * @return all current entries.
     */
    @Override
    public Set<Entry<K, V>> entrySet()
    {
        Set<Entry<K,V>> builtSet = new LinkedHashSet<>();
        
        for ( int i = 0; i < this.currentSize; i++ ){
            for( int j = 0; j < 2; j++ ){
                if(this.entryStore[j][i] != null )
                    builtSet.add(this.entryStore[j][i]);
            }
        }
        
        return builtSet;
    }
    
    /**
     * Gives a rudimentary text representation of map.
     * 
     * @return text representation of current map.
     */
    public String toString()
    {
    
        StringBuilder stringBuilder = new StringBuilder();
        
        for( int i = 0; i < this.currentSize; i++ )
        {
            stringBuilder.append(this.entryStore[0][i]);
            stringBuilder.append("\t");
            stringBuilder.append(this.entryStore[1][i]);
            stringBuilder.append("\n");
        }
        
        return stringBuilder.toString();
    }
    
}
