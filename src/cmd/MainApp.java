package cmd;
//Tenkaichi Destruction Point Editor v1.1 by ViveTheModder
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class MainApp 
{
	static boolean isForWii, hasReadArg, hasWriteArg;
	static RandomAccessFile res; //res is short for resident (not the evil one)
	static String filename;
	static final int CHARA_COUNT = 161;
	static final String CSV_PATH = "./csv/";
	static final String IN_PATH = "./in/";
	public static void exportCSV(String filename) throws IOException
	{
		String output="chara-name,chara-dp\n";
		File charaCSV = new File(CSV_PATH+"characters.csv");
		Scanner sc = new Scanner(charaCSV);
		
		res.seek(4);
		int progressCharaParamOffset = LittleEndian.getInt(res.readInt());
		int offsetOfFirstDP = progressCharaParamOffset+12;
		
		for (int i=0; i<CHARA_COUNT; i++)
		{
			String input = sc.nextLine();
			String name = input.split(",")[1];
			int pos=(60*i)+offsetOfFirstDP; res.seek(pos);
			short dp = LittleEndian.getShort(res.readShort());
			output+=name+","+dp+"\n";
		}
		sc.close();
		File outputCSV = new File("output-"+filename+".csv");
		FileWriter outputWriter = new FileWriter(outputCSV);
		outputWriter.write(output);
		outputWriter.close();
		System.out.println(outputCSV.getName()+" made successfully!");
	}
	public static void initFunctionality() throws IOException
	{
		File folder = new File("./in/");
		File[] files = folder.listFiles();
		int fileCnt = files.length;
		RandomAccessFile[] resFiles = new RandomAccessFile[fileCnt];
		for (int i=0; i<fileCnt; i++)
			resFiles[i] = new RandomAccessFile(files[i].getAbsolutePath(), "rw");
		for (int i=0; i<fileCnt; i++)
		{
			res = resFiles[i];
			filename = files[i].getName();
			//I would have preferred using regex for this, but my stopwatch said no
			if (filename.endsWith(".unk")) filename=filename.replace(".unk", "");
			else filename=filename.replace(".pak", "");
			if (hasReadArg==true) exportCSV(filename);
			else if (hasWriteArg==true) setDP();
		}
	}
	public static void readFirstArg(String[] args) throws IOException
	{
		if (args[0].equals("-r")) 
		{
			hasReadArg=true; initFunctionality();
		}
		else if (args[0].equals("-w")) 
		{
			hasWriteArg=true; initFunctionality();
		}
		else
		{
			System.out.println("Invalid argument detected. Please enter an argument that is either -r or -w.");
			System.exit(3);
		}
	}
	public static void setDP() throws IOException
	{
		File dpCSV = new File(CSV_PATH+"dp-list.csv");
		Scanner sc = new Scanner(dpCSV);
		sc.nextLine(); //skip header
		
		res.seek(4);
		int progressCharaParamOffset = LittleEndian.getInt(res.readInt());
		int offsetOfFirstDP = progressCharaParamOffset+12;

		while (sc.hasNextLine())
		{
			String input = sc.nextLine();
			String[] inputArray = input.split(",");
			int charaID = Integer.parseInt(inputArray[0]);
			short newDP = Short.parseShort(inputArray[1]);
			int pos=(60*charaID)+offsetOfFirstDP; res.seek(pos);
			//RAF write methods always write in big endian, so the value is turned to big endian beforehand to reverse the process
			newDP = LittleEndian.getShort(newDP);
			res.writeShort(newDP);
		}
		System.out.println(filename+" overwritten successfully!");
		sc.close();
	}
	public static void main(String[] args) throws IOException
	{
		if (args.length>2)
		{
			System.out.println("Too many arguments provided. Please only provide up to 2 arguments.");
			System.exit(1);
		}
		else
		{
			if (args.length==2)
			{
				if (!args[1].equals("-wii"))
				{
					System.out.println("Invalid argument detected. 2nd argument can only be -wii.");
					System.exit(2);
				}
				else isForWii=true;
				readFirstArg(args);
			}
			else if (args.length==1) readFirstArg(args);
			else
			{
				System.out.println("No arguments detected. Please enter an argument that is either -r or -w.");
				System.exit(4);
			}
		}
	}
}