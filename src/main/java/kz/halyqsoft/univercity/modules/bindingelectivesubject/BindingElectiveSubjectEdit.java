package kz.halyqsoft.univercity.modules.bindingelectivesubject;

import com.itextpdf.text.Font;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import kz.halyqsoft.univercity.entity.beans.univercity.CATALOG_ELECTIVE_SUBJECTS;
import kz.halyqsoft.univercity.entity.beans.univercity.ELECTIVE_BINDED_SUBJECT;
import kz.halyqsoft.univercity.entity.beans.univercity.PAIR_SUBJECT;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.*;
import kz.halyqsoft.univercity.entity.beans.univercity.view.VPairSubject;
import kz.halyqsoft.univercity.entity.beans.univercity.view.V_ELECTIVE_SUBJECT;
import kz.halyqsoft.univercity.modules.userarrival.subview.dialogs.PrintDialog;
import kz.halyqsoft.univercity.utils.CommonUtils;
import kz.halyqsoft.univercity.utils.EmployeePdfCreator;
import org.r3a.common.dblink.facade.CommonEntityFacadeBean;
import org.r3a.common.dblink.utils.SessionFacadeFactory;
import org.r3a.common.entity.Entity;
import org.r3a.common.entity.ID;
import org.r3a.common.entity.event.EntityEvent;
import org.r3a.common.entity.event.EntityListener;
import org.r3a.common.entity.query.QueryModel;
import org.r3a.common.entity.query.from.EJoin;
import org.r3a.common.entity.query.from.FromItem;
import org.r3a.common.entity.query.where.ECriteria;
import org.r3a.common.vaadin.AbstractWebUI;
import org.r3a.common.vaadin.widget.ERefreshType;
import org.r3a.common.vaadin.widget.dialog.AbstractDialog;
import org.r3a.common.vaadin.widget.dialog.Message;
import org.r3a.common.vaadin.widget.dialog.select.ESelectType;
import org.r3a.common.vaadin.widget.dialog.select.custom.grid.CustomGridSelectDialog;
import org.r3a.common.vaadin.widget.form.CommonFormWidget;
import org.r3a.common.vaadin.widget.form.FormModel;
import org.r3a.common.vaadin.widget.form.field.fk.FKFieldModel;
import org.r3a.common.vaadin.widget.grid.GridWidget;
import org.r3a.common.vaadin.widget.grid.model.DBGridModel;
import org.r3a.common.vaadin.widget.toolbar.AbstractToolbar;

import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.*;

public class BindingElectiveSubjectEdit extends AbstractDialog {

    private CommonFormWidget studentBindingElectiveCFW;
    private GridWidget pairSubjectGW;
    private GridWidget requisitesGW;
    private ID studentBindingElectiveId;
    private BindingElectiveSubjectView bindingElectiveSubjectView;
    private ELECTIVE_BINDED_SUBJECT electiveBindedSubject;
    private SPECIALITY speciality;
    private final boolean isNew;

    BindingElectiveSubjectEdit(SPECIALITY speciality, ELECTIVE_BINDED_SUBJECT electiveBindedSubject, boolean isNew,
                               BindingElectiveSubjectView electiveSubjectView) throws Exception {
        this.isNew = isNew;
        this.bindingElectiveSubjectView = electiveSubjectView;
        this.electiveBindedSubject = electiveBindedSubject;
        this.speciality = speciality;

        if (electiveBindedSubject != null) {
            studentBindingElectiveId = electiveBindedSubject.getId();
        }
        setSizeFull();
        center();

        studentBindingElectiveCFW = createStudentElectiveBindedSubject();
        getContent().addComponent(studentBindingElectiveCFW);
        getContent().setComponentAlignment(studentBindingElectiveCFW, Alignment.TOP_CENTER);

        Button saveButton = CommonUtils.createSaveButton();
        saveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent ev) {
                if (studentBindingElectiveCFW.getWidgetModel().isModified()) {
                    studentBindingElectiveCFW.save();
                    pairSubjectGW.setVisible(true);
                }
            }
        });

        Button cancelButton = CommonUtils.createCancelButton();
        cancelButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent ev) {
                studentBindingElectiveCFW.cancel();
            }
        });

        HorizontalLayout buttonsHL = CommonUtils.createButtonPanel();
        buttonsHL.addComponents(saveButton, cancelButton);
        buttonsHL.setComponentAlignment(saveButton, Alignment.MIDDLE_CENTER);
        buttonsHL.setComponentAlignment(cancelButton, Alignment.MIDDLE_CENTER);
        getContent().addComponent(buttonsHL);
        getContent().setComponentAlignment(buttonsHL, Alignment.BOTTOM_CENTER);

        pairSubjectGW = pairSubjectGridWidget();
        refresh(electiveBindedSubject);
        getContent().addComponent(pairSubjectGW);
        getContent().setComponentAlignment(pairSubjectGW, Alignment.MIDDLE_CENTER);

        Button closeButton = new Button(getUILocaleUtil().getCaption("close"));
        closeButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                close();
                try {
                    electiveSubjectView.refresh();
                } catch (Exception e) {
                    CommonUtils.showMessageAndWriteLog("Unable to refresh elective subject view", e);
                }
            }
        });
        getContent().addComponent(closeButton);
        getContent().setComponentAlignment(closeButton, Alignment.MIDDLE_CENTER);

        AbstractWebUI.getInstance().addWindow(this);
    }

    public void refresh(ELECTIVE_BINDED_SUBJECT electiveBindedSubject) {
        List<VPairSubject> list = new ArrayList<>();
        String sql = "SELECT\n" +
                "                  pair.id,\n" +
                "                  cycle.CYCLE_SHORT_NAME cycleShortName,\n" +
                "                  subj.name_" + CommonUtils.getLanguage() + "      subjectName,\n" +
                "                  traject.name trajectoryName," +
                "                  credit.credit,\n" +
                "                  ects.ects,\n" +
                "                  sem.semester_name semesterName,\n" +
                "                  pair.pair_number    pairNumber,\n" +
                "                  pair.aim,\n" +
                "                   pair.description   description,\n" +
                "                  pair.competence\n" +
                "                FROM pair_subject pair\n" +
                "                  INNER JOIN subject subj ON subj.id = pair.subject_id\n" +
                "                  LEFT JOIN trajectory traject ON traject.id = subj.TRAJECTORY_ID\n" +
                "                  LEFT JOIN subject_cycle cycle ON cycle.id = subj.SUBJECT_CYCLE_ID\n" +
                "                  INNER JOIN creditability credit ON credit.id = subj.creditability_id\n" +
                "                  INNER JOIN ects ects ON ects.id = subj.ects_id\n" +
                "                  INNER JOIN elective_binded_subject elect_bind ON elect_bind.id = pair.elective_binded_subject_id\n" +
                "                  INNER JOIN semester sem ON sem.id = elect_bind.semester_id\n" +
                "                WHERE pair.elective_binded_subject_id = ?1 AND subj.mandatory = FALSE AND subj.subject_cycle_id\n" +
                "                IS NOT NULL" +
                " order by trajectoryName";
        Map<Integer, Object> params = new HashMap<>();
        if (electiveBindedSubject != null) {
            params.put(1, electiveBindedSubject.getId().getId());
        } else {
            params.put(1, -1);
        }
        try {
            List tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).
                    lookupItemsList(sql, params);
            if (!tmpList.isEmpty()) {
                for (Object o : tmpList) {
                    Object[] oo = (Object[]) o;
                    VPairSubject pairSubject = new VPairSubject();
                    ID pairSubjectId = ID.valueOf((long) oo[0]);
                    pairSubject.setId(pairSubjectId);
                    pairSubject.setCycleShortName((String) oo[1]);
                    pairSubject.setSubjectName((String) oo[2]);
                    pairSubject.setTrajectoryName((String) oo[3]);
                    pairSubject.setCredit(((BigDecimal) oo[4]).intValue());
                    pairSubject.setEcts(((BigDecimal) oo[5]).intValue());
                    pairSubject.setSemesterName((String) oo[6]);
                    pairSubject.setPairNumber((Long) oo[7]);
                    pairSubject.setAim((String) oo[8]);
                    pairSubject.setDescription(((String) oo[9]));
                    pairSubject.setCompetence((String) oo[10]);

                    List<String> preRequisites = new ArrayList<>();
                    List<String> postRequisites = new ArrayList<>();

                    QueryModel<SUBJECT_REQUISITE> requisistesQM = new QueryModel<>(SUBJECT_REQUISITE.class);
                    requisistesQM.addWhere("pairSubject", ECriteria.EQUAL, pairSubject.getId());
                    List<SUBJECT_REQUISITE> requisites = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).
                            lookup(requisistesQM);
                    for (SUBJECT_REQUISITE requisite : requisites) {
                        if (requisite.isPreRequisite()) {
                            preRequisites.add(requisite.getSubject().toString());
                        } else {
                            postRequisites.add(requisite.getSubject().toString());
                        }
                    }
                    pairSubject.setPrerequisites(preRequisites);
                    pairSubject.setPostrequisites(postRequisites);

                    list.add(pairSubject);
                }
            }
        } catch (Exception ex) {
            CommonUtils.showMessageAndWriteLog("Unable to load subjects table", ex);
        }

        ((DBGridModel) pairSubjectGW.getWidgetModel()).setEntities(list);
        try {
            pairSubjectGW.refresh();
        } catch (Exception ex) {
            CommonUtils.showMessageAndWriteLog("Unable to refresh subjects table", ex);
        }
    }

    private CommonFormWidget createStudentElectiveBindedSubject() throws Exception {
        CommonFormWidget studentBindingElectiveFW = new CommonFormWidget(ELECTIVE_BINDED_SUBJECT.class);
        studentBindingElectiveFW.addEntityListener(new StudentCreativeExamListener());
        FormModel studentBindingElectiveFM = studentBindingElectiveFW.getWidgetModel();
        studentBindingElectiveFM.setReadOnly(false);
        String errorMessage = getUILocaleUtil().getCaption("title.error").concat(": ").
                concat("Binding elective subject");//TODO resource
        studentBindingElectiveFM.setErrorMessageTitle(errorMessage);
        studentBindingElectiveFM.setButtonsVisible(false);

        if (!isNew) {
            studentBindingElectiveFM.getFieldModel("semester").setReadOnly(true);
        }

        QueryModel<SEMESTER> semesterQM = new QueryModel<>(SEMESTER.class);
        semesterQM.addJoin(EJoin.INNER_JOIN, "id", ELECTIVE_BINDED_SUBJECT.class,
                "semester");
        List<SEMESTER> semesters = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).
                lookup(semesterQM);
        List<ID> ids = new ArrayList<>();
        for (SEMESTER semester : semesters) {
            ids.add(semester.getId());
        }

        QueryModel studentQM = ((FKFieldModel) studentBindingElectiveFM.getFieldModel("semester")).getQueryModel();
        if (isNew) {
            ELECTIVE_BINDED_SUBJECT studentCreativeExam = (ELECTIVE_BINDED_SUBJECT) studentBindingElectiveFM.createNew();
        } else {
            try {
                ELECTIVE_BINDED_SUBJECT studentCreativeExam = SessionFacadeFactory.getSessionFacade(
                        CommonEntityFacadeBean.class).lookup(ELECTIVE_BINDED_SUBJECT.class, studentBindingElectiveId);
                if (studentCreativeExam != null) {
                    studentBindingElectiveFM.loadEntity(studentCreativeExam.getId());
                }
            } catch (NoResultException ex) {
                ELECTIVE_BINDED_SUBJECT studentCreativeExam = (ELECTIVE_BINDED_SUBJECT) studentBindingElectiveFM.
                        createNew();
            }
        }
        return studentBindingElectiveFW;
    }

    private GridWidget pairSubjectGridWidget() throws Exception {
        if (studentBindingElectiveId == null && !studentBindingElectiveCFW.getWidgetModel().isCreateNew()) {
            ELECTIVE_BINDED_SUBJECT electiveBindedSubject = (ELECTIVE_BINDED_SUBJECT) studentBindingElectiveCFW.
                    getWidgetModel().getEntity();
            if (electiveBindedSubject != null) {
                studentBindingElectiveId = electiveBindedSubject.getId();
            }
        }

        GridWidget studentElectiveSubjectGW = new GridWidget(VPairSubject.class);
        if (isNew) {
            studentElectiveSubjectGW.setVisible(false);
            studentBindingElectiveId = ID.valueOf(-1);
        }
        studentElectiveSubjectGW.setButtonVisible(AbstractToolbar.REFRESH_BUTTON, false);
        studentElectiveSubjectGW.setButtonVisible(AbstractToolbar.PREVIEW_BUTTON, false);
        studentElectiveSubjectGW.setButtonVisible(AbstractToolbar.HELP_BUTTON, true);
        studentElectiveSubjectGW.setButtonDescription(AbstractToolbar.HELP_BUTTON, "add.requisites");
        studentElectiveSubjectGW.addButtonClickListener(AbstractToolbar.HELP_BUTTON, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (studentElectiveSubjectGW.getSelectedEntities().isEmpty()) {
                    Message.showInfo(getUILocaleUtil().getMessage("info.noentityedit"));
                } else {
                    new RequisitesDialog(BindingElectiveSubjectEdit.this, electiveBindedSubject,
                            (VPairSubject) studentElectiveSubjectGW.getSelectedEntity());
                }
            }
        });
        studentElectiveSubjectGW.setButtonVisible(AbstractToolbar.PRINT_BUTTON, true);
        studentElectiveSubjectGW.addButtonClickListener(AbstractToolbar.PRINT_BUTTON, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                List<String> tableHeader = new ArrayList<>();
                List<List<String>> tableBody = new ArrayList<>();

                String fileName = "document";

                tableHeader.add("№");
                tableHeader.add(getUILocaleUtil().getEntityFieldLabel(SUBJECT.class, "subjectCycle"));//cycle
                tableHeader.add(getUILocaleUtil().getEntityFieldLabel(V_ELECTIVE_SUBJECT.class, "subjectName"));
                tableHeader.add(getUILocaleUtil().getEntityFieldLabel(SUBJECT.class, "trajectory"));//trajectory
                tableHeader.add(getUILocaleUtil().getEntityFieldLabel(V_ELECTIVE_SUBJECT.class, "credit"));
                tableHeader.add(getUILocaleUtil().getEntityFieldLabel(SUBJECT.class, "ects"));
                tableHeader.add(getUILocaleUtil().getEntityLabel(SEMESTER.class));
                tableHeader.add(getUILocaleUtil().getCaption("prerequisites") + " / " + getUILocaleUtil().getCaption("postrequisites"));
                tableHeader.add(getUILocaleUtil().getEntityFieldLabel(PAIR_SUBJECT.class, "aim"));
                tableHeader.add(getUILocaleUtil().getEntityFieldLabel(PAIR_SUBJECT.class, "description"));
                tableHeader.add(getUILocaleUtil().getEntityFieldLabel(PAIR_SUBJECT.class, "competence"));

                for (int i = 0; i < studentElectiveSubjectGW.getAllEntities().size(); i++) {
                    ArrayList<String> rowList = new ArrayList<>();
                    rowList.add((1 + i) + "");
                    VPairSubject vPairSubject = (VPairSubject) studentElectiveSubjectGW.getAllEntities().get(i);
                    rowList.add(checkForNull(vPairSubject.getCycleShortName()));
                    rowList.add(checkForNull(vPairSubject.getSubjectName()));
                    rowList.add(checkForNull(vPairSubject.getTrajectoryName()));
                    rowList.add(checkForNull(vPairSubject.getCredit() + ""));
                    rowList.add(vPairSubject.getEcts() + "");
                    rowList.add(vPairSubject.getSemesterName());
                    String preRequsuites = getUILocaleUtil().getCaption("prerequisites") + ": ";
                    String postRequsuites = getUILocaleUtil().getCaption("postrequisites") + ": ";

                    for (int j = 0; j < vPairSubject.getPrerequisites().size(); j++) {
                        preRequsuites += vPairSubject.getPrerequisites().get(j);
                        if (j + 1 != vPairSubject.getPrerequisites().size()) {
                            preRequsuites += ", ";
                        }
                    }

                    for (int j = 0; j < vPairSubject.getPostrequisites().size(); j++) {
                        postRequsuites += vPairSubject.getPostrequisites().get(j);
                        if (j + 1 != vPairSubject.getPostrequisites().size()) {
                            postRequsuites += ", ";
                        }
                    }
                    rowList.add(preRequsuites + "\n" + postRequsuites);
                    rowList.add(checkForNull(vPairSubject.getAim()));
                    rowList.add(checkForNull(vPairSubject.getDescription()));
                    rowList.add(checkForNull(vPairSubject.getCompetence()));
                    tableBody.add(rowList);
                }
                Font font = EmployeePdfCreator.getFont(6, Font.BOLD);
                PrintDialog printDialog = new PrintDialog(tableHeader, tableBody, CommonUtils.getUILocaleUtil().getCaption("print"), fileName, font);
                printDialog.getPdfBtn().setVisible(false);
            }
        });

        studentElectiveSubjectGW.addEntityListener(new StudentCreativeExamSubjectListener());

        DBGridModel studentElectiveSubjectGM = (DBGridModel) studentElectiveSubjectGW.getWidgetModel();
        studentElectiveSubjectGM.setTitleVisible(false);
        studentElectiveSubjectGM.setMultiSelect(false);
        studentElectiveSubjectGM.setRefreshType(ERefreshType.MANUAL);
        studentElectiveSubjectGM.setCrudEntityClass(PAIR_SUBJECT.class);
        FKFieldModel subjectFM = (FKFieldModel) studentElectiveSubjectGM.getFormModel().getFieldModel("subject");
        subjectFM.setSelectType(ESelectType.CUSTOM_GRID);
        subjectFM.setDialogHeight(400);
        subjectFM.setDialogWidth(1000);

        QueryModel subjectQM = ((FKFieldModel) studentElectiveSubjectGM.getFormModel().getFieldModel("subject")).
                getQueryModel();
        FromItem specFI = subjectQM.addJoin(EJoin.INNER_JOIN, "chair", SPECIALITY.class, "department");
        subjectQM.addWhere("subjectCycle", ECriteria.LESS, ID.valueOf(4));
        subjectQM.addWhereNull("practiceType");
        subjectQM.addWhere("mandatory", false);
        subjectQM.addWhereAnd("deleted", Boolean.FALSE);
        subjectQM.addWhere(specFI, "deleted", false);

        QueryModel<DEPARTMENT> chairQM = new QueryModel<>(DEPARTMENT.class);
        chairQM.addWhereNotNull("parent");
        chairQM.addWhereAnd("deleted", Boolean.FALSE);
        chairQM.addWhereAnd("fc", Boolean.FALSE);
        chairQM.addOrder("deptName");
        BeanItemContainer<DEPARTMENT> chairBIC = new BeanItemContainer<>(DEPARTMENT.class, SessionFacadeFactory
                .getSessionFacade(CommonEntityFacadeBean.class).lookup(chairQM));
        ComboBox chairCB = new ComboBox();
        chairCB.setContainerDataSource(chairBIC);
        chairCB.setImmediate(true);
        chairCB.setNullSelectionAllowed(true);
        chairCB.setTextInputAllowed(true);
        chairCB.setFilteringMode(FilteringMode.CONTAINS);
        chairCB.setPageLength(0);
        chairCB.setWidth(400, Unit.PIXELS);

        TextField nameRuTF = new TextField();
        nameRuTF.setNullSettingAllowed(true);

        TextField nameKzTF = new TextField();
        nameKzTF.setNullSettingAllowed(true);

        CustomGridSelectDialog customGridSelectDialog = subjectFM.getCustomGridSelectDialog();
        customGridSelectDialog.setMultiSelect(false);
        customGridSelectDialog.getFilterModel().addFilter("chair", chairCB);
        customGridSelectDialog.getFilterModel().addFilter("nameRU", nameRuTF);
        customGridSelectDialog.getFilterModel().addFilter("nameKZ", nameKzTF);
        customGridSelectDialog.initFilter();

        return studentElectiveSubjectGW;
    }

    private String checkForNull(String s) {
        if (s == null)
            return "";
        return s;
    }

    private class StudentCreativeExamListener implements EntityListener {

        @Override
        public boolean preSave(Object o, Entity entity, boolean isNew, int i) throws Exception {
            ELECTIVE_BINDED_SUBJECT electiveBindedSubject = (ELECTIVE_BINDED_SUBJECT) entity;
            if (isNew) {
                try {

                    QueryModel<CATALOG_ELECTIVE_SUBJECTS> catQM = new QueryModel<>(CATALOG_ELECTIVE_SUBJECTS.class);
                    SPECIALITY spec = (SPECIALITY) bindingElectiveSubjectView.getSpecCB().getValue();
                    ENTRANCE_YEAR year = (ENTRANCE_YEAR) bindingElectiveSubjectView.getYearCB().getValue();
                    catQM.addWhere("speciality", ECriteria.EQUAL, spec.getId());
                    catQM.addWhere("entranceYear", ECriteria.EQUAL, year.getId());
                    CATALOG_ELECTIVE_SUBJECTS cat = getElectiveSubjects(catQM, spec, year);
                    electiveBindedSubject.setCatalogElectiveSubjects(cat);
                    SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).create(electiveBindedSubject);

                    studentBindingElectiveCFW.getWidgetModel().loadEntity(electiveBindedSubject.getId());
                    studentBindingElectiveCFW.refresh();

                    BindingElectiveSubjectEdit.this.electiveBindedSubject = electiveBindedSubject;
                    CommonUtils.showSavedNotification();

                } catch (Exception ex) {
                    CommonUtils.showMessageAndWriteLog("Unable to create binding elective subject", ex);
                }
            } else {
                try {
                    SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).merge(electiveBindedSubject);
                    CommonUtils.showSavedNotification();
                } catch (Exception ex) {
                    CommonUtils.showMessageAndWriteLog("Unable to merge binding elective subject", ex);
                }
            }
            return false;
        }

        @Override
        public void handleEntityEvent(EntityEvent entityEvent) {
        }

        @Override
        public boolean preCreate(Object o, int i) {
            if (studentBindingElectiveCFW.getWidgetModel().isCreateNew()) {
                Message.showInfo(getUILocaleUtil().getMessage("info.save.base.data.first"));
                return false;
            }
            return true;
        }

        @Override
        public void onCreate(Object o, Entity entity, int i) {
        }

        @Override
        public boolean onEdit(Object o, Entity entity, int i) {
            return false;
        }

        @Override
        public boolean onPreview(Object o, Entity entity, int i) {
            return false;
        }

        @Override
        public void beforeRefresh(Object o, int i) {
        }

        @Override
        public void onRefresh(Object o, List<Entity> list) {
        }

        @Override
        public void onFilter(Object o, QueryModel queryModel, int i) {
        }

        @Override
        public void onAccept(Object o, List<Entity> list, int i) {
        }

        @Override
        public boolean preDelete(Object o, List<Entity> list, int i) {
            return false;
        }

        @Override
        public void onDelete(Object o, List<Entity> list, int i) {
        }

        @Override
        public void deferredCreate(Object o, Entity entity) {
        }

        @Override
        public void deferredDelete(Object o, List<Entity> list) {
        }

        @Override
        public void onException(Object o, Throwable throwable) {
        }
    }

    private class StudentCreativeExamSubjectListener implements EntityListener {

        @Override
        public boolean preSave(Object o, Entity entity, boolean isNew, int i) throws Exception {
            PAIR_SUBJECT studentCreativeExamSubject = (PAIR_SUBJECT) entity;
            FormModel fm = studentBindingElectiveCFW.getWidgetModel();
            if (isNew) {
                try {
                    ELECTIVE_BINDED_SUBJECT studentCreativeExam = (ELECTIVE_BINDED_SUBJECT) fm.getEntity();
                    studentBindingElectiveId = studentCreativeExam.getId();
                    studentCreativeExamSubject.setElectveBindedSubject(studentCreativeExam);
                    SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).create(studentCreativeExamSubject);

//                    QueryModel studentCreativeExamSubjectQM = ((DBGridModel) pairSubjectGW.
//                            getWidgetModel()).getQueryModel();
//                    studentCreativeExamSubjectQM.addWhere("electiveBindedSubject", ECriteria.EQUAL, fm.getEntity().getId());

                    pairSubjectGW.refresh();
                    CommonUtils.showSavedNotification();
                } catch (Exception ex) {
                    CommonUtils.showMessageAndWriteLog("Unable to create student binding elective subject", ex);
                }
            } else {
                try {
                    SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).merge(studentCreativeExamSubject);
                    pairSubjectGW.refresh();
                    CommonUtils.showSavedNotification();
                } catch (Exception ex) {
                    CommonUtils.showMessageAndWriteLog("Unable to merge student binding elective subject", ex);
                }
            }
            refresh(electiveBindedSubject);
            return false;
        }

        @Override
        public boolean preDelete(Object o, List<Entity> entities, int i) {
            List<PAIR_SUBJECT> delList = new ArrayList<>();
            try {
                for (Entity entity : entities) {
                    delList.add(SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).
                            lookup(PAIR_SUBJECT.class, entity.getId()));
                }
            } catch (Exception ex) {
                CommonUtils.showMessageAndWriteLog("Unable to delete student binding elective subject", ex);
            }
            try {
                SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).delete(delList);
                refresh(electiveBindedSubject);
            } catch (Exception ex) {
                CommonUtils.LOG.error("Unable to delete student binding elective subject: ", ex);
                Message.showError(getUILocaleUtil().getMessage("error.cannotdelentity"));
            }
            return false;
        }

        @Override
        public void handleEntityEvent(EntityEvent entityEvent) {
            if (entityEvent.getAction() == EntityEvent.CREATED
                    || entityEvent.getAction() == EntityEvent.MERGED
                    || entityEvent.getAction() == EntityEvent.REMOVED) {
                refresh(electiveBindedSubject);
            }
        }

        @Override
        public boolean preCreate(Object o, int i) {
//            if (studentBindingElectiveCFW.getWidgetModel().isCreateNew()) {
//                Message.showInfo(getUILocaleUtil().getMessage("info.save.base.data.first"));
//                return false;
//            }
//            new BindingElectiveSubjectDialog(electiveBindedSubject, BindingElectiveSubjectEdit.this, null, false);
            return true;
        }

        @Override
        public void onCreate(Object o, Entity entity, int i) {
        }

        @Override
        public boolean onEdit(Object o, Entity entity, int i) {
//            try {
//                PAIR_SUBJECT pairSubject = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).
//                        lookup(PAIR_SUBJECT.class, entity.getId());
//                new BindingElectiveSubjectDialog(electiveBindedSubject, BindingElectiveSubjectEdit.this,
//                        pairSubject, true);
//            } catch (Exception e) {
//                CommonUtils.showMessageAndWriteLog("Unable to open/edit binding elective subject dialog", e);
//            }
            return true;
        }

        @Override
        public boolean onPreview(Object o, Entity entity, int i) {
            return false;
        }

        @Override
        public void beforeRefresh(Object o, int i) {
        }

        @Override
        public void onRefresh(Object o, List<Entity> list) {
        }

        @Override
        public void onFilter(Object o, QueryModel queryModel, int i) {
        }

        @Override
        public void onAccept(Object o, List<Entity> list, int i) {
        }

        @Override
        public void onDelete(Object o, List<Entity> list, int i) {
        }

        @Override
        public void deferredCreate(Object o, Entity entity) {
        }

        @Override
        public void deferredDelete(Object o, List<Entity> list) {
        }

        @Override
        public void onException(Object o, Throwable throwable) {
        }
    }

    private CATALOG_ELECTIVE_SUBJECTS getElectiveSubjects(QueryModel<CATALOG_ELECTIVE_SUBJECTS> catQM,
                                                          SPECIALITY spec, ENTRANCE_YEAR year) throws Exception {
        CATALOG_ELECTIVE_SUBJECTS catalogElectiveSubjects;
        try {
            catalogElectiveSubjects = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupSingle(catQM);
        } catch (NoResultException ex) {
            catalogElectiveSubjects = new CATALOG_ELECTIVE_SUBJECTS();
            catalogElectiveSubjects.setCreated(new Date());
            catalogElectiveSubjects.setDeleted(Boolean.FALSE);
            catalogElectiveSubjects.setEntranceYear(year);
            catalogElectiveSubjects.setSpeciality(spec);
            SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).create(catalogElectiveSubjects);
        }
        return catalogElectiveSubjects;
    }

    @Override
    protected String createTitle() {
        return "Binding elective subject";//TODO resource
    }
}
