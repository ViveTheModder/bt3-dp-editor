package cmd;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class MainApp 
{
	static RandomAccessFile res; //res is short for resident (not the evil one)
	static final int CHARA_COUNT = 161;
	static final String CSV_PATH = "./csv/";
	static final String IN_PATH = "./in/";
	public static void exportCSV(String filename) throws IOException
	{
		String output="chara-name,chara-dp\n";
		File charaCSV = new File(CSV_PATH+"characters.csv");
		Scanner sc = new Scanner(charaCSV);
		for (int i=0; i<CHARA_COUNT; i++)
		{
			String input = sc.nextLine();
			String name = input.split(",")[1];
			int pos=(60*i)+44; res.seek(pos);
			short dp = LittleEndian.getShort(res.readShort());
			output+=name+","+dp+"\n";
		}
		sc.close();
		File outputCSV = new File("output-"+filename+".csv");
		FileWriter outputWriter = new FileWriter(outputCSV);
		outputWriter.write(output);
		outputWriter.close();
	}
	public static void setDP() throws IOException
	{
		File dpCSV = new File(CSV_PATH+"dp-list.csv");
		Scanner sc = new Scanner(dpCSV);
		sc.nextLine(); //skip header
		while (sc.hasNextLine())
		{
			String input = sc.nextLine();
			String[] inputArray = input.split(",");
			int charaID = Integer.parseInt(inputArray[0]);
			short newDP = Short.parseShort(inputArray[1]);
			int pos=(60*charaID)+44; res.seek(pos);
			//RAF write methods always write in big endian, so the value is turned to big endian beforehand to reverse the process
			newDP = LittleEndian.getShort(newDP);
			res.writeShort(newDP);
		}
		sc.close();
	}
	public static void main(String[] args) throws IOException
	{
		if (args.length==0)
		{
			System.out.println("No arguments detected. Please enter an argument that is either -r or -w.");
			System.exit(1); //faulty termination
		}
		File folder = new File("./in/");
		File[] files = folder.listFiles();
		int fileCnt = files.length;
		RandomAccessFile[] resFiles = new RandomAccessFile[fileCnt];
		for (int i=0; i<fileCnt; i++)
			resFiles[i] = new RandomAccessFile(files[i].getAbsolutePath(), "rw");
		for (int i=0; i<fileCnt; i++)
		{
			res = resFiles[i];
			String filename = files[i].getName();
			//I would have preferred using regex for this, but my stopwatch said no
			if (filename.endsWith(".unk")) filename=filename.replace(".unk", "");
			else filename=filename.replace(".pak", "");
			if (args[0].equals("-r")) exportCSV(filename);
			else if (args[0].equals("-w")) setDP();
			else
			{
				System.out.println("Invalid argument detected. Please enter an argument that is either -r or -w.");
				System.exit(2); //faulty termination
			}
		}
	}
}