package util;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import conf.Configuration;
import db.BaseLine;
import db.DatbaseConnection;
import db.LogStats;


public class ReadWriteExcelFile {
	
	public static void writeDeviceReport(String status) throws ClassNotFoundException, SQLException, IOException, ParseException {
		String excelFileName = getInfoByFileConfig("saveFileExcel")+status+".xlsx";  //"D:/Test.xlsx";//name of excel file

		String sheetName = getInfoByFileConfig("sheetExcelName");//"DeviceinventoryReport";//name of sheet

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet(sheetName);

		Connection connect = DatbaseConnection.getConnection();
		PreparedStatement ps = null;
			
       try {
    	   String sql = "";
    	   if(status.equals("0")) {
    		   sql = "select  NoLogDecoder, CollectorName, DeviceType, DeviceIP, LogCoder, LastSeenTime, Count, DATE_FORMAT(datemodified, '%Y-%m-%d %T') as DateModified From deviceinventoryreport where IsNormal = 0 and LogCoder IS NOT NULL";
    	   }else if(status.equals("1")) {
    		   sql = "select  NoLogDecoder, CollectorName, DeviceType, DeviceIP, LogCoder, LastSeenTime, Count, DATE_FORMAT(datemodified, '%Y-%m-%d %T') as DateModified From deviceinventoryreport where IsNormal = 1 and LogCoder IS NOT NULL";
    	   }else {
    		   sql = "select  NoLogDecoder, CollectorName, DeviceType, DeviceIP, LogCoder, LastSeenTime, Count, DATE_FORMAT(datemodified, '%Y-%m-%d %T') as DateModified  From deviceinventoryreport where LogCoder IS NOT NULL";
    	   }
    	   
    	   ps = connect.prepareStatement(sql);
           
           ResultSet resultSet = ps.executeQuery();
           
           ResultSetMetaData rsmd = resultSet.getMetaData();

           int numOfCols = rsmd.getColumnCount();
           int numOfRows = 0 ;
           XSSFRow row = sheet.createRow(numOfRows);
           XSSFCell cell = null ;
           
           XSSFCellStyle cellStyle = wb.createCellStyle();
         
           XSSFColor myColor = new XSSFColor(Color.YELLOW);
           XSSFFont font= wb.createFont();
           font.setBold(true);
           font.setFontHeightInPoints((short)11);
           font.setFontName("Tahoma");
           
           cellStyle.setFillForegroundColor(myColor);
           cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
           cellStyle.setFont(font);
           
           cell = row.createCell(0);
           cell.setCellValue("NoLogDecoder");
           cell.setCellStyle(cellStyle);
		   
           cell = row.createCell(1);
           cell.setCellValue("CollectorName");
           cell.setCellStyle(cellStyle);
           
           cell = row.createCell(2);
           cell.setCellValue("DeviceType");
           cell.setCellStyle(cellStyle);
           
           cell = row.createCell(3);
           cell.setCellValue("DeviceIP");
           cell.setCellStyle(cellStyle);
           
           cell = row.createCell(4);
           cell.setCellValue("LogCoder");
           cell.setCellStyle(cellStyle);
           
           cell = row.createCell(5);
           cell.setCellValue("LastSeenTime");
           cell.setCellStyle(cellStyle);
           
           cell = row.createCell(6);
           cell.setCellValue("Count");
           cell.setCellStyle(cellStyle);
           
           cell = row.createCell(7);
           cell.setCellValue("DateModified");
           cell.setCellStyle(cellStyle);
           
           while(resultSet.next()){
        	   numOfRows ++;
        	   row = sheet.createRow(numOfRows);
        	   
        	   for (int c=0;c < numOfCols; c++ ) {
        		   cell = row.createCell(c);
        		   cell.setCellValue(resultSet.getString(c+1));
        	   }
     
        	}  
           
        FileOutputStream fileOut = new FileOutputStream(excelFileName);
   		
   		//write this workbook to an Outputstream.
   		wb.write(fileOut);
   		fileOut.flush();
   		fileOut.close();
   		
   		
   		System.out.println("Write File Completed!!!");
       } catch (SQLException e) {
           e.printStackTrace();
       }finally{
			try {
				connect.close();
			} catch (SQLException e) {
			}			
		}
	}
	
	public static String getInfoByFileConfig(String info) throws IOException {
		String path1 = Configuration.pathDEV; //"C:\\temp\\";
		String path2 = Configuration.pathPROD; //"/apps/";
		
		Properties prop = new Properties();
		InputStream input = null;
	
		String filename = Configuration.fileConfiguration;
		String sheetFileExcel = "";
		
		try{
			input = new FileInputStream(path1+filename);
	 	}catch(Exception e){
	 		input = new FileInputStream(path2+filename);
	 	}
		
		prop.load(input);
		sheetFileExcel = prop.getProperty(info); 
		
		return sheetFileExcel;
	}
	
	
	public static void readFileAndPutBaseLineToDB(String fileName) throws ClassNotFoundException, SQLException, IOException{
		RemoveTableDB("baseline");
        FileInputStream fis;
        XSSFRow row;
        try {
            System.out.println("-------------------------------READING THE SPREADSHEET-------------------------------------");
            fis = new FileInputStream(fileName);
            XSSFWorkbook workbookRead = new XSSFWorkbook(fis);
            XSSFSheet spreadsheetRead = workbookRead.getSheetAt(0);
            
            Iterator< Row> rowIterator = spreadsheetRead.iterator();
            int count = 0;
            while (rowIterator.hasNext()) {
                row = (XSSFRow) rowIterator.next();
                Iterator< Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    cell.setCellType(CellType.STRING);
                    /*switch (cell.getColumnIndex()) {
                        case 0:
                            System.out.print(
                                    cell.getStringCellValue() + " \t\t");
                            break;
                        case 1:
                            System.out.print(
                                    cell.getStringCellValue() + " \t\t");
                            break;
                        case 2:
                            System.out.print(
                                    cell.getStringCellValue() + " \t\t");
                            break;
                        case 3:
                            System.out.print(
                                    cell.getStringCellValue() + " \t\t");
                            break;
                        case 4:
                            System.out.print(
                                    cell.getStringCellValue() + " \t\t");
                            break;
                        case 5:
                            System.out.print(
                                    cell.getStringCellValue() + " \t\t");
                            break;
                       
                    }*/
                }
          
                if(count > 0) {
                	int no = Integer.parseInt(row.getCell(0).getStringCellValue());
                    String fwd = row.getCell(1).getStringCellValue();
                    String device = row.getCell(2).getStringCellValue();
                    String sorce = row.getCell(3).getStringCellValue();
                    String status = row.getCell(4).getStringCellValue();
                    
                    
                    String comment = "";
                    
                    if(null == row.getCell(5)) {
                    	
                    	comment = "";
                    }else {
                    	
                    	comment = row.getCell(5).getStringCellValue();
                    }
                    
                    InsertBaseLineRowInDB(no, fwd, device, sorce, status, comment);
                }
                
                
                count ++;
            }
            System.out.println("Values Inserted Successfully");

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static void readFileAndPutListDeleteToDB(String fileName) throws ClassNotFoundException, SQLException, IOException, ParseException{
		RemoveTableDB("listdelete");
		FileInputStream fis;
        XSSFRow row;
        try {
            System.out.println("-------------------------------READING THE SPREADSHEET-------------------------------------");
            fis = new FileInputStream(fileName);
            XSSFWorkbook workbookRead = new XSSFWorkbook(fis);
            XSSFSheet spreadsheetRead = workbookRead.getSheetAt(0);
            
            Iterator< Row> rowIterator = spreadsheetRead.iterator();
            int count = 0;
            while (rowIterator.hasNext()) {
                row = (XSSFRow) rowIterator.next();
                Iterator< Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    cell.setCellType(CellType.STRING);
                }
          
                if(count > 0) {
                	String noLogDecoder = row.getCell(0).getStringCellValue();
                    String collectorName = row.getCell(1).getStringCellValue();
                    InsertListDelteRowInDB(noLogDecoder, collectorName);           
                }
                count ++;
            }
            System.out.println("Values Inserted Successfully");

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static void readFileAndPutLogStatToDB(String fileName) throws ClassNotFoundException, SQLException, IOException, ParseException{
        FileInputStream fis;
        XSSFRow row;
        try {
            System.out.println("-------------------------------READING THE SPREADSHEET-------------------------------------");
            fis = new FileInputStream(fileName);
            XSSFWorkbook workbookRead = new XSSFWorkbook(fis);
            XSSFSheet spreadsheetRead = workbookRead.getSheetAt(0);
            
            Iterator< Row> rowIterator = spreadsheetRead.iterator();
            int count = 0;
            while (rowIterator.hasNext()) {
                row = (XSSFRow) rowIterator.next();
                Iterator< Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    cell.setCellType(CellType.STRING);
                }
          
                if(count > 0) {
                	
                	String lastSeenTime = row.getCell(4).getStringCellValue();
                	
                	int hour = hoursAgo(lastSeenTime);
                	System.out.println("HOUR " + hour);
                	if(hour >= 24) {
                		String noLogDecoder = row.getCell(0).getStringCellValue();
                        String collectorName = row.getCell(1).getStringCellValue();
                        String deviceType = row.getCell(2).getStringCellValue();
                        String deviceIP = row.getCell(3).getStringCellValue();
                        String count1 = row.getCell(5).getStringCellValue();
                        InsertLogStatRowInDB(noLogDecoder, collectorName, deviceType, deviceIP, lastSeenTime, count1);
                	}
                }
                count ++;
            }
            System.out.println("Values Inserted Successfully");

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static Map<String, String> getTypeofLogDecoder(){
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("Logdecoder1", "HQ");
		map.put("Logdecoder3", "HQ");
		map.put("Logdecoder5", "HQ");
		map.put("Logdecoder7", "HQ");
		map.put("Logdecoder9", "HQ");
		
		map.put("Logdecoder2", "DR");
		map.put("Logdecoder4", "DR");
		map.put("Logdecoder6", "DR");
		map.put("Logdecoder8", "DR");
		map.put("Logdecoder10", "DR");
		
		return map;

	}
	
	public static void readFileAndPutAgencyToDB(String fileName) throws ClassNotFoundException, SQLException, IOException, ParseException{
        RemoveTableDB("agencyindex");
		FileInputStream fis;
        XSSFRow row;
        try {
            System.out.println("-------------------------------READING THE SPREADSHEET-------------------------------------");
            fis = new FileInputStream(fileName);
            XSSFWorkbook workbookRead = new XSSFWorkbook(fis);
            XSSFSheet spreadsheetRead = workbookRead.getSheetAt(0);
            
            Iterator< Row> rowIterator = spreadsheetRead.iterator();
            int count = 0;
            
            Map<String, String> map = getTypeofLogDecoder();
           
            while (rowIterator.hasNext()) {
                row = (XSSFRow) rowIterator.next();
                Iterator< Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    cell.setCellType(CellType.STRING);
                }
          
                if(count > 0) {
                	String noLogdeCoder = row.getCell(0).getStringCellValue();
                	
                	String [] logCodes = noLogdeCoder.split(",");
                	
                	logCodes[1] = "Logdecoder" + logCodes[1];
                	
                	String logCode = "";
                	for(String log : logCodes) {
                		
                		for (Map.Entry<String, String> entry : map.entrySet())
                		{
                		    if(log.equals(entry.getKey())) {
                		    	
                		    	logCode = entry.getValue();
                		    }
                		    	
                		}
                		System.out.println("LOG " + log +  "logCode " + logCode + " count " + count );
                		
                		String noAgency = row.getCell(1).getStringCellValue();
                        String collectorName = row.getCell(2).getStringCellValue();
                        
                        
                        InsertAgencyRowInDB(log, noAgency, collectorName, logCode);
                       
                	}
                }
                count ++;
            }
            System.out.println("Values Inserted Successfully");

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static void RemoveListDeleteFromReportDB() throws ClassNotFoundException, SQLException, IOException {
		 Connection connect = DatbaseConnection.getConnection();
		 PreparedStatement ps = null;
		 
		 try {
				ps = connect.prepareStatement("delete from deviceinventoryreport \r\n" + 
						"where NoLogDecoder in (select NoLogDecoder from listdelete) \r\n" + 
						"and CollectorName in (select CollectorName from listdelete)");
				
				ps.executeQuery();
				
				System.out.println("REMOVE ALL List Deleted !!!!!");
				ps.close();
				 
				 
			
			} catch (SQLException e) {
			
				
				e.printStackTrace();
			}finally{
				try {
					connect.close();
				} catch (SQLException e) {
				}			
			}
	}
	
	
	public static void RemoveTableDB(String table) throws ClassNotFoundException, SQLException, IOException {
		 Connection connect = DatbaseConnection.getConnection();
		 PreparedStatement ps = null;
		 
		 try {
				ps = connect.prepareStatement("delete from " + table);
				
				ps.executeQuery();
				
				System.out.println("REMOVE ALL " + table + " !!!!!");
				ps.close();
				 
				 
			
			} catch (SQLException e) {
			
				
				e.printStackTrace();
			}finally{
				try {
					connect.close();
				} catch (SQLException e) {
				}			
			}
	}
	
	public static void RemoveReport(boolean normal) throws ClassNotFoundException, SQLException, IOException {
		 Connection connect = DatbaseConnection.getConnection();
		 PreparedStatement ps = null;
		 
		 try {
			 	if(normal)
			 		ps = connect.prepareStatement("delete from deviceinventoryreport where Isnormal = " + 1);
			 	else {
			 		ps = connect.prepareStatement("delete from deviceinventoryreport where Isnormal = " + 0);
			 	}
			 	
				ps.executeQuery();
				
				System.out.println("REMOVE ALL deviceinventoryreport !!!!!!");
				ps.close();
				 
				 
			
			} catch (SQLException e) {
			
				
				e.printStackTrace();
			}finally{
				try {
					connect.close();
				} catch (SQLException e) {
				}			
			}
	}
	
	 public static void InsertBaseLineRowInDB(int no, String forwarder, String device, String source, String status, String comment) throws ClassNotFoundException, SQLException, IOException {
		 Connection connect = DatbaseConnection.getConnection();
		 PreparedStatement ps = null;
			
       try {

           String sql = "insert into  baseline(No, Forwarder, Device, Source, Status, Comment) "
           			 + "values ( ?, ?, ?, ?, ?, ?)";
           ps = connect.prepareStatement(sql);
           ps.setInt(1, no);
           ps.setString(2, forwarder);
           ps.setString(3, device);
           ps.setString(4, source);
           ps.setString(5, status);
           ps.setString(6, comment);
           
           ps.executeUpdate();
           connect.close();
       } catch (SQLException e) {
           e.printStackTrace();
       }finally{
			try {
				connect.close();
			} catch (SQLException e) {
			}			
		}
	}
	 
	public static void InsertAgencyRowInDB(String noLogdeCoder, String noAgency, String collectorName, String logCoder) throws ClassNotFoundException, SQLException, IOException {
		 Connection connect = DatbaseConnection.getConnection();
		 PreparedStatement ps = null;
			
        try {

            String sql = "insert into agencyIndex(noLogdeCoder, noAgency, collectorName, logCoder) "
            			 + "values ( ?, ?, ?, ?)";
            ps = connect.prepareStatement(sql);
          
            ps.setString(1, noLogdeCoder);
            ps.setString(2, noAgency);
            ps.setString(3, collectorName);
            ps.setString(4, logCoder);
       
            
            ps.executeUpdate();
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
			try {
				connect.close();
			} catch (SQLException e) {
			}			
		}
	}
	
	public static void InsertListDelteRowInDB(String noLogDecoder, String collectorName) throws ClassNotFoundException, SQLException, IOException {
		 Connection connect = DatbaseConnection.getConnection();
		 PreparedStatement ps = null;
			
       try {

           String sql = "insert into listdelete(NoLogDecoder, CollectorName) "
           			 + "values ( ?, ?)";
           
           ps = connect.prepareStatement(sql);
           ps.setString(1, noLogDecoder);
           ps.setString(2, collectorName);
     
           ps.executeUpdate();
           connect.close();
       } catch (SQLException e) {
           e.printStackTrace();
       }finally{
			try {
				connect.close();
			} catch (SQLException e) {
			}			
		}
	}
	
	
	 public static void InsertLogStatRowInDB(String noLogDecoder, String collectorName, String deviceType, String deviceIP, String lastSeenTime, String count) throws ClassNotFoundException, SQLException, IOException {
		 Connection connect = DatbaseConnection.getConnection();
		 PreparedStatement ps = null;
			
        try {

            String sql = "insert into logstats(NoLogDecoder, CollectorName, DeviceType, DeviceIP, LastSeenTime, Count) "
            			 + "values ( ?, ?, ?, ?, ?, ?)";
            ps = connect.prepareStatement(sql);
            ps.setString(1, noLogDecoder);
            ps.setString(2, collectorName);
            ps.setString(3, deviceType);
            ps.setString(4, deviceIP);
            ps.setString(5, lastSeenTime);
            ps.setString(6,count);
            
            ps.executeUpdate();
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
			try {
				connect.close();
			} catch (SQLException e) {
			}			
		}
	}
	
	public static void readXLSXFile() throws IOException
	{
		String excelFile = getInfoByFileConfig("fileBaseLine");
		
		InputStream ExcelFileToRead = new FileInputStream(excelFile);
		
		XSSFWorkbook  wb = new XSSFWorkbook(ExcelFileToRead);
		
		XSSFWorkbook test = new XSSFWorkbook(); 
		
		XSSFSheet sheet = wb.getSheetAt(0);
		XSSFRow row; 
		XSSFCell cell;

		Iterator rows = sheet.rowIterator();

		while (rows.hasNext())
		{
			row=(XSSFRow) rows.next();
			Iterator cells = row.cellIterator();
			while (cells.hasNext())
			{
				cell=(XSSFCell) cells.next();
				//System.out.println("DATA" + cell.get);
				if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING)
				{
					
					System.out.print(cell.getStringCellValue()+" ");
				}
				else if(cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC)
				{
					
					System.out.print(cell.getNumericCellValue()+" ");
				}
				else
				{
					//U Can Handel Boolean, Formula, Errors
				}
			}
		}
	
	}
	
	public static void writeXLSXFile() throws IOException {
		
		String excelFileName = "D:/Test.xlsx";//name of excel file

		String sheetName = "Sheet1";//name of sheet

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet(sheetName) ;

		//iterating r number of rows
		for (int r=0;r < 5; r++ )
		{
			XSSFRow row = sheet.createRow(r);

			//iterating c number of columns
			for (int c=0;c < 5; c++ )
			{
				XSSFCell cell = row.createCell(c);
	
				cell.setCellValue("Cell "+r+" "+c);
			}
		}

		FileOutputStream fileOut = new FileOutputStream(excelFileName);

		//write this workbook to an Outputstream.
		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();
	}
	
	public static int hoursAgo(String datetime) throws ParseException {
		Calendar date = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.ENGLISH);
		date.setTime(df.parse(datetime)); // Parse into Date object
		Calendar now = Calendar.getInstance(); // Get time now
		
		System.out.println(">> " + date.getTime());
		long differenceInMillis = now.getTimeInMillis() - date.getTimeInMillis();
		long differenceInHours = (differenceInMillis) / 1000L / 60L / 60L; // Divide by millis/sec, secs/min, mins/hr
		    return (int)differenceInHours;
	}
	
	
	public static Map<String, LogStats> getMapLogStat() throws ClassNotFoundException, SQLException, IOException {
		Map<String, LogStats> map = new HashMap<String, LogStats>();
		LogStats log = null;
		Connection connect = DatbaseConnection.getConnection();
		PreparedStatement ps = null;
			
       try {
    	   String sql = "select  CollectorName, DeviceType, DeviceIP, count(*) as Count From logstats\r\n" + 
    	   		"group by   CollectorName, DeviceType, DeviceIP";
           ps = connect.prepareStatement(sql);
           
           ResultSet resultSet = ps.executeQuery();
           
           while(resultSet.next()){
        	   log = new LogStats();
        	   
        	   String collectorName = resultSet.getString("CollectorName");
        	   String deviceType = resultSet.getString("DeviceType");
        	   String deviceIP = resultSet.getString("DeviceIP");
        	   String count = resultSet.getString("Count");
        	   String key = collectorName+ "," + deviceType +  "," + deviceIP +  "," + count;
        			   
        	   log.setCollectorName(collectorName);
        	   log.setDeviceType(deviceType);
        	   log.setDeviceIP(deviceIP);
        	   log.setCount(count);
        	   
        	   map.put(key, log);
           }  
       } catch (SQLException e) {
           e.printStackTrace();
       }finally{
			try {
				connect.close();
			} catch (SQLException e) {
			}			
		}
       
       return map;
	}
	
	public static void saveReportToDB(boolean normal) throws ClassNotFoundException, IOException, SQLException{
		//RemoveReport(normal);
		//RemoveTableDB("deviceinventoryreport");
		System.out.println("Start to Save Report !!!");
		Connection connect = DatbaseConnection.getConnection();
		PreparedStatement ps = null;
		try {
			String listOfIP = "";
			
			Map<String, String> log = getDataCompareBaseLine(normal);
			for (Map.Entry<String, String> entry : log.entrySet()) {
				listOfIP = listOfIP + "'" + entry.getValue() + "',";
			}
			
			listOfIP = listOfIP.substring(0, listOfIP.length()-1);
			
			System.out.println("listOfIP " + listOfIP);
		
    	   String sql = " select lg.NoLogDecoder, lg.CollectorName, lg.DeviceType, lg.DeviceIP, \r\n" + 
    	   		"   (select ag.logCoder from agencyindex ag where lg.NoLogDecoder = ag.NoLogDecoder and lg.CollectorName = ag.collectorName) as LogCoder, \r\n" + 
    	   		"   lg.LastSeenTime, lg.Count" + 
    	   		"   from logstats lg inner join baseline bs on lg.DeviceIP = bs.Source and lg.CollectorName = bs.Forwarder and lg.DeviceType = bs.Device" +
    	   	    "   where \r\n" + 
    	   		"	lg.DeviceIP in (" + listOfIP + ")  and bs.`Status` <> 0";
    	   
           ps = connect.prepareStatement(sql);
           //ps.setString(1, listOfIP);
           ResultSet resultSet = ps.executeQuery();
           connect.close();
           System.out.println("PS " + ps);
           while(resultSet.next()){
        	   
        	   String noLogDecoder = resultSet.getString("NoLogDecoder");
        	   String collectorName = resultSet.getString("CollectorName");
        	   String deviceType = resultSet.getString("DeviceType");
        	   String deviceIP = resultSet.getString("DeviceIP");
        	   String logCoder = resultSet.getString("LogCoder");
        	   String lastSeenTime = resultSet.getString("LastSeenTime");
        	   String count = resultSet.getString("Count");
        	   
        	   System.out.println(noLogDecoder + " " +  collectorName + " " + deviceType + " " +  deviceIP + " " +  logCoder + " ");
        	   
        	   if(normal) {
        		   InsertReportRowInDB(noLogDecoder, collectorName, deviceType, deviceIP, logCoder, lastSeenTime, count, 1);
        		   System.out.println("Sve Normal Report TO DB Completed");
        	   }else {
        		   InsertReportRowInDB(noLogDecoder, collectorName, deviceType, deviceIP, logCoder, lastSeenTime, count, 0);
        		   System.out.println("Sve ABNormal Report TO DB Completed");
        	   }
           
           }  
       } catch (SQLException e) {
           e.printStackTrace();
       }finally{
			try {
				connect.close();
			} catch (SQLException e) {
			}			
		}
    }
	
	
	public static Map<String, BaseLine> getMapBaseLine() throws ClassNotFoundException, SQLException, IOException {
		Map<String, BaseLine> map = new HashMap<String, BaseLine>();
		BaseLine base = null;
		Connection connect = DatbaseConnection.getConnection();
		PreparedStatement ps = null;
			
       try {
    	   String sql = "select distinct Forwarder, Device, Source, Status  from baseLine";
           ps = connect.prepareStatement(sql);
           
           ResultSet resultSet = ps.executeQuery();
           
           while(resultSet.next()){
        	   base = new BaseLine();
        	   
        	   String forwarder = resultSet.getString("Forwarder");
        	   String devicee = resultSet.getString("Device");
        	   String source = resultSet.getString("Source");
        	   String status = "";
        	   
        	   if(resultSet.getString("Status").length() > 0) {
        		   status = resultSet.getString("Status");
        	   }else {
        		   status = "0";
        	   }
        	   
        	   String key = forwarder+ "," + devicee +  "," + source +  "," + status;
        			   
        	   base.setForwarder(forwarder);
        	   base.setDevice(devicee);
        	   base.setSource(source);
        	   base.setStatus(status);
        	   
        	   map.put(key, base);
           }  
       } catch (SQLException e) {
           e.printStackTrace();
       }finally{
			try {
				connect.close();
			} catch (SQLException e) {
			}			
		}
       
       return map;
	}
	
	public static void InsertReportRowInDB(String NoLogDecoder, String CollectorName, String DeviceType, String DeviceIP, String LogCoder, String LastSeenTime, String Count, int normal) throws ClassNotFoundException, SQLException, IOException {
		 Connection connect = DatbaseConnection.getConnection();
		 PreparedStatement ps = null;
			
      try {

          String sql = "insert into  deviceinventoryreport(NoLogDecoder, CollectorName, DeviceType, DeviceIP, LogCoder, LastSeenTime, Count, Isnormal, DateModified) "
          			 + "values ( ?, ?, ?, ?, ?, ?, ?, ?, now())";
          ps = connect.prepareStatement(sql);
          ps.setString(1, NoLogDecoder);
          ps.setString(2, CollectorName);
          ps.setString(3, DeviceType);
          ps.setString(4, DeviceIP);
          ps.setString(5, LogCoder);
          ps.setString(6, LastSeenTime);
          ps.setString(7, Count);
          ps.setInt(8, normal);
          
          
          ps.executeUpdate();
          connect.close();
      } catch (SQLException e) {
          e.printStackTrace();
      }finally{
			try {
				connect.close();
			} catch (SQLException e) {
			}			
		}
	}
	
	public static Map<String, String> getDataCompareBaseLine(boolean normal) throws ClassNotFoundException, SQLException, IOException {
		Map<String, LogStats> mapLog = getMapLogStat();
		Map<String, BaseLine> mapBase = getMapBaseLine();
		
		Map<String, String> mapNormal = new HashMap<String, String>();
		Map<String, String> mapAbNormal = new HashMap<String, String>();
		
		for (Map.Entry<String, LogStats> entryLog : mapLog.entrySet()) {
			String [] keys = entryLog.getKey().split(",");
			String entry1 =  keys[0] + keys[1] + keys[2];
			int count = Integer.parseInt(keys[3]);
			
			for (Map.Entry<String, BaseLine> entryBase : mapBase.entrySet()) {
				String [] key = entryBase.getKey().split(",");
				String entry2 =  key[0] + key[1] + key[2];
				String status = key[3];
				
				if(entry1.equals(entry2)) {
					if(!status.equals("0")) {
						if(count == 2) {
							mapNormal.put(entry2, entryLog.getValue().getDeviceIP());
						}else {
							mapAbNormal.put(entry2, entryLog.getValue().getDeviceIP());
						}
					}
				}
			}
		}
		
		if(normal) {
			return mapNormal;
		}else {
			return mapAbNormal;
		}
	}
	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, ParseException {
		
		//writeXLSFile();
		//readXLSFile();
		
		//writeXLSXFile();
		//readXLSXFile();
		
		//String fileBaseLineToDB = getFileBaseLineConfig();
		
		//readFileAndPutBaseLineToDB(fileBaseLineToDB);
		
		
		//String fileLogStatsToDB = getInfoByFileConfig("fileLogStats");
		//readFileAndPutLogStatToDB(fileLogStatsToDB);
		
		//String fileAgencyToDB = getInfoByFileConfig("fileAgency");
		//readFileAndPutAgencyToDB(fileAgencyToDB);
		
		String fileListDel = getInfoByFileConfig("fileListDelete");
		readFileAndPutListDeleteToDB(fileListDel);
		
		/*String listOfIP = "";
		Map<String, String> log = getDataCompareBaseLine(false);
		for (Map.Entry<String, String> entry : log.entrySet()) {
			System.out.println(  " KEY "  + entry.getKey() + " VAL " + entry.getValue() );
			listOfIP = listOfIP + "'" + entry.getValue() + "',";
		}
		
		
		System.out.println(" listOfIP " + listOfIP);*/
		
		//Map<String, Report> rep = getMapReport();
		
		//saveReportToDB(true);
		//saveReportToDB(false);
		
	
	}

}
