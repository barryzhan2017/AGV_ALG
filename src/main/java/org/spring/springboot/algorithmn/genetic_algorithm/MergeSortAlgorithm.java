package org.spring.springboot.algorithmn.genetic_algorithm;

import org.spring.springboot.algorithmn.exception.NoAGVInTheBuffer;

//merge sort for 2 independent array
public class MergeSortAlgorithm {

    public static void mergesort(int[] a, double[] time) throws NoAGVInTheBuffer {
        //To avoid sort useless elements, firstly find the length to element -1.
        int length = -1;
        for (int ele: a) {
            if (ele == -1) {
                break;
            }
            length++;
        }
        if (length == -1) {
            throw new NoAGVInTheBuffer("No AGV is returning to the buffer!");
        }
        int[] temp = new int [length + 1];
        mergesort(a, temp, 0, length, time);
    }
    /**
     * Internal method that makes recursive calls.
     */
    private static void mergesort(int[] a, int[] tempArray, int left, int right, double[] time)
    {
        if (left < right)
        {
            int center = (left + right) / 2;
            mergesort(a, tempArray, left, center, time); // divide
            mergesort(a, tempArray, center + 1, right, time);
            merge(a, tempArray, left, center + 1, right, time); // conquer
        }
    }

    /**
     * Internal method that merges two sorted halves of a subarray.
     */
    private static void merge(int[] a, int[] tempArray, int leftPos,
                              int rightPos, int rightEnd, double[] time)
    {
        int leftEnd = rightPos - 1;
        int numElements = rightEnd - leftPos + 1;
        int tempPos = leftPos; // Ctr counter

        while (leftPos <= leftEnd && rightPos <= rightEnd)
        {
            if (time[a[leftPos]] <= (time[a[rightPos]]))
            {
                tempArray[tempPos++] = a[leftPos++];
            }
            else
            {
                tempArray[tempPos++] = a[rightPos++];
            }
        }

        while (leftPos <= leftEnd)   //Copy rest of first half
        {
            tempArray[tempPos++] = a[leftPos++];
        }

        while(rightPos <= rightEnd)  //Copy rest of right halfw
        {
            tempArray[tempPos++] = a[rightPos++];
        }

        // copy tempArray back
        for(int i = 0; i < numElements; i++, rightEnd--)
        {
            a[rightEnd] = tempArray[rightEnd];
        }
    }
}
