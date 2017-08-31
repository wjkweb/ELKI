package de.lmu.ifi.dbs.elki.application;

public class Search {
	public static int binarySearch(Integer[] srcArray, int des) {
	    int low = 0;
	    int high = srcArray.length - 1;
	    int times = 0;
	    while ((low <= high) && (low <= srcArray.length - 1)
	            && (high <= srcArray.length - 1)) {
	        int middle = (high + low)/2 ;
	        times++;
	        System.out.print("第"+times+"次"+":");
	        System.out.println(srcArray[middle]+":");
	        if (des == srcArray[middle]) {
	            return middle;
	        } else if (des < srcArray[middle]) {
	            high = middle - 1;
	        } else {
	            low = middle + 1;
	        }
	    }
	    return -1;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	
		Integer[] srcArray ={1,3,9,12,32,41,45,62,75,77,82,95,100};
		binarySearch(srcArray,82);
	}

}
