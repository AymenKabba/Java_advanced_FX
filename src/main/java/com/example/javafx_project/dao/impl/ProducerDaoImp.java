package com.example.javafx_project.dao.impl;

import com.example.javafx_project.dao.ProducerDao;
import com.example.javafx_project.entities.Producer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ProducerDaoImp implements ProducerDao {
    private Connection conn = DB.getConnection();

    @Override
    public void insert(Producer producer) {
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement("INSERT INTO producer (Name, CIN, Address, PhoneNumber) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, producer.getName());
            ps.setString(2, producer.getCIN());
            ps.setString(3, producer.getAddress());
            ps.setInt(4, producer.getPhoneNumber());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    int id = rs.getInt(1);
                    producer.setId(id);
                }
                DB.closeResultSet(rs);
            } else {
                System.out.println("Aucune ligne renvoyée");
            }
        } catch (SQLException e) {
            System.err.println("Problème d'insertion d'un producteur");
            e.printStackTrace();
        } finally {
            DB.closeStatement(ps);
        }
    }

    @Override
    public void update(Producer producer) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("UPDATE producer SET Name = ?, Cin = ?, Address = ?, PhoneNumber = ? WHERE Id = ?");
            ps.setString(1, producer.getName());
            ps.setString(2, producer.getCIN());
            ps.setString(3, producer.getAddress());
            ps.setInt(4, producer.getPhoneNumber());
            ps.setInt(5, producer.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Problème de mise à jour d'un producteur");
            e.printStackTrace();
        } finally {
            DB.closeStatement(ps);
        }
    }

    @Override
    public void deleteById(Integer id) {
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement("DELETE FROM producer WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Problème de suppression d'un producteur");
            e.printStackTrace();
        } finally {
            DB.closeStatement(ps);
        }
    }

    @Override
    public Producer findById(Integer id) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement("SELECT * FROM producer WHERE id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                Producer producer = new Producer();
                producer.setId(rs.getInt("Id"));
                producer.setName(rs.getString("Name"));
                producer.setCIN(rs.getString("Cin"));
                producer.setAddress(rs.getString("Address"));
                producer.setPhoneNumber(rs.getInt("PhoneNumber"));
                return producer;
            }

            return null;
        } catch (SQLException e) {
            System.err.println("Problème de requête pour trouver un producteur");
            e.printStackTrace();
            return null;
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(ps);
        }
    }

    @Override
    public List<Producer> findAll() {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement("SELECT * FROM producer");
            rs = ps.executeQuery();

            List<Producer> listProducer = new ArrayList<>();

            while (rs.next()) {
                Producer producer = new Producer();
                producer.setId(rs.getInt("Id"));
                producer.setName(rs.getString("Name"));
                producer.setCIN(rs.getString("Cin"));
                producer.setAddress(rs.getString("Address"));
                producer.setPhoneNumber(rs.getInt("PhoneNumber"));
                listProducer.add(producer);
            }

            return listProducer;
        } catch (SQLException e) {
            System.err.println("Problème de requête pour sélectionner un producteur");
            e.printStackTrace();
            return null;
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(ps);
        }
    }

    @Override
    public List<Producer> readFromTextFile(FileReader fileReader) {
        ArrayList<Producer> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(fileReader);
            Producer p = null;
            String readLine = br.readLine();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            while (readLine != null) {
                String[] producer = readLine.split(",");
                p = new Producer();
                p.setId(Integer.valueOf(producer[0].trim()));
                p.setName(producer[1].trim());
                p.setCIN(producer[2].trim());
                p.setAddress(producer[3].trim());
                p.setPhoneNumber(Integer.valueOf(producer[4].trim()));
                list.add(p);
                readLine = br.readLine();
            }
            System.out.println(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void readFromDatabaseToTextFile() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Producer> listProducer = new ArrayList<>();
        try {
            ps = conn.prepareStatement("SELECT * FROM producer");
            rs = ps.executeQuery();
            FileOutputStream fout = new FileOutputStream("src/main/resources/producers.txt");
            while (rs.next()) {
                Producer producer = new Producer();
                producer.setId(rs.getInt("Id"));
                producer.setName(rs.getString("Name"));
                producer.setCIN(rs.getString("Cin"));
                producer.setAddress(rs.getString("Address"));
                producer.setPhoneNumber(rs.getInt("PhoneNumber"));
                listProducer.add(producer);
            }
            for (Producer producer : listProducer) {
                fout.write(producer.toString().getBytes());
                fout.write('\n');
                System.out.println("Producer: " + producer.toString());
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.err.println("Problème de requête pour sélectionner un producteur");
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(ps);
        }
    }

    @Override
    public void readFromStyleSheetAndInsertInDatabase(String path) {
        PreparedStatement ps = null;
        try (FileInputStream fis = new FileInputStream(new File(path))) {
            ps = conn.prepareStatement("INSERT INTO producer (Name, CIN, Address, PhoneNumber) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

            // Iterate over rows
            for (Row row : sheet) {
                // Skip the first row if it contains headers
                if (row.getRowNum() == 0) {
                    continue;
                }

                // Extract data from cells
                Cell nameCell = row.getCell(1);
                Cell cinCell = row.getCell(2);
                Cell addressCell = row.getCell(3);
                Cell phoneNumberCell = row.getCell(4);
                System.out.println(nameCell.getStringCellValue());

                // Set values in the prepared statement
                ps.setString(1, nameCell.getStringCellValue());
                ps.setString(2, cinCell.getStringCellValue());
                ps.setString(3, addressCell.getStringCellValue());
                ps.setInt(4, (int) phoneNumberCell.getNumericCellValue());

                // Execute the insert statement
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    ResultSet rs = ps.getGeneratedKeys();

                    if (rs.next()) {
                        int id = rs.getInt(1);
                        System.out.println("Inserted producer with id: " + id);
                    }
                    DB.closeResultSet(rs);
                } else {
                    System.out.println("Aucune ligne renvoyée");
                }
            }

            workbook.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.err.println("Problème de lecture du fichier Excel ou d'insertion dans la base de données");
        } finally {
            DB.closeStatement(ps);
        }
    }


    public void writingOutPut(ObservableList<Producer> list) {
        try( FileOutputStream fout = new FileOutputStream("src/main/resources/files/outputData.txt"))
        {
            for(Producer producer : list){
                fout.write(producer.toString().getBytes());
                fout.write('\n');

            }
            System.out.println("Data successfully written to the output file.");

        }
        catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
    }

    public void writingJsonFile(ObservableList<Producer> list) {
        File fout = new File("src/main/resources/files/outputData.json");
        try {
            for (Producer producer : list) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(fout, list);
            }
            System.out.println("Data successfully written to the output file.");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void writingExcelFile(ObservableList<Producer> list ) throws Exception{

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Producers");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Name");
        headerRow.createCell(2).setCellValue("CIN");
        headerRow.createCell(3).setCellValue("Address");
        headerRow.createCell(4).setCellValue("Phone Number");

        // Create data rows
        int rowNum = 1;
        for (Producer producer : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(producer.getId());
            row.createCell(1).setCellValue(producer.getName());
            row.createCell(2).setCellValue(producer.getCIN());
            row.createCell(3).setCellValue(producer.getAddress());
            row.createCell(4).setCellValue(producer.getPhoneNumber());
        }

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write the workbook to a file
        FileOutputStream  outputStream = null;
        try {
            outputStream = new FileOutputStream( new File("src/main/resources/files/outputData.xlsx"));
            workbook.write(outputStream);
            System.out.println("Data successfully written to Excel file.");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


    }




    public ObservableList<Producer> readFromJsonFile(FileInputStream file) {
        ObservableList<Producer> list = FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/files/inputDataJson.json"))) {

            ObjectMapper objectMapper = new ObjectMapper();
            Producer[] producers = objectMapper.readValue(file, Producer[].class);
            list.addAll(producers);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ObservableList<Producer> readFromExcelFile(File file) throws Exception {
        ObservableList<Producer> list = FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/files/producerInfo.xlsx"))) {
            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0);

            // Read data rows
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    // Skip header row
                    continue;
                }
                int id = (int) row.getCell(0).getNumericCellValue();
                String name = row.getCell(1).getStringCellValue();
                String cin = row.getCell(2).getStringCellValue();
                String address = row.getCell(3).getStringCellValue();
                String phoneNumber = row.getCell(4).getStringCellValue();

                Producer producer = new Producer(id, name, cin, address, phoneNumber);
                list.add(producer);
            }

            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    public ObservableList<Producer> readFromTextFile(File file) {
        ObservableList<Producer> list = FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/files/inputData.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 5) {
                    int id = Integer.parseInt(values[0]);
                    String name = values[1];
                    String cin = values[2];
                    String address = values[3];
                    String phoneNumber = values[4];

                    Producer producer = new Producer(id, name, cin, address, phoneNumber);
                    list.add(producer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}




