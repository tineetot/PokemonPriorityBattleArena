public final class BattleActionMaxHeap { // used for storing the battleactions in order of priority
    private static final int DEFAULT_CAPACITY = 4;

    // In this max heap, each parent ranks at least as high as its children.
    // Array indexes represent the binary tree level by level from left to right.
    private BattleAction[] heap;
    private int size;

    public BattleActionMaxHeap() {
        this(DEFAULT_CAPACITY);
    }

    // initializes an empty heap with the specified initial capacity.
    public BattleActionMaxHeap(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("Heap initial capacity must be at least 1.");
        }

        heap = new BattleAction[initialCapacity];
        size = 0;
    }

    // O(log n): the new action may travel from a leaf to the root.
    public void insert(BattleAction action) {
        if (action == null) {
            throw new IllegalArgumentException("Cannot insert a null battle action.");
        }

        ensureCapacity();
        heap[size] = action;
        size++;
        heapifyUp(size - 1);
    }

    // O(1): the maximum action is always stored at the root.
    public BattleAction peek() {
        ensureNotEmpty("peek at");
        return heap[0];
    }

    // O(log n): the replacement root may travel down the height of the tree.
    public BattleAction extractMax() {
        ensureNotEmpty("extract from");

        BattleAction maximum = heap[0];
        int lastIndex = size - 1;
        heap[0] = heap[lastIndex];
        heap[lastIndex] = null;
        size--;

        if (!isEmpty()) {
            heapifyDown(0);
        }

        return maximum;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int index = 0; index < size; index++) {
            heap[index] = null;
        }
        size = 0;
    }

    public BattleAction[] getHeapSnapshot() {
        BattleAction[] snapshot = new BattleAction[size];
        for (int index = 0; index < size; index++) {
            snapshot[index] = heap[index];
        }
        return snapshot;
    }

    private int parentIndex(int childIndex) {
        return (childIndex - 1) / 2;
    }

    private int leftChildIndex(int parentIndex) {
        return parentIndex * 2 + 1;
    }

    private int rightChildIndex(int parentIndex) {
        return parentIndex * 2 + 2;
    }

    private void swap(int firstIndex, int secondIndex) {
        BattleAction temporary = heap[firstIndex];
        heap[firstIndex] = heap[secondIndex];
        heap[secondIndex] = temporary;
    }

    // Heapify-up swaps a newly inserted action with lower-priority parents.
    private void heapifyUp(int startIndex) {
        int currentIndex = startIndex;

        while (currentIndex > 0) {
            int parentIndex = parentIndex(currentIndex);
            if (heap[currentIndex].comparePriorityTo(heap[parentIndex]) <= 0) {
                break;
            }

            swap(currentIndex, parentIndex);
            currentIndex = parentIndex;
        }
    }

    // Heapify-down swaps a replacement root with its highest-priority child.
    private void heapifyDown(int startIndex) {
        int currentIndex = startIndex;

        while (leftChildIndex(currentIndex) < size) {
            int leftIndex = leftChildIndex(currentIndex);
            int rightIndex = rightChildIndex(currentIndex);
            int higherChildIndex = leftIndex;

            if (rightIndex < size
                    && heap[rightIndex].comparePriorityTo(heap[leftIndex]) > 0) {
                higherChildIndex = rightIndex;
            }

            if (heap[higherChildIndex].comparePriorityTo(heap[currentIndex]) <= 0) {
                break;
            }

            swap(currentIndex, higherChildIndex);
            currentIndex = higherChildIndex;
        }
    }

    private void ensureCapacity() {
        if (size < heap.length) {
            return;
        }

        BattleAction[] expandedHeap = new BattleAction[heap.length * 2];
        for (int index = 0; index < size; index++) {
            expandedHeap[index] = heap[index];
        }
        heap = expandedHeap;
    }

    private void ensureNotEmpty(String operation) {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot " + operation + " an empty battle-action heap.");
        }
    }
}

// Our heap uses a BattleAction array and an integer called size. 
// The array’s length represents its capacity, while size represents how many actions are currently stored. 
// A new action is placed at index size, then size is increased. 
// Heapify-up compares the new action with its parent and swaps them while the child has higher priority. 
// Because the action only travels through the height of the tree, insertion has a time complexity of O(log n).