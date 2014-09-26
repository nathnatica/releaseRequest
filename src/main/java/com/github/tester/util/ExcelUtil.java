package com.github.tester.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelUtil {
    final static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);
    
    public static Workbook getWorkbook(String file) throws IOException {
        Workbook wb = null;
        if (StringUtils.endsWith(file, ".xlsx") || StringUtils.endsWith(file, ".xlsm")) {
            wb = new XSSFWorkbook(new FileInputStream(new File(file)));
        } else if (StringUtils.endsWith(file, ".xls")) {
            wb = new HSSFWorkbook(new FileInputStream(new File(file)));
        }
        return wb;
    }

    public static void writeFile(Workbook wb, String file) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(file.replace(".xls", "(checked)_" + TimeUtil.getYYYYMMDDHH24MISS() + ".xls"));
        wb.write(fileOut);
        fileOut.close();
        logger.info("{} file has been written", file);
    }
    
    
    public static void setOK(Cell c, Workbook wb) {
        c.setCellValue("OK");
        XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
//        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 128, 0)));
        style.setFillForegroundColor(new XSSFColor(Color.GREEN));
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        c.setCellStyle(style);
    }
    public static void setNG(Cell c, Workbook wb) {
        c.setCellValue("NG");
        XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
//        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(128, 0, 0)));
        style.setFillForegroundColor(new XSSFColor(Color.RED));
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        c.setCellStyle(style);
    }
    
    
    
    public static void setValue(String location, String value, Sheet sh) {
        getCell(location, sh).setCellValue(value);
    }
    
    private static Cell getCell(String location, Sheet sh) {
        String[] l = location.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        int rowIndex = Integer.parseInt(l[1]) -1;
        
        String t = l[0];
        int position = getCellColumn(t);
        return sh.getRow(rowIndex).getCell(position);
    }

    public static int getCellColumn(String t) {
        String reversed = new StringBuilder(t.toLowerCase()).reverse().toString();
        int position = 0;
        for (int i=0; i<reversed.length(); i++) {
            int temp = (reversed.charAt(0) - 'a') + 1;
            if (i>0) {
                int tt = (reversed.charAt(1) - 'a') + 1;
                position = (26*tt) + position;
            } else {
                position = position + temp;
            }
        }
        return position -1;
    }

//	public static int getCellColumn(String t) {
//        String input = t.toLowerCase();
//        int position = 0;
//        for (int i=input.length()-1; i>=0; i--) {
//            int temp = input.charAt(i) - 'a';
//            if (i>0) {
//                position = (temp + 26) + position;
//            } else {
//                position = position + temp;
//            }
//        }
//        return position;
//    }

}
