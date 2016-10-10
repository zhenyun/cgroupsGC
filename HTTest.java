import java.io.*;
import java.util.*;
import java.text.*;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

public class HTTest {
	static int ThreadNum = 1;
	static int Duration = 300;
	static int ArraySize = 102;
	static int CountDownSize = 1000 * 1000 * 30;
	static int reportInterval = 2; 

	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			ThreadNum = Integer.parseInt(args[0]);
			Duration = Integer.parseInt(args[1]);
		}

		for (int i = 0; i < ThreadNum; i++) {
			String filename = Integer.toString(i) + ".log";
			LoadThread thread = new LoadThread(i, filename);
			thread.start();
		}

	}
}

class LoadThread extends Thread {
	long timeZero = 0;
	long finishedUnit = 0;
	long threadid;
	PrintWriter file_th;
	PrintWriter file_finish;
	PrintWriter file_latency;

	public LoadThread(int id, String Filename) {
		timeZero = System.currentTimeMillis();
	}

	public void run() {
		try {
			threadid = Thread.currentThread().getId();
			file_th = new PrintWriter("throughput." + Long.toString(threadid)
					+ ".csv", "UTF-8");
			file_finish = new PrintWriter("finish." + Long.toString(threadid)
					+ ".csv", "UTF-8");
			file_latency = new PrintWriter("latency." + Long.toString(threadid)
					+ ".csv", "UTF-8");
		} catch (Exception e) {
		}

		AbstractQueue<String> q = new ArrayBlockingQueue<String>(
				HTTest.CountDownSize * 2);
		char[] srcArray = new char[HTTest.ArraySize];
		String emptystr = new String(srcArray);
		finishedUnit = 0;
		long prevTime = timeZero;
		long prevFinished = 0;
		boolean stopRun = false;
		long reportUnit = 1000 * 1000 * 1;

		while (!stopRun) {
			if (q.size() >= HTTest.CountDownSize) {
				String strHuge_remove = null;
				for (int j = 0; j < HTTest.CountDownSize / 10; j++) {
					strHuge_remove = q.remove();
				}
			}

			if (q.size() % 100 == 0) {
				long curTime = System.currentTimeMillis();
				long timeDiff = curTime - prevTime;

				if (timeDiff > HTTest.reportInterval * 1000) {
					prevTime = curTime;
					long totalTime = curTime - timeZero;
					long deltaFinished = finishedUnit - prevFinished;
					prevFinished = finishedUnit;

					Date dNow = new Date();
					SimpleDateFormat ft = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss.SSSSS");
					System.out.println(ft.format(dNow) + " thread_id="
							+ threadid + " last throughput (unit/second)="
							+ deltaFinished / reportUnit / (timeDiff / 1000.0));
					System.out.println(ft.format(dNow) + " thread_id="
							+ threadid + " finished Units =" + finishedUnit
							/ reportUnit);
					file_th.println(ft.format(dNow) + "," + deltaFinished
							/ reportUnit / (timeDiff / 1000.0));
					file_finish.println(ft.format(dNow) + "," + finishedUnit
							/ reportUnit);
				}

				if (curTime - timeZero > HTTest.Duration * 1000) {
					stopRun = true;
					file_th.close();
					file_finish.close();
					file_latency.close();
				}
			}

			long t1 = System.currentTimeMillis();
			srcArray = new char[HTTest.ArraySize];
			emptystr = new String(srcArray);
			String str = emptystr.replace('\0', 'a');
			q.add(str);
			finishedUnit++;
			long td = System.currentTimeMillis() - t1;
			if (td > 50) {
				Date dNow = new Date();
				SimpleDateFormat ft = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss.SSSSS");
				file_latency.println(ft.format(dNow) + "," + Long.toString(td));
			}
		}
	}
}
