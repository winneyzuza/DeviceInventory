package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import conf.Configuration;
public class DatbaseConnection {

	
	public DatbaseConnection() {
	}
	
	public static Connection getConnection() throws SQLException, ClassNotFoundException, IOException {
		
		String path1 = Configuration.pathDEV; //"C:\\temp\\";
		String path2 = Configuration.pathPROD; //"/apps/";
		Properties prop = new Properties();
		InputStream input = null;
		String driverName = "";
		String serverName = "";
		String database = "";
		String useJdbc = "";
		String userName = "";
		String passWord = "";
		Connection connection = null;
		try {
			String filename = Configuration.fileConfiguration;
			
			try{
				input = new FileInputStream(path1+filename);
		 	}catch(Exception e){
		 		input = new FileInputStream(path2+filename);
		 	}
			
			prop.load(input);
			driverName = prop.getProperty("driverName"); // org.mariadb.jdbc.Driver
			serverName = prop.getProperty("serverName"); // localhost
			database = prop.getProperty("database"); // deviceinventory
			useJdbc = prop.getProperty("useJdbc"); // jdbc:mariadb://
			userName = prop.getProperty("userName"); // root
			passWord = prop.getProperty("passWord"); // root

			Class.forName(driverName);

		    String url = useJdbc + serverName + "/" + database; 

		    connection = DriverManager.getConnection(url, userName, passWord);
		    
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
	    
	    return connection;
	}
}
