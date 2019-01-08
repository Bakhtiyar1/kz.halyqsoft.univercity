package kz.halyqsoft.univercity.modules.pdf;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import kz.halyqsoft.univercity.entity.beans.univercity.*;
import kz.halyqsoft.univercity.entity.beans.univercity.catalog.POST;
import kz.halyqsoft.univercity.utils.CommonUtils;
import org.r3a.common.dblink.facade.CommonEntityFacadeBean;
import org.r3a.common.dblink.utils.SessionFacadeFactory;
import org.r3a.common.entity.Entity;
import org.r3a.common.entity.event.EntityEvent;
import org.r3a.common.entity.event.EntityListener;
import org.r3a.common.entity.query.QueryModel;
import org.r3a.common.entity.query.from.EJoin;
import org.r3a.common.entity.query.from.FromItem;
import org.r3a.common.entity.query.where.ECriteria;
import org.r3a.common.vaadin.view.AbstractCommonView;
import org.r3a.common.vaadin.widget.dialog.Message;
import org.r3a.common.vaadin.widget.grid.GridWidget;
import org.r3a.common.vaadin.widget.grid.model.DBGridModel;
import org.r3a.common.vaadin.widget.toolbar.IconToolbar;

import java.util.*;

public class PdfAccessDocument extends AbstractCommonView implements EntityListener{

    private HorizontalSplitPanel mainHSP;

    private VerticalLayout firstVL;
    private VerticalLayout secondVL;
    private HorizontalSplitPanel innerSecondHL;
    private GridWidget pdfDocumentGW;
    private GridWidget postGW;
    private GridWidget pdfDocumentAccessPostGW;
    public PdfAccessDocument() {
        mainHSP = new HorizontalSplitPanel();
        mainHSP.setSplitPosition(30);
        mainHSP.setSizeFull();
        mainHSP.setImmediate(true);
        mainHSP.setResponsive(true);

        firstVL = new VerticalLayout();
        firstVL.setImmediate(true);
        firstVL.setResponsive(true);
        initFirstVL();

        secondVL = new VerticalLayout();
        secondVL.setImmediate(true);
        secondVL.setResponsive(true);

        innerSecondHL = new HorizontalSplitPanel();
        innerSecondHL.setImmediate(true);
        innerSecondHL.setResponsive(true);


        mainHSP.setFirstComponent(firstVL);
        mainHSP.setSecondComponent(secondVL);

        getContent().addComponent(mainHSP);
    }

    public void initFirstVL(){

        pdfDocumentGW = new GridWidget(PDF_DOCUMENT.class);
        pdfDocumentGW.setMultiSelect(false);
        DBGridModel dbGridModel = (DBGridModel) pdfDocumentGW.getWidgetModel();
        QueryModel<PDF_DOCUMENT> pdfDocumentQM = dbGridModel.getQueryModel();
        pdfDocumentQM.addWhere("deleted" , ECriteria.EQUAL , false);
        pdfDocumentGW.addEntityListener(this);
        pdfDocumentGW.setButtonVisible(IconToolbar.PREVIEW_BUTTON , false);
        pdfDocumentGW.setButtonVisible(IconToolbar.EDIT_BUTTON , false);
        pdfDocumentGW.setButtonVisible(IconToolbar.ADD_BUTTON, false);
        pdfDocumentGW.setButtonVisible(IconToolbar.DELETE_BUTTON, false);

        firstVL.addComponent(pdfDocumentGW);
    }
    @Override
    public String getViewName() {
        return getUILocaleUtil().getCaption("access");
    }

    @Override
    protected String getViewTitle(Locale locale) {
        return getUILocaleUtil().getCaption("access");
    }

    @Override
    public void initView(boolean b) throws Exception {

    }



    @Override
    public void handleEntityEvent(EntityEvent entityEvent) {
        if(entityEvent.getAction()==EntityEvent.SELECTED){
            if(entityEvent.getSource().equals(pdfDocumentGW))
            {
                if( pdfDocumentGW.getSelectedEntity()!=null) {
                    innerSecondHL.removeAllComponents();

                    postGW = new GridWidget(POST.class);
                    postGW.setResponsive(true);
                    postGW.setMultiSelect(false);
                    postGW.setImmediate(true);

                    DBGridModel dbGridModel = (DBGridModel) postGW.getWidgetModel();

                    dbGridModel.getColumnModel("studyLoad").setInGrid(false);
                    dbGridModel.getColumnModel("tp").setInGrid(false);
                    dbGridModel.getColumnModel("priority").setInGrid(false);

                    QueryModel<POST> postQM = dbGridModel.getQueryModel();

                    //FromItem fi = postQM.addJoin(EJoin.INNER_JOIN, "id", EMPLOYEE_DEPT.class, "post");
                    //fi.addJoin(EJoin.INNER_JOIN, "employee", EMPLOYEE.class, "id");
                    postGW.addEntityListener(this);
                    postGW.setButtonVisible(IconToolbar.PREVIEW_BUTTON, false);
                    postGW.setButtonVisible(IconToolbar.EDIT_BUTTON, false);
                    postGW.setButtonVisible(IconToolbar.DELETE_BUTTON, false);
                    Button allBtn = new Button(getUILocaleUtil().getCaption("send.all"));
                    allBtn.setIcon(FontAwesome.PLUS_CIRCLE);
                    allBtn.addClickListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            if(pdfDocumentGW.getSelectedEntity()!=null){
                                List<Entity> all = postGW.getAllEntities();
                                List<Entity> existing = pdfDocumentAccessPostGW.getAllEntities();
                                all.removeAll(existing);
                                List<PDF_DOCUMENT_ACCESS_POST> pdfDocumentAccessPosts = new ArrayList<>();
                                for(Entity entity : all){
                                    PDF_DOCUMENT_ACCESS_POST pdfDocumentAccessPost = new PDF_DOCUMENT_ACCESS_POST();
                                    pdfDocumentAccessPost.setPdfDocument((PDF_DOCUMENT) pdfDocumentGW.getSelectedEntity());
                                    pdfDocumentAccessPost.setPost((POST)entity);
                                    pdfDocumentAccessPosts.add(pdfDocumentAccessPost);
                                }

                                try{
                                    SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).create(pdfDocumentAccessPosts);
                                    pdfDocumentAccessPostGW.refresh();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    postGW.getToolbarPanel().addComponent(allBtn);
                    postGW.getToolbarPanel().setSizeUndefined();
                    innerSecondHL.addComponent(postGW);

                    secondVL.addComponent(innerSecondHL);
                    if (pdfDocumentAccessPostGW != null) {
                        innerSecondHL.removeComponent(pdfDocumentAccessPostGW);
                    }
                    pdfDocumentAccessPostGW = new GridWidget(PDF_DOCUMENT_ACCESS_POST.class);
                    pdfDocumentAccessPostGW.setImmediate(true);
                    pdfDocumentAccessPostGW.setMultiSelect(true);
                    pdfDocumentAccessPostGW.setResponsive(true);
                    DBGridModel pdfDbGridModel = (DBGridModel) pdfDocumentAccessPostGW.getWidgetModel();
                    pdfDbGridModel.getQueryModel().addWhere("pdfDocument", ECriteria.EQUAL, pdfDocumentGW.getSelectedEntity().getId());
                    pdfDocumentAccessPostGW.setButtonVisible(IconToolbar.PREVIEW_BUTTON, false);
                    pdfDocumentAccessPostGW.setButtonVisible(IconToolbar.EDIT_BUTTON, false);
                    pdfDocumentAccessPostGW.setButtonVisible(IconToolbar.ADD_BUTTON, false);

                    innerSecondHL.addComponent(pdfDocumentAccessPostGW);

                }else {
                    secondVL.removeAllComponents();
                }
            }
        }else if(entityEvent.getAction()==EntityEvent.UNSELECTED){
            if(entityEvent.getSource().equals(pdfDocumentGW))
            {
                secondVL.removeAllComponents();
            }
        }else{
            if(pdfDocumentGW.getSelectedEntity()==null)
            {
                secondVL.removeAllComponents();
            }
        }

    }

    @Override
    public boolean preCreate(Object o, int i) {
        if(postGW.getSelectedEntity()!=null && pdfDocumentGW.getSelectedEntity()!=null){

            for(Entity entity : pdfDocumentAccessPostGW.getAllEntities()){
                if(entity instanceof PDF_DOCUMENT_ACCESS_POST){
                    if( ((PDF_DOCUMENT_ACCESS_POST) entity).getPost().getId().getId().longValue()==postGW.getSelectedEntity().getId().getId().longValue()){
                        Message.showInfo(String.format(getUILocaleUtil().getMessage("already.exists"), getUILocaleUtil().getEntityLabel(POST.class)));
                        return false;
                    }
                }
            }

            PDF_DOCUMENT_ACCESS_POST pdfDocumentAccessPost = new PDF_DOCUMENT_ACCESS_POST();
            pdfDocumentAccessPost.setPdfDocument((PDF_DOCUMENT) pdfDocumentGW.getSelectedEntity());
            pdfDocumentAccessPost.setPost((POST)postGW.getSelectedEntity());
            try{
                SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).create(pdfDocumentAccessPost);
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                pdfDocumentAccessPostGW.refresh();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }else{
            Message.showError(getUILocaleUtil().getCaption("chooseARecord"));
        }
        return false;
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
    public boolean preSave(Object o, Entity entity, boolean b, int i) throws Exception {
        return false;
    }

    @Override
    public boolean preDelete(Object o, List<Entity> list, int i) {
        if(list!=null){
            if(list.size()>0){
                try{
                    SessionFacadeFactory.getSessionFacade(CommonEntityFacadeBean.class).delete(list);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
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