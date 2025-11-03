package sura.pruebalegoback.infraestructure.helpers.excel.exporter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.springframework.stereotype.Service;
import sura.pruebalegoback.domain.patient.Patient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExcelExportService implements sura.pruebalegoback.usecase.patient.ExportPatientsToExcelUseCase.ExcelExportService {
    
    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);

    @Override
    public byte[] createExcel(List<Patient> patients) throws IOException {
        log.info("Creando archivo Excel para {} pacientes", patients.size());
        
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Patients");
            
            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "First Name", "Last Name", "Document Number", "Document Type",
                              "Birth Date", "Address", "Phone", "Email", "City", "State",
                              "Admission Date", "Active"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Data
            int rowIdx = 1;
            for (Patient patient : patients) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(patient.getId());
                row.createCell(1).setCellValue(patient.getFirstName());
                row.createCell(2).setCellValue(patient.getLastName());
                row.createCell(3).setCellValue(patient.getDocumentNumber());
                row.createCell(4).setCellValue(patient.getDocumentType());
                row.createCell(5).setCellValue(patient.getBirthDate().toString());
                row.createCell(6).setCellValue(patient.getAddress());
                row.createCell(7).setCellValue(patient.getPhone());
                row.createCell(8).setCellValue(patient.getEmail());
                row.createCell(9).setCellValue(patient.getCity());
                row.createCell(10).setCellValue(patient.getState());
                row.createCell(11).setCellValue(patient.getAdmissionDate().toString());
                row.createCell(12).setCellValue(patient.isActive());
            }
            
            workbook.write(out);
            log.info("Archivo Excel creado exitosamente.");
            return out.toByteArray();
            
        } catch (IOException e) {
            log.error("Error al escribir el archivo Excel: {}", e.getMessage(), e);
            throw e;
        }
    }
}
