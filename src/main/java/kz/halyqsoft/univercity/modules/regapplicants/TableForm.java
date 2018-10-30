package kz.halyqsoft.univercity.modules.regapplicants;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import kz.halyqsoft.univercity.entity.beans.univercity.PDF_PROPERTY;
import kz.halyqsoft.univercity.modules.pdf.CustomField;
import kz.halyqsoft.univercity.utils.CommonUtils;
import kz.halyqsoft.univercity.utils.EmployeePdfCreator;
import org.r3a.common.dblink.facade.CommonEntityFacadeBean;
import org.r3a.common.dblink.utils.SessionFacadeFactory;
import org.r3a.common.entity.ID;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TableForm {
    private Document document;
    private PdfPTable pdfPTable;
    private ByteArrayOutputStream byteArrayOutputStream;
    private ID studentId;
    private static int fontSize = 10;
    public TableForm(Document document, ID studentID) {
        this.document = document;
        this.studentId = studentID;
        initialize();
    }

    public void initialize() {
        try {
            PdfWriter pdfWriter = PdfWriter.getInstance(this.document, byteArrayOutputStream);
            // pdfWriter.open();

            PdfPTable table = new PdfPTable(7);
            document.add(new Paragraph(" Семестрде оқылатын пәндер тізімі:", EmployeePdfCreator.getFont(fontSize, Font.BOLD)));

            insertCell(table, "СЕМЕСТР 1", Element.ALIGN_CENTER, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            insertCell(table, "Міндетті түрде оқытылатын пәндер:", Element.ALIGN_LEFT, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            insertCell(table, "Пәннің коды", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            insertCell(table, "Модуль түрлері", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            insertCell(table, "Пәннің толық атауы", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            insertCell(table, "Кредит саны", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            insertCell(table, "ЕСТS", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            insertCell(table, "Семестр", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            insertCell(table, "Тьютордың аты-жөні", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            table.setWidthPercentage(100);



            String sql = "SELECT\n" +
                    "  DISTINCT s4.code,\n" +
                    "  module.module_short_name moduleType,\n" +
                    "  subj.name_RU                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  sem.semester_name   ,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id" +
                    "   WHERE   sem_data.semester_period_id=1\n" +
                    "      AND usr.deleted = FALSE AND\n" +
                    "      subj.subject_cycle_id IS NOT NULL " +
                    "  AND subj.module_id  != 3\n " +
                    "  AND subj.mandatory=TRUE AND\n" +
                    "      usr.locked = FALSE AND usr.id = " + studentId;

            Map<Integer, Object> params = new HashMap<>();
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sql, params);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 7; i++) {
                            insertCell(table, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }
                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }
            insertCell(table, "Студенттің таңдаған пәндері:", Element.ALIGN_LEFT, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            String sql1 = "SELECT\n" +
                    "  DISTINCT s4.code,\n" +
                    "  module.module_short_name moduleType,\n" +
                    "  subj.name_RU                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  sem.semester_name   ,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id" +
                    "   WHERE   sem_data.semester_period_id=1\n" +
                    "      AND usr.deleted = FALSE AND\n" +
                    "      subj.subject_cycle_id IS NOT NULL\n" +
                    "  AND subj.mandatory=FALSE AND " +
                    "  subj.module_id  != 3 AND \n" +
                    "      usr.locked = FALSE AND usr.id = " + studentId;
            Map<Integer, Object> param = new HashMap<>();
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sql1, param);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 7; i++) {
                            insertCell(table, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }

                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            insertCell(table, "Студенттің қосымша пәндері:\t", Element.ALIGN_LEFT, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            String sql2 = "SELECT\n" +
                    "  DISTINCT s4.code,\n" +
                    "  module.module_short_name moduleType,\n" +
                    "  subj.name_RU                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  sem.semester_name   ,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id \n" +
                    "WHERE  usr.deleted = FALSE AND\n" +
                    "      subj.subject_cycle_id IS NOT NULL\n" +
                    "AND subj.module_id=3\n" +
                    "  AND usr.locked = FALSE AND usr.id = " + studentId;
            Map<Integer, Object> para = new HashMap<>();
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sql2, para);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 7; i++) {
                            insertCell(table, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }

                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            String sql3 = "SELECT  DISTINCT sum( credit.credit) credit, sum( ects.ects) ects\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id\n" +
                    "WHERE\n" +
                    "  usr.id =  " + studentId +
                    " AND sem_data.semester_period_id=1";

            insertCell(table, ("Семестрде барлығы"), Element.ALIGN_LEFT, 3, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));

            Map<Integer, Object> par = new HashMap<>();
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sql3, par);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 2; i++) {
                            insertCell(table, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }

                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            insertCell(table, (" "), Element.ALIGN_LEFT, 2, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
            insertCell(table, "СЕМЕСТР 2", Element.ALIGN_CENTER, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));


            insertCell(table, "Міндетті түрде оқытылатын пәндер:", Element.ALIGN_LEFT, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            String sqlSem2 = "SELECT DISTINCT\n" +
                    "  s4.code,\n" +
                    "  module.module_short_name moduleType,\n" +
                    "  subj.name_RU                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  sem.semester_name   ,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id " +
                    "   WHERE   sem_data.semester_period_id=2\n" +
                    "      AND usr.deleted = FALSE AND\n" +
                    "      subj.subject_cycle_id IS NOT NULL " +
                    "  AND subj.module_id  != 3\n " +
                    "  AND subj.mandatory=TRUE AND\n" +
                    "      usr.locked = FALSE AND usr.id = " + studentId;
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlSem2, params);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 7; i++) {
                            insertCell(table, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }

                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }
            insertCell(table, "Студенттің таңдаған пәндері:", Element.ALIGN_LEFT, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            String sqlAdd = "SELECT DISTINCT\n" +
                    "  s4.code,\n" +
                    "  module.module_short_name moduleType,\n" +
                    "  subj.name_RU                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  sem.semester_name   ,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id\n" +
                    "   WHERE   sem_data.semester_period_id=2\n" +
                    "      AND usr.deleted = FALSE AND\n" +
                    "      subj.subject_cycle_id IS NOT NULL\n" +
                    "  AND subj.mandatory=FALSE AND " +
                    "  subj.module_id  != 3 AND \n" +
                    "      usr.locked = FALSE AND usr.id = " + studentId;
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlAdd, param);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 7; i++) {
                            insertCell(table, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }
                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            insertCell(table, "Студенттің қосымша пәндері:", Element.ALIGN_LEFT, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            String sqlStud = "SELECT DISTINCT\n" +
                    "  s4.code,\n" +
                    "  module.module_short_name moduleType,\n" +
                    "  subj.name_RU                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  sem.semester_name   ,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id" +
                    "   WHERE  usr.deleted = FALSE AND\n" +
                    "      subj.subject_cycle_id IS NOT NULL\n" +
                    "AND subj.module_id=3\n" +
                    "  AND usr.locked = FALSE AND usr.id = " + studentId;
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlStud, para);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 7; i++) {
                            insertCell(table, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }
                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            String sqlSum = "SELECT DISTINCT sum( credit.credit) credit, sum( ects.ects) ects\n" +
                    "FROM student_subject stu_subj " +
                    " INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id\n" +
                    "   WHERE\n" +
                    "  usr.id = " + studentId +
                    "  AND sem_data.semester_period_id=2";

            insertCell(table, ("Семестрде барлығы"), Element.ALIGN_LEFT, 3, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));

            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlSum, par);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 2; i++) {
                            insertCell(table, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }
                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            insertCell(table, (" "), Element.ALIGN_LEFT, 2, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));


            PdfPTable table1 = new PdfPTable(6);
            insertCell(table1, "Пәннің коды", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            insertCell(table1, "Пәннің толық атауы", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            insertCell(table1, "Кредит саны", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            insertCell(table1, "ECTS", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            insertCell(table1, "Оқытушының аты-жөні", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            insertCell(table1, "Емтихан", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            insertCell(table1, "СЕМЕСТР 1", Element.ALIGN_CENTER, 6, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            insertCell(table1, "Міндетті түрде оқытылатын пәндер:", Element.ALIGN_LEFT, 6, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            table1.setWidthPercentage(100);

            String sqlTeacherSem1 = "SELECT DISTINCT\n" +
                    "  s4.code,\n" +
                    "  subj.name_kz                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO,\n" +
                    "  control.type_name                                                              examType\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id " +
                    "   WHERE\n" +
                    "  sem_data.semester_period_id=1 AND\n" +
                    "       usr.deleted = FALSE AND\n" +
                    "      subj.subject_cycle_id IS NOT NULL   AND subj.module_id  != 3\n" +
                    "      AND subj.mandatory=TRUE AND\n" +
                    "      usr.locked = FALSE AND usr.id = " + studentId;
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlTeacherSem1, params);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 6; i++) {
                            insertCell(table1, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }
                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }
            insertCell(table1, "Студенттің таңдаған пәндері:", Element.ALIGN_LEFT, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            String sqlTeaAdd = "SELECT DISTINCT\n" +
                    "  s4.code,\n" +
                    "  subj.name_kz                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO,\n" +
                    "  control.type_name                                                              examType\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id " +
                    "   WHERE\n" +
                    "  sem_data.semester_period_id=1\n" +
                    "  AND usr.deleted = FALSE AND\n" +
                    "  subj.subject_cycle_id IS NOT NULL\n" +
                    "  AND subj.mandatory=FALSE AND\n" +
                    "  subj.module_id  != 3 AND\n" +
                    "  usr.locked = FALSE AND usr.id = " + studentId;
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlTeaAdd, param);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 6; i++) {
                            insertCell(table1, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }


                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            insertCell(table1, "Студенттің қосымша пәндері:", Element.ALIGN_LEFT, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            String sqlTeachAdd = "SELECT DISTINCT\n" +
                    "  s4.code,\n" +
                    "  subj.name_kz                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO,\n" +
                    "  control.type_name                                                              examType\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id \n" +
                    "   WHERE\n" +
                    "  usr.deleted = FALSE AND\n" +
                    "  subj.subject_cycle_id IS NOT NULL AND subj.module_id=3\n" +
                    "AND\n" +
                    "  usr.locked = FALSE AND usr.id = " + studentId;
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlTeachAdd, para);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 6; i++) {
                            insertCell(table1, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }


                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            String sqlTeachSum = "SELECT DISTINCT sum( credit.credit) credit, sum( ects.ects) ects\n" +
                    "FROM student_subject stu_subj\n" +
                    " INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id " +
                    "   WHERE\n" +
                    "  usr.id = " + studentId +
                    " AND sem_data.semester_period_id=1";

            insertCell(table1, ("Семестрде барлығы"), Element.ALIGN_LEFT, 2, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));

            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlTeachSum, par);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 2; i++) {
                            insertCell(table1, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }
                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            insertCell(table1, (" "), Element.ALIGN_LEFT, 2, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));


            insertCell(table1, "СЕМЕСТР 2", Element.ALIGN_CENTER, 6, EmployeePdfCreator.getFont(fontSize, Font.BOLD));
            insertCell(table1, "           Міндетті түрде оқытылатын пәндер:", Element.ALIGN_LEFT, 6, EmployeePdfCreator.getFont(fontSize, Font.BOLD));


            String sqlTeacherSem2 = "SELECT DISTINCT\n" +
                    "  s4.code,\n" +
                    "  subj.name_kz                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO,\n" +
                    "  control.type_name                                                              examType\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id\n" +
                    "   WHERE\n" +
                    "  sem_data.semester_period_id=2 AND\n" +
                    "       usr.deleted = FALSE AND\n" +
                    "      subj.subject_cycle_id IS NOT NULL   AND subj.module_id  != 3\n" +
                    "      AND subj.mandatory=TRUE AND\n" +
                    "      usr.locked = FALSE AND usr.id = " + studentId;
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlTeacherSem2, params);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 6; i++) {
                            insertCell(table1, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }


                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }
            insertCell(table1, "Студенттің таңдаған пәндері:", Element.ALIGN_LEFT, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            String sqlTeaAdd2 = "SELECT DISTINCT\n" +
                    "  s4.code,\n" +
                    "  subj.name_kz                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO,\n" +
                    "  control.type_name                                                              examType\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id\n" +
                    "   WHERE\n" +
                    "  sem_data.semester_period_id=2\n" +
                    "  AND usr.deleted = FALSE AND\n" +
                    "  subj.subject_cycle_id IS NOT NULL\n" +
                    "  AND subj.mandatory=FALSE AND\n" +
                    "  subj.module_id  != 3 AND\n" +
                    "  usr.locked = FALSE AND usr.id = " + studentId;
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlTeaAdd2, param);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 6; i++) {
                            insertCell(table1, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }

                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            insertCell(table1, "Студенттің қосымша пәндері:", Element.ALIGN_LEFT, 7, EmployeePdfCreator.getFont(fontSize, Font.BOLD));

            String sqlTeachAdd2 = "SELECT DISTINCT\n" +
                    "  s4.code,\n" +
                    "  subj.name_kz                                                                   subjectName,\n" +
                    "  credit.credit,\n" +
                    "  ects.ects,\n" +
                    "  trim(u.LAST_NAME || ' ' || u.FIRST_NAME || ' ' || coalesce(u.MIDDLE_NAME, '')) FIO,\n" +
                    "  control.type_name                                                              examType\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id\n" +
                    "   WHERE\n" +
                    "  usr.deleted = FALSE AND\n" +
                    "  subj.subject_cycle_id IS NOT NULL AND subj.module_id=3\n" +
                    "AND\n" +
                    "  usr.locked = FALSE AND usr.id = " + studentId;
            try {
                java.util.List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlTeachAdd2, para);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;
                        for (int i = 0; i < 6; i++) {
                            insertCell(table1, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }

                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            String sqlTeachSum2 = "SELECT DISTINCT sum( credit.credit) credit, sum( ects.ects) ects\n" +
                    "FROM student_subject stu_subj\n" +
                    "  INNER JOIN student_education stu_edu ON stu_subj.student_id = stu_edu.id\n" +
                    "  INNER JOIN student ON stu_edu.student_id = student.id AND stu_edu.child_id IS NULL\n" +
                    "  INNER JOIN users usr ON student.id = usr.id\n" +
                    "  INNER JOIN semester_subject sem_subj ON stu_subj.subject_id = sem_subj.id\n" +
                    "  INNER JOIN semester_data sem_data ON sem_subj.semester_data_id = sem_data.id\n" +
                    "  INNER JOIN semester sem\n" +
                    "    ON sem.study_year_id = stu_edu.study_year_id AND sem.semester_period_id = sem_data.semester_period_id\n" +
                    "  INNER JOIN subject subj ON sem_subj.subject_id = subj.id\n" +
                    "  INNER JOIN creditability credit ON subj.creditability_id = credit.id\n" +
                    "  INNER JOIN ects ON subj.ects_id = ects.id\n" +
                    "  INNER JOIN subject_module module ON subj.module_id = module.id\n" +
                    "  INNER JOIN control_type control ON subj.control_type_id = control.id\n" +
                    "  LEFT JOIN teacher_subject ts ON ts.subject_id = subj.id\n" +
                    "  LEFT JOIN subject sbj ON ts.subject_id = sbj.id\n" +
                    "  LEFT JOIN users u ON u.id = ts.employee_id\n" +
                    "  INNER JOIN student_teacher_subject s3 ON s3.teacher_subject_id = ts.id\n" +
                    "  LEFT JOIN pair_subject s4 ON subj.id = s4.subject_id \n" +
                    "   WHERE\n" +
                    "  usr.id = " + studentId +
                    "  AND sem_data.semester_period_id=2";

            insertCell(table1, ("Семестрде барлығы"), Element.ALIGN_LEFT, 2, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));

            try {
                List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sqlTeachSum2, par);
                if (!tmpList.isEmpty()) {
                    for (Object o : tmpList) {
                        Object[] oo = (Object[]) o;

                        for (int i = 0; i < 2; i++) {
                            insertCell(table1, oo[i] != null ? oo[i] instanceof String ? (String) oo[i] : String.valueOf(oo[i]) : "", Element.ALIGN_LEFT, 1, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));
                        }
                    }
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to load absents list", ex);
            }

            insertCell(table1, (" "), Element.ALIGN_LEFT, 2, EmployeePdfCreator.getFont(fontSize, Font.NORMAL));


            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));


            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            //document.add(new Paragraph("Студенттің қолы .............................................       Күні   «.......»........................... 20......ж.\n", EmployeePdfCreator.getFont(fontSize, Font.NORMAL)));


            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));

            try{
                document.add( table );
            }catch (Exception e){
                CommonUtils.LOG.error(e.getMessage());
                document.open();
            }



            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("\n"));

            try{
                document.add(table1);
            }catch (Exception e){
                CommonUtils.LOG.error(e.getMessage());
                document.open();
            }

            document.add(new Paragraph("\n "));
            document.add(new Paragraph("\n "));
            document.add(new Paragraph("Студент    __________  _______________________\n" +
                    "                        (қолы)            (Т.А.Ә.)            \n", EmployeePdfCreator.getFont(fontSize, Font.BOLD)));

            document.add(new Paragraph("Эдвайзер    __________  _______________________\n" +
                    "                        (қолы)            (Т.А.Ә.)            \n", EmployeePdfCreator.getFont(fontSize, Font.BOLD)));

            document.add(new Paragraph("Тіркеу офисі     __________  __________________\n" +
                    "                        (қолы)            (Т.А.Ә.)            \n", EmployeePdfCreator.getFont(fontSize, Font.BOLD)));
            try{
                pdfWriter.close();
            }catch (Exception e){
                CommonUtils.LOG.error(e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void insertCell(PdfPTable table, String text, int align, int colspan, Font font) {

        PdfPCell cell = new PdfPCell(new Phrase(text.trim(), font));
        cell.setHorizontalAlignment(align);
        cell.setColspan(colspan);
        if (text.trim().equalsIgnoreCase("")) {
            cell.setMinimumHeight(10f);
        }
        table.addCell(cell);

    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return byteArrayOutputStream;
    }

    public void setByteArrayOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
        this.byteArrayOutputStream = byteArrayOutputStream;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}


