package app;

import db.*;
import util.ReadWriteExcelFile;

public class Device {
	public static void main(String[] args) {
		try {
			ReadWriteExcelFile rd = new ReadWriteExcelFile();
			// BASE LINE
			String fileBaseLineToDB = rd.getInfoByFileConfig("fileBaseLine");
			rd.readFileAndPutBaseLineToDB(fileBaseLineToDB);
			
			// Agency Index
			String fileAgencyToDB = rd.getInfoByFileConfig("fileAgency");
			rd.readFileAndPutAgencyToDB(fileAgencyToDB);
			
			// Log Stat
			rd.RemoveTableDB("logstats"); // no need to delete every day.
			String fileLogStatsToDB = rd.getInfoByFileConfig("fileLogStats");
			rd.readFileAndPutLogStatToDB(fileLogStatsToDB);
			
			// List Delete
			String fileListDel = rd.getInfoByFileConfig("fileListDelete");
			rd.readFileAndPutListDeleteToDB(fileListDel);
			
			//clear table deviceinventoryreport
			rd.RemoveTableDB("deviceinventoryreport");
			
			// save to table deviceinventoryreport
			rd.saveReportToDB(true);
			rd.saveReportToDB(false);
			
			//remove list from table listdelete
			rd.RemoveListDeleteFromReportDB();
			
			//write csv file
			rd.writeDeviceReport("1");
			rd.writeDeviceReport("0");
			//rd.writeDeviceReport("ALL");
			
			//write csv for Diff4LogstatAndBaseLine
			rd.writeDiffLogStatAndBaseLineReport("Diff4LogstatAndBaseLine");
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
