import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DLeftHashTable {
    private int buckets; // Number of buckets per table
    private Map<Integer, List<Entry>> leftTable;
    private Map<Integer, List<Entry>> rightTable;

    // Entry class to store <key, value> pairs
    private static class Entry {
        String key;
        int value;

        public Entry(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    // Constructor to create DLeftHashTable with specified number of buckets
    public DLeftHashTable(int buckets) {
        this.buckets = buckets;
        leftTable = new HashMap<>(buckets);
        rightTable = new HashMap<>(buckets);
    }

    // Method to insert a <key, value> pair in the hash table
    public void insert(String key, int value) {
        // Hash the key using both hash functions
        int leftHash = hashLeft(key);
        int rightHash = hashRight(key);

        // Get the corresponding bucket in both tables
        List<Entry> leftBucket = leftTable.computeIfAbsent(leftHash, k -> new ArrayList<>());
        List<Entry> rightBucket = rightTable.computeIfAbsent(rightHash, k -> new ArrayList<>());

        // Create an Entry object for the <key, value> pair
        Entry entry = new Entry(key, value);

        // Insert the Entry object in the table with the lowest occupancy
        if (leftBucket.size() <= rightBucket.size()) {
            leftBucket.add(entry);
        } else {
            rightBucket.add(entry);
        }
    }

    // Method to lookup a key in the hash table and return the corresponding value
    // if found
    public Integer lookup(String key) {
        // Hash the key using both hash functions
        int leftHash = hashLeft(key);
        int rightHash = hashRight(key);

        // Search for the key in both tables
        List<Entry> leftBucket = leftTable.get(leftHash);
        List<Entry> rightBucket = rightTable.get(rightHash);

        if (leftBucket == null && rightBucket == null) {
            // No bucket in either table
            return null;
        }

        // Search for the key in the left table
        if (leftBucket != null) {
            for (Entry entry : leftBucket) {
                if (entry.key.equals(key)) {
                    return entry.value;
                }
            }
        }

        // Search for the key in the right table
        if (rightBucket != null) {
            for (Entry entry : rightBucket) {
                if (entry.key.equals(key)) {
                    return entry.value;
                }
            }
        }

        // Return null if key not found
        return null;
    }

    // Hash function for the left table
    private int hashLeft(String key) {
        // Simple hash function: sum of ASCII values of characters modulo table size
        int hash = 0;
        for (char c : key.toCharArray()) {
            hash += (int) c;
        }
        return hash % buckets;
    }

    // Hash function for the right table
    private int hashRight(String key) {
        // DJB2 hash function
        int hash = 5381;
        for (int i = 0; i < key.length(); i++) {
            hash = ((hash << 5) + hash) + key.charAt(i);
        }
        // Make sure the hash is positive and within the table size
        return Math.abs(hash % buckets);
    }
}
