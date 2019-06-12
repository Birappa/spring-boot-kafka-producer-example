package com.techprimers.kafka.springbootkafkaproducerexample.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import com.techprimers.kafka.springbootkafkaproducerexample.model.Employee;
import com.techprimers.kafka.springbootkafkaproducerexample.model.User;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("kafka")
public class UserResource {

    @Autowired
    private KafkaTemplate<String, User> kafkaTemplate;
    
    @Autowired
    private KafkaTemplate<String, Employee> empKafkaTemplate;

    private static final String TOPIC = "Kafka_Example_json";
    private static final String TOPIC1 = "Employee";

    @GetMapping("/publish/{name}")
    public String post(@PathVariable("name") final String name) {

        kafkaTemplate.send(TOPIC, new User(name, "Technology", 12000L));

        return "Published successfully";
    }
    
    
    @GetMapping(value = "/readfromexcel")
	public String sendMessageToKafkaTopicFromExcel() throws IOException {

		final String fileLocation = "C:/Users/birpatil/Documents/Charter Project/employee_details.xlsx";
		FileInputStream file;
		Employee emp = new Employee();
		try {
			file = new FileInputStream(new File(fileLocation));

			Workbook workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);

			// Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				// For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();

				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();

					// Check the cell type and format accordingly
					switch (cell.getCellType()) {
					case NUMERIC:
						if (cell.getColumnIndex() == 0) {
							emp.setEmpId((long) cell.getNumericCellValue());
						}else{
							emp.setSalary((long) cell.getNumericCellValue());
						}
						//System.out.print(cell.getNumericCellValue() + "\t");
						break;
					case STRING:
						if (cell.getColumnIndex() == 1) {
							emp.setName(cell.getStringCellValue());
						} else if(cell.getColumnIndex() == 2){
							emp.setPosition(cell.getStringCellValue());
						}else if(cell.getColumnIndex() == 3){
							emp.setDept(cell.getStringCellValue());
						}else{
							emp.setCompany(cell.getStringCellValue());
						}
						//System.out.print(cell.getStringCellValue() + "(" + cell.getColumnIndex() + ")\t");
						break;
					default:
						break;
					}
				}
				//System.out.println("");
				this.empKafkaTemplate.send(TOPIC1,emp);
			}
			file.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "Data published successsfully";
	}

}
