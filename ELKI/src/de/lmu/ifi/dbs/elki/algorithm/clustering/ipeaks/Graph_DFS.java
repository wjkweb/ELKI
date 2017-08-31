package de.lmu.ifi.dbs.elki.algorithm.clustering.ipeaks;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class Graph_DFS {

	public static int[] color;
	private static int count = 0;
	private HashMap allCLuster;
	public void DFS_visit(int[][] array,int n,int count){
		color[n]=1;
		for(int i = n;i<array.length;i++){
			for(int j = 0;j<array.length;j++){
				if(array[n][j]==1){
					if(color[j]==0){
						DFS_visit(array,j,count); 
						Vector v =(Vector) allCLuster.get(count);
						v.add(j);
					}
				} 
			}
		}
		color[n]=2;
	}
	
	public Graph_DFS(){
		allCLuster = new  HashMap();
	}
	
	public HashMap getAllPath(int[][] graph,ArrayList<Integer> centerList)
	{
		for(int i = 0;i<graph.length;i++){
			color[i]=0;
		}
		for(int j = 0;j<graph.length;j++){
			if(color[j]==0){
				System.out.print(centerList.get(j));
				Vector v = new Vector();
				v.add(j);
				count++;
				allCLuster.put(count, v);
				DFS_visit(graph,j,count);
				System.out.println();
			}
		}
		System.out.println();
		return this.allCLuster;
	}
	/**
	 * 主函数
	 */
	public static void main(String[] args) {
		int[][] map = new int[][]{	{0,1,0,0,0,0},
									{1,0,0,0,0,0},
									{0,0,0,1,1,0},
									{0,0,1,0,0,0},
									{0,0,1,0,0,1},
									{0,0,0,0,1,0}};
		color = new int[map.length];
		Graph_DFS demo = new Graph_DFS();
		//demo.getAllPath(map);
		//System.out.println("连通图个数为:"+count);
	}
	

}