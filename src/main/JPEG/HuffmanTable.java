package main.JPEG;

import java.util.ArrayList;
import java.util.List;

import main.Util.BitConverter;

public class HuffmanTable {

	
	public int[][] codes;
	
	String[][] LuminanceDC;
	String[][] LuminanceAC;
	String[][] ChrominanceDC;
	String[][] ChrominanceAC;
	int LuminanceACIndex;
	int LuminanceDCIndex;
	int ChrominanceACIndex;
	int ChrominanceDCIndex;
	
	boolean tmp = true;
	
	public HuffmanTable(Object[] content)
	{
		this.extractTablesOfDataSteam(content);
	}
	
	public HuffmanTable(Object[] content0, Object[] content1, Object[] content2, Object[] content3)
	{
		this.initTable(content0);
		this.initTable(content1);
		this.initTable(content2);
		this.initTable(content3);
	}
	//wenn alle HuffmanTables als ein Datenstrom kommen
	private void extractTablesOfDataSteam(Object[] table) //int
	{
		//length 
		int length = (int)table[0] * (16*16) + (int)table[1];
		
		int index = 2;
		//loop
		while (index < length)
		{
			List<Integer> contentSingleTable = new ArrayList<>();
			
			contentSingleTable.add((int)table[index++]); //class and identifier
			int numberOfValues = 0; //total number of encoded values
			
			for (int i = 0; i < 16; i++) //count of Huffman codes, 16 length values
			{
				numberOfValues += (int) table[index];
				contentSingleTable.add((int)table[index]);
				index++;
			}
			
			for (int i = 0; i < numberOfValues; i++) //read encoded values
			{
				contentSingleTable.add((int)table[index++]);
			}
			
			int fullTableLength = numberOfValues + 19; //19=2(Laenge) + 1(Id, Class) + 16(Anzahlen)
			contentSingleTable.add(0, fullTableLength / 256); //das sind die ersten zwei bytes fuer die laenge
			contentSingleTable.add(1, fullTableLength % 256);

			initTable(contentSingleTable.toArray());
		}
	}
	
	

	
	private void initTable(Object[] table)
	{	
		//length 
		int tableLength = (int)table[0] * (16*16) + (int)table[1]; //table.length should be the same; not used...
		
		//code count for each length
		int[] counts = new int[16];
		for(int i=0;i<counts.length;i++) {
			counts[i] = (int) table[i+3];
		}		
		
		//codes sorted by their length
		int tableIndex = 19;
		this.codes = new int[16][];
		int codecount = 0;
		for (int i = 0; i < 16; i++)
		{
			codes[i] = new int[counts[i] + 1];
			codes[i][0] = counts[i];
			codecount += counts[i];
			for (int j = 1; j < counts[i] + 1; j++) //counts[i] +1 = codes[i].length
			{
				codes[i][j] = (int) table[tableIndex++];
			}
		}
		
		//DHT class and ID
		int dhtClass = 0;
		int dhtID = 0;
		int table2 = (int) table[2];
		if (table2 == 0)
		{
			dhtClass = 0;
			dhtID = 0;
			this.LuminanceDC = new String[codecount][2];
			this.LuminanceDCIndex = 0;
		}
		else if (table2 == 1)
		{
			dhtClass = 0;
			dhtID = 1;
			this.ChrominanceDC = new String[codecount][2];
			this.ChrominanceDCIndex = 0;
		}
		else if (table2 == 16)
		{
			dhtClass = 1;
			dhtID = 0;
			this.LuminanceAC = new String[codecount][2];
			this.LuminanceACIndex = 0;
		}
		else if (table2 == 17)
		{
			dhtClass = 1;
			dhtID = 1;
			this.ChrominanceAC = new String[codecount][2];
			this.ChrominanceACIndex = 0;
		}
		//create bits for each code
		TreeNode tree = new TreeNode(null, 0, this, table2, 0);
		
	}
	
	public void setCode(int table, String code, String bits)
	{
		if (table == 0)
		{
			this.LuminanceDC[this.LuminanceDCIndex][0] = bits;
			this.LuminanceDC[this.LuminanceDCIndex][1] = code;
			this.LuminanceDCIndex++;
		}
		else if (table == 1)
		{
			this.ChrominanceDC[this.ChrominanceDCIndex][0] = bits;
			this.ChrominanceDC[this.ChrominanceDCIndex][1] = code;
			this.ChrominanceDCIndex++;
		}
		else if (table == 16)
		{
			this.LuminanceAC[this.LuminanceACIndex][0] = bits;
			this.LuminanceAC[this.LuminanceACIndex][1] = code;
			this.LuminanceACIndex++;
		}
		else if (table == 17)
		{
			this.ChrominanceAC[this.ChrominanceACIndex][0] = bits;
			this.ChrominanceAC[this.ChrominanceACIndex][1] = code;
			this.ChrominanceACIndex++;
		}
	}
	
	public void printHuffmanTables()
	{
		System.out.println("Luminance DC:");
		for (int i = 0; i < this.LuminanceDC.length; i++)
		{
			System.out.println(this.LuminanceDC[i][0] + "|" + this.LuminanceDC[i][1]);
		}
		System.out.println("Luminance AC:");
		for (int i = 0; i < this.LuminanceAC.length; i++)
		{
			System.out.println(this.LuminanceAC[i][0] + "|" + this.LuminanceAC[i][1]);
		}
		System.out.println("Chrominance DC:");
		for (int i = 0; i < this.ChrominanceDC.length; i++)
		{
			System.out.println(this.ChrominanceDC[i][0] + "|" + this.ChrominanceDC[i][1]);
		}
		System.out.println("Chrominance AC:");
		for (int i = 0; i < this.ChrominanceAC.length; i++)
		{
			System.out.println(this.ChrominanceAC[i][0] + "|" + this.ChrominanceAC[i][1]);
		}
	}
}

class TreeNode
{
	TreeNode parent;
	TreeNode child0;
	TreeNode child1;
	
	TreeNode(TreeNode parent, int length, HuffmanTable hufftable, int table, int bits)
	{
		this.parent = parent;
		if (length < 17)
		{
			if(length > 0)
			{
				int codecount = hufftable.codes[length-1][0];
				if (codecount > 0)
				{
					int k = hufftable.codes[length-1].length - hufftable.codes[length-1][0];
					String codetopass = Integer.toString(hufftable.codes[length-1][k]);
					String bitstopass = BitConverter.convertToBitString(bits, length);
					hufftable.setCode(table, codetopass, bitstopass);
					hufftable.codes[length-1][0] -= 1;
				}
				else
				{
					this.child0 = new TreeNode(this, length+1, hufftable, table, bits*2);
					this.child1 = new TreeNode(this, length+1, hufftable, table, bits*2 + 1);
				}
			}
			else
			{
				this.child0 = new TreeNode(this, length+1, hufftable, table, bits*2);
				this.child1 = new TreeNode(this, length+1, hufftable, table, bits*2 + 1);
			}
		}
	}
}
