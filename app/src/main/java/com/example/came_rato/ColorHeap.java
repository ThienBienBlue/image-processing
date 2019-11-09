package com.example.came_rato;

import android.graphics.Color;
import android.util.SparseArray;
import android.util.SparseIntArray;

import static android.graphics.Color.alpha;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static java.lang.Math.min;

public class ColorHeap {

    public class ColorCount {
        int m_count;
        int m_color;

        public
        ColorCount(int t_color, int t_count) {
            this.m_color = t_color;
            this.m_count = t_count;
        }
    }

    ColorCount m_color_heap[];
    SparseIntArray m_hash;
    int m_count;
    int m_size;

    //-----------------------
    // Heap Implementation.
    //-----------------------

    public
    ColorHeap() {
       this.m_hash = new SparseIntArray();
       this.m_size = Palette.K_CLUSTERS;
       this.m_count = 0;
       this.m_color_heap = new ColorCount[this.m_size];
    }

    public int
    get_max()
        /* Returns the color that achieved a plurality. */
    {
        if (m_size > 0) {
            return m_color_heap[0].m_color;
        }
        return -1;
    }

    public int
    parent(int i) { return (i-1)/2; }

    public int
    left(int i) { return (2*i + 1); }

    public int
    right(int i) { return (2*i + 2); }

    public void
    increment_color(int t_color)
        /* Increments the count of a color in the heap. */
    {
        int index = read_hash(t_color);
        if (index >= 0) {
            m_color_heap[index].m_count++;

            while (index > 0
                    && m_color_heap[parent(index)].m_count < m_color_heap[index].m_count)
            {
                swap(index, parent(index));
                index = parent(index);
            }
        }
        else {
            write_hash(t_color, m_count);
            m_color_heap[m_count] = new ColorCount(t_color, 1);
            m_count++;
        }
    }

    public void
    decrement_color(int t_color) {
        int index = read_hash(t_color);
        if (index >= 0) {
            m_color_heap[index].m_count--;
            while(max_child(index) >= 0
                   && m_color_heap[index].m_count < m_color_heap[max_child(index)].m_count)
            {
                swap(index, max_child(index));
                index = max_child(index);
            }
        }
    }

    //--------------------
    // Helper Functions.
    //--------------------

    public int
    read_hash(int t_color)
        /* Returns the index of a color in the heap. */
    {
        int index = m_hash.get(t_color);
        if (index < m_count && m_color_heap[index].m_color == t_color) {
            return index;
        }
        return -1;
    }

    public void
    write_hash(int t_color, int t_index)
        /* Given a color, update it's index in the hash. */
    {
        m_hash.put(t_color, t_index);
    }

    public void
    swap(int t_a, int t_b)
        /* Swaps the colors on the ColorHeap given the indices. */
    {
        if (t_a != t_b) {
            swap_index(m_color_heap[t_a].m_color, m_color_heap[t_b].m_color);

            ColorCount a = m_color_heap[t_a];
            m_color_heap[t_a] = m_color_heap[t_b];
            m_color_heap[t_b] = a;
        }
    }

    public void
    swap_index(int t_color_a, int t_color_b) {
        if (t_color_a != t_color_b) {
            int index_a = read_hash(t_color_a);
            int index_b = read_hash(t_color_b);

            write_hash(t_color_a, index_b);
            write_hash(t_color_b, index_a);
        }
    }

    public int
    max_child(int t_parent_index)
        /* Returns the index of the child with the larger count. */
    {
        int left_child = left(t_parent_index);
        int right_child = right(t_parent_index);

        if (left_child < this.m_count && right_child < this.m_count) {
            int count_left = m_color_heap[left_child].m_count;
            int count_right = m_color_heap[right_child].m_count;
            if (count_left > count_right) {
                return left_child;
            }
            return right_child;
        }
        else if (left_child < this.m_count) {
            return left_child;
        }
        else if (right_child < this.m_count) {
            return right_child;
        }
        return -1;
    }
}
