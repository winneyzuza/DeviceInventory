package app;

import db.*;
import util.ReadWriteExcelFile;

public class Device {
	public static void main(String[] args) {
		try {
			ReadWriteExcelFile rd = new ReadWriteExcelFile();
			rd.saveReportToDB(true);
			rd.saveReportToDB(false);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
