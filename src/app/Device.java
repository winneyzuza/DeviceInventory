package app;

import db.*;
import util.ReadWriteExcelFile;

public class Device {
	public static void main(String[] args) {
		try {
			ReadWriteExcelFile rd = new ReadWriteExcelFile();
			//rd.saveReportToDB(true);
			//rd.saveReportToDB(false);		
			rd.writeDeviceReport("0");

			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
