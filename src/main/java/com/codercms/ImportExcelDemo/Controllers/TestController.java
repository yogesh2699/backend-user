package com.codercms.ImportExcelDemo.Controllers;

import com.codercms.ImportExcelDemo.Entities.UserEntity;
import com.codercms.ImportExcelDemo.Exceptions.UserException;
import com.codercms.ImportExcelDemo.Models.User;
import com.codercms.ImportExcelDemo.Repositories.UserRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TestController {

    @Autowired
    UserRepository userRepository;


    @PostMapping("/import-order-excel")
    public List<User> importExcelFile(@RequestParam("file") MultipartFile files)throws IOException {
        HashMap<User, List<String>> hm = new HashMap<>();
        List<User> users = new ArrayList<>();
        XSSFWorkbook workbook = new XSSFWorkbook(files.getInputStream());

        // Read student data form excel file sheet1.
        XSSFSheet worksheet = workbook.getSheetAt(0);
        for (int index = 0; index < worksheet.getPhysicalNumberOfRows(); index++) {
            if (index > 0) {
                XSSFRow row = worksheet.getRow(index);

                 User user = new User();


                 user.username = getCellValue(row, 0);
                 user.email =  getCellValue(row, 1);
                 user.password = getCellValue(row, 2);
                 user.contact = getCellValue(row, 3);
                 user.uniqueId = getCellValue(row, 4);
                if(user.username!="" && user.email!="" && user.contact!="" && user.password!="" && user.uniqueId!="") {
                    users.add(user);
                }else{
                    List<String> exceptions = isValidate(user);
                    hm.put(user,exceptions);
                }
            }
        }

        // Save to db.
        List<UserEntity> entities = new ArrayList<>();
        if (users.size() > 0) {
            users.forEach(x->{
                UserEntity entity = new UserEntity();

                entity.username = x.username;
                entity.email =  x.email;
                entity.password =  x.password;
                entity.contact = x.contact;
                entity.uniqueId = x.uniqueId;
                System.out.println(x.uniqueId);
                entities.add(entity);
            });


            userRepository.saveAll(entities);
        }

        return users;
    }


    public  static List<String> isValidate(User user)
    {
        List<String> list = new ArrayList<>();
        boolean testing = false;
        try{
            if(isValidValue(user.getUsername())) {
                testing = true;
                throw new UserException("Username is not valid");
            }
        }catch(UserException e) {
            System.out.println(e);
            list.add(e.getMessage());
        }
        try{
            if(isValidValue(user.getPassword())) {
                testing = true;
                throw new UserException("Password is not valid");
            }
        }catch(UserException e) {
            list.add(e.getMessage());

        }
        try{
            if(isValidValue(user.getEmail())) {
                testing = true;
                throw new UserException("Email is not valid");
            }
        }catch(UserException e) {
            list.add(e.getMessage());
        }
        try{
            if(isValidValue(user.getContact())) {
                testing = true;
                throw new UserException("Contact is not valid");
            }
        }catch(UserException e) {
            list.add(e.getMessage());
        }
        return list;
    }


    private int convertStringToInt(String str) {
        int result = 0;

        if (str == null || str.isEmpty() || str.trim().isEmpty()) {
            return result;
        }

        result = Integer.parseInt(str);

        return result;
    }

    public static boolean isValidValue(String value) {
        return value.length() > 20 ? true: false;
    }


    private String getCellValue(Row row, int cellNo) {
        DataFormatter formatter = new DataFormatter();

        Cell cell = row.getCell(cellNo);

        return formatter.formatCellValue(cell);
    }


}
