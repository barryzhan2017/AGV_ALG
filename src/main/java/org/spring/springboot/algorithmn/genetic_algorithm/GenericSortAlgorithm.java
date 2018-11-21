package org.spring.springboot.algorithmn.genetic_algorithm;

//merge sort and insert sort
public class GenericSortAlgorithm<T> {

    public static  <T extends Comparable<? super T>>void insertSort(int[] a, T[] compareTool) {
        int i, j, insertNote;// 要插入的数据
        for (i = 1; i < a.length; i++) {// 从数组的第二个元素开始循环将数组中的元素插入
            insertNote = a[i];// 设置数组中的第2个元素为第一次循环要插入的数据
            //特殊情况不再排序
            if (insertNote == -1) {
                break;
            }
            j = i - 1;
            while (j >= 0 && compareTool[insertNote].compareTo(compareTool[a[j]]) < 0 ) {
                a[j + 1] = a[j];// 如果要插入的元素小于第j个元素,就将第j个元素向后移动
                j--;
            }
            a[j + 1] = insertNote;// 直到要插入的元素不小于第j个元素,将insertNote插入到数组中
        }
    }

    public static <T extends Comparable<? super T>> void mergesort(T[] a)
    {
        T[] temp = (T[]) new Comparable[a.length];
        mergesort(a, temp, 0, a.length - 1);
    }
    /**
     * Internal method that makes recursive calls.
     */
    private static <T extends Comparable<? super T>> void mergesort(T[] a,
                                                                    T[] tempArray, int left, int right)
    {
        if (left < right)
        {
            int center = (left + right) / 2;
            mergesort(a, tempArray, left, center); // divide
            mergesort(a, tempArray, center + 1, right);
            merge(a, tempArray, left, center + 1, right); // conquer
        }
    }

    /**
     * Internal method that merges two sorted halves of a subarray.
     */
    private static <T extends Comparable<? super T>> void merge(T[] a,
                                                                T[] tempArray, int leftPos, int rightPos, int rightEnd)
    {
        int leftEnd = rightPos - 1;
        int numElements = rightEnd - leftPos + 1;
        int tempPos = leftPos; // Cctr counter

        while (leftPos <= leftEnd && rightPos <= rightEnd)
        {
            if (a[leftPos].compareTo(a[rightPos]) <= 0)
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
