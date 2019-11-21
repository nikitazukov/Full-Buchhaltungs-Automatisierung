import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//import statements
public class WriteExcel
{
	//write Daten into Object[]
	public Map<String, Object[]> data;
	
	public void setMapObject(Map<String, Object[]> data) {
		this.data = data;
	}
	
	public Map<String, Object[]> getMapObject() {
		return data;
	}
	
		public void putDataObject() {

		//Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
         
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Employee Data");

        //Iterate over data and write to sheet
        Set<String> keyset =  getMapObject().keySet();
        int rownum = 5;
        for (String key : keyset)
        {
            Row row = sheet.createRow(rownum++);
            Object [] objArr =  getMapObject().get(key);
            int cellnum = 0;
            for (Object obj : objArr)
            {
               Cell cell = row.createCell(cellnum++);
               if(obj instanceof String)
                    cell.setCellValue((String)obj);
                else if(obj instanceof Integer)
                    cell.setCellValue((Integer)obj);
            }
        }
        
        try
        {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File("Buchhaltung_2019.xlsx"));
            workbook.write(out);
            out.close();
            System.out.println("Buchhaltung wurde durchgeführt!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
		
		
	}
