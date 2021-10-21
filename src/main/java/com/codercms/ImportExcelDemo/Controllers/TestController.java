package com.codercms.ImportExcelDemo.Controllers;

import com.codercms.ImportExcelDemo.Entities.UserEntity;
import com.codercms.ImportExcelDemo.Exceptions.UserException;
import com.codercms.ImportExcelDemo.Models.Message;
import com.codercms.ImportExcelDemo.Models.User;
import com.codercms.ImportExcelDemo.Repositories.UserRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TestController {

    @Autowired
    UserRepository userRepository;

    public HashMap<String, List<String>> hm = new HashMap<>();

    @PostMapping("/import-order-excel")
    public ResponseEntity<Message> importExcelFile(@RequestParam("file") MultipartFile files) throws IOException {

        String message = "";
        try {
            List<User> users = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(files.getInputStream());

            // Read user data form excel file sheet.
            XSSFSheet worksheet = workbook.getSheetAt(0);
            for (int index = 0; index < worksheet.getPhysicalNumberOfRows(); index++) {
                if (index > 0) {
                    XSSFRow row = worksheet.getRow(index);

                    User user = new User();


                    user.username = getCellValue(row, 0);
                    user.email = getCellValue(row, 1);
                    // user.password = getCellValue(row, 2);
                    user.contact = getCellValue(row, 2);
                    user.uniqueId = getCellValue(row, 3);
                    if (isValidate(user).isEmpty()) {
                        if (user.username != "" && user.email != "" && user.contact != "" && user.uniqueId != "") {
                            users.add(user);
                        }
                    } else {
                        List<String> exceptions = isValidate(user);
                        hm.put(user.uniqueId, exceptions);
                    }
                }
            }

            // take data from db and check values
            List<UserEntity> dbEntities =  userRepository.findAll();
             boolean updated = false;
               if(dbEntities.size() == users.size())
               {
                   System.out.println("I am in ---- if");
                   for(int i = 0;i<dbEntities.size();i++)
                   {
                       System.out.println("I am in ---- for");
                       if(dbEntities.get(i).uniqueId == users.get(i).uniqueId
                       && dbEntities.get(i).username == users.get(i).username
                               && dbEntities.get(i).contact == users.get(i).contact
                               && dbEntities.get(i).email == users.get(i).email
                       )
                           updated = true;
                   }
               }
               System.out.println(updated);


              if(!updated){
            // Save to db.
            List<UserEntity> entities = new ArrayList<>();
            if (users.size() > 0) {
                users.forEach(x -> {
                    UserEntity entity = new UserEntity();

                    entity.username = x.username;
                    entity.email = x.email;
                    //  entity.password =  x.password;
                    entity.contact = x.contact;
                    entity.uniqueId = x.uniqueId;
                    System.out.println(x.uniqueId);
                    entities.add(entity);
                });


                userRepository.saveAll(entities);
                message = "file is uploaded successful";
                message = message + System.lineSeparator() + hm;
            }
            else{
                System.out.println("File is already updated");
                message = "file contains same value";
                //message = message + System.lineSeparator() + hm;
            }

              //  return ResponseEntity.status(HttpStatus.OK).body(new Message(message));
            }
        } catch (Exception e) {
            e.getStackTrace();
            message = "file is not uploaded successful";
        }
        return ResponseEntity.status(HttpStatus.OK).body(new Message(message));
    }


    /*@GetMapping(value = "/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HashMap<User, List<String>>> fetchAll() {
        HashMap<User, List<String>> list = hm;

        return new ResponseEntity<HashMap<User, List<String>>>(list, new HttpHeaders(),
                HttpStatus.OK);
    }*/

    public  static List<String> isValidate(User user)
    {
        List<String> list = new ArrayList<>();

        try{
            if(isValidValue(user.getUsername())) {

                throw new UserException("Username is not valid");
            }
        }catch(UserException e) {
            System.out.println(e);
            list.add(e.getMessage());
        }
        try{
            if(isValidValue(user.getEmail())) {

                throw new UserException("Email is not valid");
            }
        }catch(UserException e) {
            list.add(e.getMessage());
        }
        try{
            if(isValidValue(user.getContact())) {

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
        return value.length() > 255 ? true: false;
    }


    private String getCellValue(Row row, int cellNo) {
        DataFormatter formatter = new DataFormatter();

        Cell cell = row.getCell(cellNo);

        return formatter.formatCellValue(cell);
    }


}
