package io.github.SebastianDanielFrenz.Mc2Web;

import org.bukkit.OfflinePlayer;

public class QuickSort {
	/*
	 * This function takes last element as pivot, places the pivot element at
	 * its correct position in sorted array, and places all smaller (smaller
	 * than pivot) to left of pivot and all greater elements to right of pivot
	 */
	public static int partition(int arr[], int low, int high) {
		int pivot = arr[high];
		int i = (low - 1); // index of smaller element
		for (int j = low; j < high; j++) {
			// If current element is smaller than the pivot
			if (arr[j] < pivot) {
				i++;

				// swap arr[i] and arr[j]
				int temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
			}
		}

		// swap arr[i+1] and arr[high] (or pivot)
		int temp = arr[i + 1];
		arr[i + 1] = arr[high];
		arr[high] = temp;

		return i + 1;
	}

	/*
	 * The main function that implements QuickSort() arr[] --> Array to be
	 * sorted, low --> Starting index, high --> Ending index
	 */
	public static void sort(int arr[], int low, int high) {
		if (low < high) {
			/*
			 * pi is partitioning index, arr[pi] is now at right place
			 */
			int pi = partition(arr, low, high);

			// Recursively sort elements before
			// partition and after partition
			sort(arr, low, pi - 1);
			sort(arr, pi + 1, high);
		}
	}

	/*
	 * The main function that implements QuickSort() arr[] --> Array to be
	 * sorted, low --> Starting index, high --> Ending index
	 */
	public static void sort(int arr[]) {
		sort(arr, 0, arr.length - 1);
	}

	public static int partition(OfflinePlayer[] arr, int arrr[], int low, int high) {
		double pivot = Mc2Web.economy.getBalance(arr[high]);
		int i = (low - 1); // index of smaller element
		for (int j = low; j < high; j++) {
			// If current element is smaller than the pivot
			if (Mc2Web.economy.getBalance(arr[j]) > pivot) {
				i++;

				// swap arr[i] and arr[j]
				int temp = arrr[i];
				arrr[i] = arrr[j];
				arrr[j] = temp;
			}
		}

		// swap arr[i+1] and arr[high] (or pivot)
		int temp = arrr[i + 1];
		arrr[i + 1] = arrr[high];
		arrr[high] = temp;

		return i + 1;
	}

	/*
	 * returns an array of the sorted indexes.
	 */
	public static int[] sortPlayersByMoney(OfflinePlayer arr[], int low, int high) {

		int[] arrr = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			arrr[i] = i;
		}
		if (low < high) {

			/*
			 * pi is partitioning index, arr[pi] is now at right place
			 */
			int pi = partition(arr, arrr, low, high);

			// Recursively sort elements before
			// partition and after partition
			sort(arrr, low, pi - 1);
			sort(arrr, pi + 1, high);
		}

		return arrr;
	}

}