import java.util.BitSet;

public class BloomFilter {
    private BitSet bitset;
    private final int REFRESH_COUNT;
    private int refresh_counter;

    /**
     * Creates a BloomFilter based on a BitSet of size `bitsetsize`,
     * which refreshes the bitset every `refreshcount` insertions.
     * 
     * @param bitsetsize   Size of the bitset
     * @param refreshcount Number of insertions before the bitset is refreshed
     */
    public BloomFilter(int bitsetsize, int refreshcount) {
        this.bitset = new BitSet(bitsetsize);
        this.REFRESH_COUNT = refreshcount;
        this.refresh_counter = 0;
    }

    /**
     * Records an object into the filter.
     * 
     * @param s The object to record
     */
    public void record(String s) {
        // Refresh the bitset if necessary
        if (this.refresh_counter == this.REFRESH_COUNT) {
            this.bitset.clear();
            this.refresh_counter = 0;
        }
        this.refresh_counter++;

        // Record the object
        this.bitset.set(this.hash1(s));
        this.bitset.set(this.hash2(s));
        this.bitset.set(this.hash3(s));
    }

    /**
     * Checks if an object is in the filter.
     * 
     * @param s The object to look up
     * @return true if the object is in the filter, false otherwise
     */
    public boolean lookup(String s) {
        return this.bitset.get(this.hash1(s))
                && this.bitset.get(this.hash2(s))
                && this.bitset.get(this.hash3(s));
    }

    /**
     * Hashes a string using the DJB2 algorithm.
     * 
     * @param s The string to hash
     * @return The hash of the string
     */
    private int hash1(String s) {
        int hash = 5381;
        for (int i = 0; i < s.length(); i++) {
            hash = ((hash << 5) + hash) + s.charAt(i);
        }
        // Make sure the hash is positive and within the bitset size
        return Math.abs(hash % bitset.size());
    }

    /**
     * Hashes a string using the FNV-1a algorithm.
     * 
     * @param s The string to hash
     * @return The hash of the string
     */
    private int hash2(String s) {
        final int FNV_32_PRIME = 0x01000193;
        int hash = 0x811c9dc5;
        for (int i = 0; i < s.length(); i++) {
            hash ^= s.charAt(i);
            hash *= FNV_32_PRIME;
        }
        // Make sure the hash is positive and within the bitset size
        return Math.abs(hash % bitset.size());
    }

    /**
     * Hashes a string using the Jenkins one at a time algorithm.
     * 
     * @param s The string to hash
     * @return The hash of the string
     */
    private int hash3(String s) {
        int hash = 0;
        for (int i = 0; i < s.length(); i++) {
            hash += s.charAt(i);
            hash += (hash << 10);
            hash ^= (hash >> 6);
        }
        hash += (hash << 3);
        hash ^= (hash >> 11);
        hash += (hash << 15);
        // Make sure the hash is positive and within the bitset size
        return Math.abs(hash % bitset.size());
    }
}
