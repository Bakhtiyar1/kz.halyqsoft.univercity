package kz.halyqsoft.univercity.modules.enrolltheapplicants;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import kz.halyqsoft.univercity.entity.beans.univercity.STUDENT;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.STUDENT_CATEGORY;
import kz.halyqsoft.univercity.entity.beans.univercity.view.VStudent;
import kz.halyqsoft.univercity.utils.CommonUtils;
import org.r3a.common.dblink.facade.CommonEntityFacadeBean;
import org.r3a.common.dblink.utils.SessionFacadeFactory;
import org.r3a.common.entity.ID;
import org.r3a.common.entity.beans.AbstractTask;
import org.r3a.common.vaadin.view.AbstractTaskView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author Omarbek
 * @created on 04.05.2018
 */
public class EnrollTheApplicantsView extends AbstractTaskView {

    private Grid studentGrid;

    public EnrollTheApplicantsView(AbstractTask task) throws Exception {
        super(task);
    }

    @Override
    public void initView(boolean b) throws Exception {
        initButton();
        initGrid();
    }

    private void initButton() {
        Button enrollButton = new Button("enroll");//TODO
        enrollButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    Collection<Object> selectedRows = studentGrid.getSelectedRows();
                    for (Object object : selectedRows) {
                        VStudent vStudent = (VStudent) object;
                        STUDENT student = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).
                                lookup(STUDENT.class, vStudent.getId());
                        student.setCategory(SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).
                                lookup(STUDENT_CATEGORY.class, ID.valueOf(3)));
                        SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).merge(student);
                    }
                    CommonUtils.showSavedNotification();
                    refresh();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        getContent().addComponent(enrollButton);
        getContent().setComponentAlignment(enrollButton, Alignment.MIDDLE_LEFT);
    }

    private void initGrid() {
        studentGrid = new Grid();
        studentGrid.setCaption(getUILocaleUtil().getEntityLabel(VStudent.class));
        studentGrid.setSizeFull();
        studentGrid.addStyleName("lesson-detail");
        studentGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        studentGrid.addColumn("code").setHeaderCaption(getUILocaleUtil().getEntityFieldLabel(VStudent.class,
                "code")).setHidable(false).setWidth(120);
        studentGrid.addColumn("fio").setHeaderCaption(getUILocaleUtil().getEntityFieldLabel(VStudent.class,
                "fio")).setHidable(false);
        studentGrid.addColumn("category").setHeaderCaption(getUILocaleUtil().getEntityFieldLabel(VStudent.class,
                "category")).setHidable(false);
        studentGrid.addColumn("status").setHeaderCaption(getUILocaleUtil().getEntityFieldLabel(VStudent.class,
                "status")).setHidable(false);
        studentGrid.addColumn("faculty").setHeaderCaption(getUILocaleUtil().getEntityFieldLabel(VStudent.class,
                "faculty")).setHidable(false);
        studentGrid.addColumn("specialty").setHeaderCaption(getUILocaleUtil().getEntityFieldLabel(VStudent.class,
                "specialty")).setHidable(false);
        studentGrid.addColumn("studyYear").setHeaderCaption(getUILocaleUtil().getEntityFieldLabel(VStudent.class,
                "studyYear")).setHidable(false);
        studentGrid.addColumn("languageName").setHeaderCaption(getUILocaleUtil().getEntityFieldLabel(VStudent.class,
                "languageName")).setHidable(false);

        refresh();

        getContent().addComponent(studentGrid);
        getContent().setComponentAlignment(studentGrid, Alignment.MIDDLE_CENTER);
    }

    public void refresh() {

        List<VStudent> list = new ArrayList<>();
        String sql = "SELECT " +
                "  stu.ID, " +
                "  usr.CODE, " +
                "  trim(usr.LAST_NAME || ' ' || usr.FIRST_NAME || ' ' || coalesce(usr.MIDDLE_NAME, '')) FIO, " +
                "  stu_status.STATUS_NAME, " +
                "  dep.DEPT_SHORT_NAME                                                                  FACULTY, " +
                "  spec.SPEC_NAME " +
                "FROM STUDENT stu INNER JOIN USERS usr ON stu.ID = usr.ID " +
                "  INNER JOIN STUDENT_EDUCATION stu_edu ON stu.ID = stu_edu.STUDENT_ID AND stu_edu.CHILD_ID IS NULL " +
                "  INNER JOIN STUDENT_STATUS stu_status ON stu_edu.STUDENT_STATUS_ID = stu_status.ID " +
                "  INNER JOIN DEPARTMENT dep ON stu_edu.FACULTY_ID = dep.ID " +
                "  INNER JOIN SPECIALITY spec ON stu_edu.SPECIALITY_ID = spec.ID " +
                "WHERE stu.category_id = 1 AND usr.deleted = FALSE AND dep.deleted = FALSE AND spec.deleted = FALSE " +
                "ORDER BY FIO";
        try {
            List<Object> tmpList = SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).lookupItemsList(sql, new HashMap<>());
            if (!tmpList.isEmpty()) {
                for (Object o : tmpList) {
                    Object[] oo = (Object[]) o;
                    VStudent vs = new VStudent();
                    vs.setId(ID.valueOf((long) oo[0]));
                    vs.setCode((String) oo[1]);
                    vs.setFio((String) oo[2]);
                    vs.setStatus((String) oo[3]);
                    vs.setFaculty((String) oo[4]);
                    vs.setSpecialty((String) oo[5]);
                    list.add(vs);
                }
            }
        } catch (Exception ex) {
            CommonUtils.showMessageAndWriteLog("Unable to load student list", ex);
        }

        final BeanItemContainer<VStudent> bic = new BeanItemContainer<>(VStudent.class, list);
        studentGrid.setContainerDataSource(bic);
    }
}
