import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DLeftHashTable {
    private int buckets; // Number of buckets per table
    private Map<Integer, List<Entry>> leftTable; // Left table
    private Map<Integer, List<Entry>> rightTable; // Right table

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
        leftTable = new HashMap<>();
        rightTable = new HashMap<>();
    }

    // Method to insert a <key, value> pair in the hash table
    public void insert(String key, int value) {
        // Hash the key using both hash functions
        int leftHash = hash(key, leftTable.size());
        int rightHash = hash(key, rightTable.size());

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

    // Method to lookup a key in the hash table and return the corresponding value if found
    public Integer lookup(String key) {
        // Hash the key using both hash functions
        int leftHash = hash(key, leftTable.size());
        int rightHash = hash(key, rightTable.size());

        // Search for the key in both tables
        List<Entry> leftBucket = leftTable.get(leftHash);
        if (leftBucket != null) {
            for (Entry entry : leftBucket) {
                if (entry.key.equals(key)) {
                    return entry.value;
                }
            }
        }

        List<Entry> rightBucket = rightTable.get(rightHash);
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

    // Helper method to compute the hash value for a given key
    private int hash(String key, int size) {
        // Simple hash function: sum of ASCII values of characters modulo size
        int hash = 0;
        for (char c : key.toCharArray()) {
            hash += (int) c;
        }
        return hash % size;
    }
}

