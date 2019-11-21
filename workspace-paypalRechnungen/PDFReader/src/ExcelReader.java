import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.IOException;

public class ExcelReader {

	public static final String SAMPLE_XLSX_FILE_PATH = "D:\\Workspaces\\workspace-paypalRechnungen\\PDFReader\\Kosten.xlsx";
	// D:\Workspaces\workspace-paypalRechnungen\PDFReader\Buchhaltung 2018.xlsm
	// D:\\Workspaces\\workspace-paypalRechnungen\\PDFReader\\testi.xlsx

	public static void main() throws IOException, InvalidFormatException {

		// Creating a Workbook from an Excel file (.xls or .xlsx)
		Workbook workbook = WorkbookFactory.create(new File(SAMPLE_XLSX_FILE_PATH));

		// Retrieving the number of sheets in the Workbook
		System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

		/*
		 * ============================================================= Iterating over
		 * all the sheets in the workbook (Multiple ways)
		 * =============================================================
		 */

		// 2. Or you can use a for-each loop
		System.out.println("Retrieving Sheets using for-each loop");
		for (Sheet sheett : workbook) {
			System.out.println("=> " + sheett.getSheetName());

			/*
			 * ================================================================== Iterating
			 * over all the rows and columns in a Sheet (Multiple ways)
			 * ==================================================================
			 */

			// Getting the Sheet at index zero
			Sheet sheet = workbook.getSheetAt(0);

			// Create a DataFormatter to format and get each cell's value as String
			DataFormatter dataFormatter = new DataFormatter();

			// 2. Or you can use a for-each loop to iterate over the rows and columns
			System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");
			for (Row row : sheet) {
				for (Cell cell : row) {
					String cellValue = dataFormatter.formatCellValue(cell);
					System.out.print(cellValue + "\t");

					if (PDFRead.isNumeric(cellValue)) {
						System.out.println("das ist cellValue : " + cellValue);
						PDFRead.sonstigeKostenGesamt += Double.parseDouble(cellValue);
					}
				}
				System.out.println();
			}
		}
		// Closing the workbook
		workbook.close();
	}
}
